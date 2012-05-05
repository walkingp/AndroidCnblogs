package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.cnblogs.android.adapter.CommentListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.CommentHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.CommentDalHelper;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.utility.NetHelper;
/**
 * 评论（博客评论和新闻评论共用）
 * @author walkingp
 * @date:2011-12
 *
 */
public class CommentActivity extends BaseActivity {
	List<Comment> listComment = new ArrayList<Comment>();
	Comment.EnumCommentType commentType;// 评论类型：博客|新闻
	int contentId;// 主编号
	String contentTitle;// 内容标题
	String contentUrl;// 内容地址

	CommentListAdapter adapter;

	int pageIndex = 1;// 页码

	ListView listView;

	private Button comment_button_back;// 返回

	ProgressBar commentsMore_progressBar;// 主体进度条

	LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// 底部更多显示
	ProgressBar list_footer_progress;// 底部进度条

	private int lastItem;

	static final int MENU_VIEW_AUTHOR = Menu.FIRST;// 查看评论者主页
	static final int MENU_COPY = Menu.FIRST + 1;// 复制到剪贴板
	static final int MENU_SHARE = Menu.FIRST + 2;// 分享到……

	Resources res;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.comment_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask(0, true).execute();
	}
	/**
	 * 绑定事件
	 */
	private void BindControls() {
		// 上拉刷新
		((PullToRefreshListView) listView)
				.setOnRefreshListener(new OnRefreshListener() {
					@Override
					public void onRefresh() {
						new PageTask(-1, true).execute();
					}
				});
		// 下拉刷新
		listView.setOnScrollListener(new OnScrollListener() {
			/**
			 * 下拉到最后一行
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				Log.i("w", lastItem + "|" + adapter.getCount() + "|"
						+ scrollState + "|"
						+ OnScrollListener.SCROLL_STATE_IDLE);
				if (lastItem == adapter.getCount()
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					pageIndex = pageIndex + 1;
					new PageTask(pageIndex, false).execute();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				lastItem = firstVisibleItem - 2 + visibleItemCount;
			}
		});
		// 长按事件
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("请选择操作");
				menu.add(0, MENU_VIEW_AUTHOR, 0, "查看评论者主页");
				menu.add(0, MENU_COPY, 0, "复制到剪贴板");
				menu.add(0, MENU_SHARE, 0, "分享到……");
			}
		});
	}
	// 长按菜单响应函数
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemIndex = item.getItemId();
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		View v = menuInfo.targetView;
		switch (itemIndex) {
			case MENU_VIEW_AUTHOR :// 查看主页
				RedirectAuthorActivity(v);
				break;
			case MENU_COPY :// 拷贝到剪贴板
				CopyText(v);
				break;
			case MENU_SHARE :// 分享到
				ShareTo(v);
				break;
		}
		return super.onContextItemSelected(item);
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls() {
		int type = getIntent().getIntExtra("commentType", 0);
		commentType = Comment.EnumCommentType.values()[type];
		contentId = getIntent().getIntExtra("contentId", 0);
		contentTitle = getIntent().getStringExtra("title");
		contentUrl = getIntent().getStringExtra("url");

		listView = (ListView) findViewById(R.id.comment_list);
		commentsMore_progressBar = (ProgressBar) findViewById(R.id.commentList_progressBar);
		commentsMore_progressBar.setVisibility(View.VISIBLE);

		// 底部view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);

		// 返回
		comment_button_back = (Button) findViewById(R.id.comment_button_back);
		comment_button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CommentActivity.this.finish();
			}
		});
	}
	/**
	 * 多线程启动（用于上拉加载、初始化、下载加载、刷新）
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Comment>> {
		CommentDalHelper dbHelper = new CommentDalHelper(
				getApplicationContext());
		boolean isRefresh = false;
		int curPageIndex = 0;
		boolean isLocalData = false;// 是否是从本地读取的数据
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<Comment> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper
					.networkIsAvailable(getApplicationContext());
			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			// 优先读取本地数据
			List<Comment> listCommentLocal = dbHelper.GetCommentListByPage(
					_pageIndex, Config.COMMENT_PAGE_SIZE, contentId,
					commentType);

			if (isNetworkAvailable) {// 有网络情况
				List<Comment> listCommentNew = CommentHelper.GetCommentList(
						contentId, _pageIndex, commentType);
				switch (curPageIndex) {
					case -1 :// 上拉\
						List<Comment> listTmp = new ArrayList<Comment>();
						if (listComment != null && listComment.size() > 0) {
							if (listCommentNew != null
									&& listCommentNew.size() > 0) {
								int size = listCommentNew.size();
								for (int i = 0; i < size; i++) {
									if (!listComment.contains(listCommentNew
											.get(i))) {// 避免出现重复
										listTmp.add(listCommentNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// 首次加载
					case 1 :// 刷新
						if (listCommentNew != null && listCommentNew.size() > 0) {
							return listCommentNew;
						}
						break;
					default :// 下拉
						List<Comment> listT = new ArrayList<Comment>();
						if (listComment != null && listComment.size() > 0) {// 避免首页无数据时
							if (listCommentNew != null
									&& listCommentNew.size() > 0) {
								int size = listCommentNew.size();
								for (int i = 0; i < size; i++) {
									if (!listComment.contains(listCommentNew
											.get(i))) {// 避免出现重复
										listT.add(listCommentNew.get(i));
									}
								}
							}
						}
						return listT;
				}
			} else {// 无网络情况
				isLocalData = true;
				if (curPageIndex == -1) {// 上拉不加载数据
					return null;
				}
				return listCommentLocal;
			}

			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		/**
		 * 加载内容
		 */
		@Override
		protected void onPostExecute(List<Comment> result) {
			// 网络不可用并且本地没有保存数据
			if (result == null || result.size() == 0) {// 没有新数据
				((PullToRefreshListView) listView).onRefreshComplete();
				if (!NetHelper.networkIsAvailable(getApplicationContext())
						&& curPageIndex > 1) {// 下拉并且没有网络
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					listView.removeFooterView(viewFooter);
				}
				return;
			}
			int size = result.size();
			if (size >= Config.COMMENT_PAGE_SIZE
					&& listView.getFooterViewsCount() == 0) {
				listView.addFooterView(viewFooter);
			}
			// 保存到数据库
			if (!isLocalData) {
				dbHelper.SynchronyData2DB(result);
			}

			if (curPageIndex == -1) {// 上拉刷新
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// 首次加载
				listComment = result;

				commentsMore_progressBar.setVisibility(View.GONE);
				adapter = new CommentListAdapter(getApplicationContext(),
						listComment, pageIndex);
				listView.setAdapter(adapter);

				// 传递参数
				((PullToRefreshListView) listView).SetDataRow(listComment
						.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.COMMENT_PAGE_SIZE);
			} else if (curPageIndex == 1) {// 刷新
				try {// 避免首页无网络加载，按刷新按钮
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
					} else if (result != null) {
						adapter = new CommentListAdapter(
								getApplicationContext(), result, pageIndex);
						listView.setAdapter(adapter);
					}
					adapter.AddMoreData(result);
				} catch (Exception ex) {
				}
			} else {// 下拉
				adapter.AddMoreData(result);
			}

			if (isRefresh) {// 刷新时处理
				((PullToRefreshListView) listView).onRefreshComplete();
			}
		}
		@Override
		protected void onPreExecute() {
			// 主体进度条
			if (listView.getCount() == 0) {
				commentsMore_progressBar.setVisibility(View.VISIBLE);
			}

			if (!isRefresh) {// 底部控件，刷新时不做处理
				TextView tvFooterMore = (TextView) findViewById(R.id.tvFooterMore);
				if (tvFooterMore != null) {
					tvFooterMore
							.setText(R.string.pull_to_refresh_refreshing_label);
					tvFooterMore.setVisibility(View.VISIBLE);
				}
				ProgressBar list_footer_progress = (ProgressBar) findViewById(R.id.list_footer_progress);
				list_footer_progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	/**
	 * 跳转到博主所有随笔
	 * 
	 * @param v
	 */
	private void RedirectAuthorActivity(View v) {
		// 博客名
		TextView tvAuthor = (TextView) (v.findViewById(R.id.comment_user_name));
		String blogTitle = tvAuthor.getText().toString();
		if (blogTitle == "") {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT);
			return;
		}
		// 用户名
		TextView tvUrl = (TextView) (v.findViewById(R.id.comment_user_url));
		String homeUrl = tvUrl.getText().toString();
		String userName = UserHelper.GetHomeUrlName(homeUrl);
		if (userName == "") {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT);
			return;
		}

		Intent intent = new Intent();
		intent.setClass(CommentActivity.this, AuthorBlogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("author", userName);// 用户名
		bundle.putString("blogName", blogTitle);// 博客标题

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * 分享到
	 * 
	 * @param v
	 */
	private void ShareTo(View v) {
		TextView tvContent = (TextView) (v.findViewById(R.id.comment_content));
		String text = tvContent.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, contentTitle);
		String shareContent = "《" + contentTitle + "》,评论内容：" + text + " 原文链接："
				+ contentUrl + " 分享自：" + res.getString(R.string.app_name)
				+ "Android客户端(" + res.getString(R.string.app_homepage) + ")";
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent, contentTitle));
	}
	/**
	 * 复制到剪贴板
	 * 
	 * @param v
	 */
	private void CopyText(View v) {
		TextView tvContent = (TextView) (v.findViewById(R.id.comment_content));
		String text = tvContent.getText().toString();
		ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clip.setText(text);
		Toast.makeText(getApplicationContext(), R.string.sys_copy_text,
				Toast.LENGTH_SHORT).show();
	}
}
