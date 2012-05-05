package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageButton;
//import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import com.cnblogs.android.adapter.NewsListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.NetHelper;
/**
 * 新闻列表
 * @author walkingp
 * @date:2011-12
 *
 */
public class NewsActivity extends BaseMainActivity {
	List<News> listNews = new ArrayList<News>();

	NewsListAdapter adapter;

	int pageIndex = 1;// 页码

	ListView listView;

	private ImageButton refresh_btn; // 头部刷新按钮
	ProgressBar news_progress_bar; // 头部加载按钮

	ProgressBar newsBody_progressBar;// 主题进度

	LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// 底部更多显示
	ProgressBar list_footer_progress;// 底部进度条

	private int lastItem;

	static final int MENU_DETAIL = Menu.FIRST;// 查看详细
	static final int MENU_COMMENT = Menu.FIRST + 1;// 查看评论
	static final int MENU_VIEW_BROWSER = Menu.FIRST + 2;// 在浏览器中查看
	static final int MENU_SHARE_TO = Menu.FIRST + 3;// 分享到

	Resources res;// 资源
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.news_layout);

		res = this.getResources();
		InitialControls();
		InitialNewsList();
		BindEvent();
		
		UpdateListViewReceiver receiver=new UpdateListViewReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.cnblogs.com.update_newslist");
		registerReceiver(receiver, filter);
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls() {
		listView = (ListView) findViewById(R.id.news_list);
		listView.removeAllViewsInLayout();
		newsBody_progressBar = (ProgressBar) findViewById(R.id.newsList_progressBar);
		newsBody_progressBar.setVisibility(View.VISIBLE);
		// 刷新
		refresh_btn = (ImageButton) findViewById(R.id.news_refresh_btn);
		news_progress_bar = (ProgressBar) findViewById(R.id.news_progressBar);
		// 底部view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);
	}
	/**
	 * 加载初始数据(初始化)
	 */
	private void InitialNewsList() {
		new PageTask(0, true).execute();
	}
	/**
	 * 绑定事件
	 */
	private void BindEvent() {
		// 刷新
		refresh_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new PageTask(1, true).execute();
			}
		});
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
		// 点击跳转
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				RedirectDetailActivity(v);
			}
		});
		// 长按事件
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("请选择操作");
				menu.add(0, MENU_DETAIL, 0, "查看内容");
				menu.add(0, MENU_COMMENT, 0, "查看评论");
				menu.add(0, MENU_VIEW_BROWSER, 0, "在浏览器中查看");
				menu.add(0, MENU_SHARE_TO, 0, "分享到……");
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
			case MENU_DETAIL :// 详细
				RedirectDetailActivity(v);
				break;
			case MENU_COMMENT :// 评论
				RedirectCommentActivity(v);
				break;
			case MENU_VIEW_BROWSER :// 在浏览器中查看
				ViewInBrowser(v);
				break;
			case MENU_SHARE_TO :// 分享到
				ShareTo(v);
				break;
		}

		return super.onContextItemSelected(item);
	}
	/**
	 * 多线程启动（用于上拉加载、初始化、下载加载、刷新）
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<News>> {
		boolean isRefresh = false;
		int curPageIndex = 0;
		boolean isLocalData = false;// 是否是从本地读取的数据
		NewsDalHelper dbHelper = new NewsDalHelper(getApplicationContext());
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<News> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper
					.networkIsAvailable(getApplicationContext());
			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			// 优先读取本地数据
			List<News> listNewsLocal = dbHelper.GetNewsListByPage(_pageIndex,
					Config.NEWS_PAGE_SIZE);

			if (isNetworkAvailable) {// 有网络情况
				List<News> listNewsNew = NewsHelper.GetNewsList(_pageIndex);
				switch (curPageIndex) {
					case -1 :// 上拉\
						List<News> listTmp = new ArrayList<News>();
						if (listNews != null && listNews.size() > 0) {
							if (listNewsNew != null && listNewsNew.size() > 0) {
								int size = listNewsNew.size();
								for (int i = 0; i < size; i++) {
									if (!listNews.contains(listNewsNew.get(i))) {// 避免出现重复
										listTmp.add(listNewsNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// 首次加载
					case 1 :// 刷新
						if (listNewsNew != null && listNewsNew.size() > 0) {
							return listNewsNew;
						}
						break;
					default :// 下拉
						List<News> listT = new ArrayList<News>();
						if (listNews != null && listNews.size() > 0) {// 避免首页无数据时
							if (listNewsNew != null && listNewsNew.size() > 0) {
								int size = listNewsNew.size();
								for (int i = 0; i < size; i++) {
									if (!listNews.contains(listNewsNew.get(i))) {// 避免出现重复
										listT.add(listNewsNew.get(i));
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
				return listNewsLocal;
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
		protected void onPostExecute(List<News> result) {
			// 右上角
			news_progress_bar.setVisibility(View.GONE);
			refresh_btn.setVisibility(View.VISIBLE);

			// 网络不可用并且本地没有保存数据
			if (result == null || result.size() == 0) {// 没有新数据
				((PullToRefreshListView) listView).onRefreshComplete();
				if (!NetHelper.networkIsAvailable(getApplicationContext())
						&& curPageIndex > 1) {// 下拉并且没有网络
					Toast.makeText(getApplicationContext(),
							R.string.sys_network_error, Toast.LENGTH_SHORT)
							.show();
					// listView.removeFooterView(viewFooter);
				}
				return;
			}

			int size = result.size();
			if (size >= Config.BLOG_PAGE_SIZE
					&& listView.getFooterViewsCount() == 0) {
				listView.addFooterView(viewFooter);
			}

			// 保存到数据库
			if (!isLocalData) {
				dbHelper.SynchronyData2DB(result);
			}
			dbHelper.Close();

			if (curPageIndex == -1) {// 上拉刷新
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// 首次加载
				listNews = result;

				newsBody_progressBar.setVisibility(View.GONE);
				adapter = new NewsListAdapter(getApplicationContext(), listNews);
				listView.setAdapter(adapter);

				// 传递参数
				((PullToRefreshListView) listView).SetDataRow(listNews.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.NEWS_PAGE_SIZE);
			} else if (curPageIndex == 1) {// 刷新
				if (adapter != null && adapter.GetData() != null) {
					adapter.GetData().clear();
					adapter.AddMoreData(result);
				} else {
					adapter = new NewsListAdapter(getApplicationContext(),
							listNews);
					listView.setAdapter(adapter);
				}
				newsBody_progressBar.setVisibility(View.GONE);
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
				newsBody_progressBar.setVisibility(View.VISIBLE);
			}
			// 右上角
			news_progress_bar.setVisibility(View.VISIBLE);
			refresh_btn.setVisibility(View.GONE);

			if (!isRefresh) {// 底部控件，刷新时不做处理
				TextView tvFooterMore = (TextView) findViewById(R.id.tvFooterMore);
				tvFooterMore.setText(R.string.pull_to_refresh_refreshing_label);
				tvFooterMore.setVisibility(View.VISIBLE);
				ProgressBar list_footer_progress = (ProgressBar) findViewById(R.id.list_footer_progress);
				list_footer_progress.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	// ****************************************以下为菜单操作
	/**
	 * 跳转到评论
	 * 
	 * @param v
	 */
	private void RedirectCommentActivity(View v) {
		TextView tvNewsComment = (TextView) (v
				.findViewById(R.id.news_text_comments));
		TextView tvNewsId = (TextView) (v.findViewById(R.id.news_text_id));
		TextView tvNewsTitle = (TextView) (v.findViewById(R.id.news_text_title));
		TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
		int newsId = Integer.parseInt(tvNewsId.getText().toString());
		int commentCount = Integer.parseInt(tvNewsComment.getText().toString());
		String newsTitle = tvNewsTitle.getText().toString();
		String newsUrl = tvNewsUrl.getText().toString();
		// 还没有评论
		if (commentCount == 0) {
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(NewsActivity.this, CommentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("contentId", newsId);
		bundle.putInt("commentType", 1);// Comment.EnumCommentType.News.ordinal());
		bundle.putString("title", newsTitle);
		bundle.putString("url", newsUrl);

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * 查看详情
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {
		Intent intent = new Intent();
		try {
			// 传递参数
			intent.setClass(NewsActivity.this, NewsDetailActivity.class);
			Bundle bundle = new Bundle();
			TextView tvNewsId = (TextView) (v.findViewById(R.id.news_text_id));
			TextView tvNewsTitle = (TextView) (v
					.findViewById(R.id.news_text_title));
			TextView tvNewsDate = (TextView) (v
					.findViewById(R.id.news_text_date));
			TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
			TextView tvNewsComment = (TextView) (v
					.findViewById(R.id.news_text_comments));
			TextView tvNewsView = (TextView) (v
					.findViewById(R.id.news_text_view));

			String newsId = tvNewsId.getText().toString();
			String newsTitle = tvNewsTitle.getText().toString();
			String newsDate = tvNewsDate.getText().toString();
			String newsUrl = tvNewsUrl.getText().toString();
			int view = Integer.parseInt(tvNewsView.getText().toString());
			int comment = Integer.parseInt(tvNewsComment.getText().toString());

			bundle.putString("newsId", newsId);
			bundle.putString("newsTitle", newsTitle);
			bundle.putString("date", newsDate);
			bundle.putString("newsUrl", newsUrl);
			bundle.putInt("view", view);
			bundle.putInt("comment", comment);

			Log.d("newsId", newsId.toString());
			intent.putExtras(bundle);

			startActivityForResult(intent, 0);
			tvNewsTitle.setTextColor(R.color.gray);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * 在浏览器中查看
	 * 
	 * @param v
	 */
	private void ViewInBrowser(View v) {
		TextView tvBlogUrl = (TextView) (v.findViewById(R.id.news_text_url));
		String blogUrl = tvBlogUrl.getText().toString();
		Uri blogUri = Uri.parse(blogUrl);
		Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
		startActivity(it);
	}
	/**
	 * 分享到
	 * 
	 * @param v
	 */
	private void ShareTo(View v) {
		TextView tvNewsTitle = (TextView) (v.findViewById(R.id.news_text_title));
		String newsTitle = tvNewsTitle.getText().toString();
		TextView tvNewsUrl = (TextView) (v.findViewById(R.id.news_text_url));
		String newsUrl = tvNewsUrl.getText().toString();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, "请选择分享到…");
		String shareContent = "《" + newsTitle + "》，原文链接：" + newsUrl + " 分享自："
				+ res.getString(R.string.app_name) + "Android客户端("
				+ res.getString(R.string.app_homepage) + ")";
		intent.putExtra(Intent.EXTRA_TEXT, shareContent);
		startActivity(Intent.createChooser(intent, newsTitle));
	}
	/**
	 * 更新ListView为已读状态
	 * @author walknigp
	 *
	 */
	public class UpdateListViewReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context content, Intent intent) {
			/*Bundle bundle=intent.getExtras();
			int[] newsIdArr=bundle.getIntArray("newsIdArray");
			for(int i=0,len=listView.getChildCount();i<len;i++){
				View view=listView.getChildAt(i);
				TextView tvId=(TextView)view.findViewById(R.id.news_text_id);
				int newsId=Integer.parseInt(tvId.getText().toString());
				
				ImageView icoDown=(ImageView)view.findViewById(R.id.icon_downloaded);
				TextView tvTitle=(TextView)view.findViewById(R.id.news_text_title);
				
				for(int j=0,size=newsIdArr.length;j<size;j++){
					if(newsId==newsIdArr[j]){
						icoDown.setVisibility(View.VISIBLE);//已经离线
						tvTitle.setTextColor(R.color.gray);//已读
					}
				}
			}*/
		}		
	}
}
