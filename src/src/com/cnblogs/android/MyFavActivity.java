package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import com.cnblogs.android.adapter.MyFavListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.FavListHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.entity.FavList;
import com.cnblogs.android.utility.NetHelper;
/**
 * 我的收藏
 * @author walkingp
 * @date 2012-3-24
 */
public class MyFavActivity extends BaseActivity{
	List<Blog> listBlog = new ArrayList<Blog>();
	List<FavList> listFav=new ArrayList<FavList>();
	
	int pageIndex = 1;// 页码

	TextView txtAppTitle;
	
	ListView listView;
	private MyFavListAdapter adapter;// 数据源

	ProgressBar blogBody_progressBar;// 主题ListView加载框
	ImageButton blog_refresh_btn;// 刷新按钮
	ProgressBar blog_progress_bar;// 加载按钮
	
	Button btnBack;//返回按钮

	private LinearLayout viewFooter;// footer view
	TextView tvFooterMore;// 底部更多显示
	ProgressBar list_footer_progress;// 底部进度条

	Resources res;// 资源
	private ProgressDialog progressDialog;  
	private int lastItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.author_order_recommend_news_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask(0, true).execute();
		
		//注册广播
		UpdateListViewReceiver receiver=new UpdateListViewReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.cnblogs.com.update_favlist");
		registerReceiver(receiver, filter);
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls() {
		txtAppTitle=(TextView)findViewById(R.id.txtAppTitle);
		txtAppTitle.setText("我的收藏");
		btnBack=(Button)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
		
		listView = (ListView) findViewById(R.id.blog_list);
		blogBody_progressBar = (ProgressBar) findViewById(R.id.blogList_progressBar);
		blogBody_progressBar.setVisibility(View.VISIBLE);

		blog_refresh_btn = (ImageButton) findViewById(R.id.blog_refresh_btn);
		blog_progress_bar = (ProgressBar) findViewById(R.id.blog_progressBar);
		// 底部view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout) mInflater.inflate(R.layout.listview_footer,
				null, false);
	}
	/**
	 * 绑定事件
	 */
	private void BindControls() {
		// 刷新
		blog_refresh_btn.setOnClickListener(new OnClickListener() {
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
		});// 点击跳转
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
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.my_fav_contextmenu, menu);
				menu.setHeaderTitle(R.string.menu_bar_title);
			}
		});
	}

	/**
	 * 跳转到详情
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {

		Intent intent = new Intent();
		try {
			// 传递参数
			intent.setClass(MyFavActivity.this, BlogDetailActivity.class);
			Bundle bundle = new Bundle();
			TextView tvBlogId = (TextView) (v
					.findViewById(R.id.recommend_text_id));
			TextView tvBlogTitle = (TextView) (v
					.findViewById(R.id.recommend_text_title));
			TextView tvBlogAuthor = (TextView) (v
					.findViewById(R.id.recommend_text_author));
			TextView tvBlogDate = (TextView) (v
					.findViewById(R.id.recommend_text_date));
			TextView tvBlogUrl = (TextView) (v
					.findViewById(R.id.recommend_text_url));
			TextView tvBlogViewCount = (TextView) (v
					.findViewById(R.id.recommend_text_view));
			TextView tvBlogCommentCount = (TextView) (v
					.findViewById(R.id.recommend_text_comments));
			TextView tvBlogDomain = (TextView) (v
					.findViewById(R.id.recommend_text_domain));

			int blogId = Integer.parseInt(tvBlogId.getText().toString());
			String blogTitle = tvBlogTitle.getText().toString();
			String blogAuthor = tvBlogAuthor.getText().toString();
			String blogDate = tvBlogDate.getText().toString();
			String blogUrl = tvBlogUrl.getText().toString();
			String blogDomain = tvBlogDomain.getText().toString();
			int viewsCount = Integer.parseInt(tvBlogViewCount.getText()
					.toString());
			int commentCount = Integer.parseInt(tvBlogCommentCount.getText()
					.toString());

			bundle.putInt("blogId", blogId);
			bundle.putString("blogTitle", blogTitle);
			bundle.putString("author", blogAuthor);
			bundle.putString("date", blogDate);
			bundle.putString("blogUrl", blogUrl);
			bundle.putInt("view", viewsCount);
			bundle.putInt("comment", commentCount);
			bundle.putString("blogDomain", blogDomain);

			Log.d("blogId", String.valueOf(blogId));
			intent.putExtras(bundle);

			startActivity(intent);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	// 长按菜单响应函数
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int itemIndex = item.getItemId();
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		View v = menuInfo.targetView;
		switch (itemIndex) {
			case R.id.menu_unrss://取消订阅
				default:
				//显示ProgressDialog  
	            progressDialog = ProgressDialog.show(MyFavActivity.this, "删除收藏", "正在删除收藏中，请稍候", true, false);
				TextView tvId=(TextView)v.findViewById(R.id.recommend_text_id);
				int contentId=Integer.valueOf(tvId.getText().toString());
				FavList.EnumContentType contentType=FavList.EnumContentType.Blog;
				
				try{
					FavListHelper.RemoveFav(contentId,contentType,getApplicationContext());
					Toast.makeText(getApplicationContext(), R.string.unfav_succ, Toast.LENGTH_SHORT).show();
				}catch(Exception ex){
					Toast.makeText(getApplicationContext(), R.string.unfav_fail, Toast.LENGTH_SHORT).show();
				}
				progressDialog.dismiss();
				// 广播
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("contentId",contentId);
				bundle.putInt("contentType", contentType.ordinal());
				bundle.putBoolean("isfav", false);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_favlist");
				sendBroadcast(intent);
		}
		return super.onContextItemSelected(item);
	}
	/**
	 * 多线程启动（用于上拉加载、初始化、下载加载、刷新）
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Blog>> {
		boolean isRefresh = false;
		int curPageIndex = 0;
		public PageTask(int page, boolean isRefresh) {
			curPageIndex = page;
			this.isRefresh = isRefresh;
		}

		protected List<Blog> doInBackground(String... params) {
			boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());

			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}
			FavList.EnumContentType contentType=FavList.EnumContentType.Blog;
			if (isNetworkAvailable) {// 有网络情况				
				List<Blog> listBlogTmp=new ArrayList<Blog>();
				List<FavList> listFavNew = FavListHelper.GetFavListByPage(_pageIndex, contentType, getApplicationContext());
				switch (curPageIndex) {
					case -1 :// 上拉\
						List<FavList> listTmp = new ArrayList<FavList>();
						if (listBlog != null && listBlog.size() > 0) {// 避免首页无数据时
							if (listFavNew != null && listFavNew.size() > 0) {
								int size = listFavNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listFavNew.get(i))) {// 避免出现重复
										listTmp.add(listFavNew.get(i));
									}
								}
							}
						}
						listFav =listTmp;
					case 0 :// 首次加载
					case 1 :// 刷新
						if (listFavNew != null && listFavNew.size() > 0) {
							listFav= listFavNew;
						}
						break;
					default :// 下拉
						List<FavList> listT = new ArrayList<FavList>();
						if (listBlog != null && listBlog.size() > 0) {// 避免首页无数据时
							if (listFavNew != null && listFavNew.size() > 0) {
								int size = listFavNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listFavNew.get(i))) {// 避免出现重复
										listT.add(listFavNew.get(i));
									}
								}
							}
						}
						listFav= listT;
				}

				for(int i=0,len=listFav.size();i<len;i++){
					BlogDalHelper helper=new BlogDalHelper(getApplicationContext());
					Blog entity=helper.GetBlogEntity(listFav.get(i).GetContentId());
					listBlogTmp.add(entity);
				}
				return listBlogTmp;
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
		protected void onPostExecute(List<Blog> result) {
			// 右上角
			blog_progress_bar.setVisibility(View.GONE);
			blog_refresh_btn.setVisibility(View.VISIBLE);

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

			if (curPageIndex == -1) {// 上拉刷新
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// 首次加载
				listBlog = result;// dbHelper.GetTopBlogList();

				blogBody_progressBar.setVisibility(View.GONE);
				adapter = new MyFavListAdapter(getApplicationContext(),listBlog);
				listView.setAdapter(adapter);

				// 传递参数
				((PullToRefreshListView) listView).SetDataRow(listBlog.size());
				((PullToRefreshListView) listView).SetPageSize(Config.BLOG_PAGE_SIZE);
			} else if (curPageIndex == 1) {// 刷新
				try {// 避免首页无网络加载，按刷新按钮
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
						adapter.AddMoreData(result);
					} else if (result != null) {
						adapter = new MyFavListAdapter(getApplicationContext(),result);
						listView.setAdapter(adapter);
					}
					blogBody_progressBar.setVisibility(View.GONE);
				} catch (Exception ex) {
					// Log.e("BlogActivity", ex.getMessage());
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
				blogBody_progressBar.setVisibility(View.VISIBLE);
			}
			// 右上角
			blog_progress_bar.setVisibility(View.VISIBLE);
			blog_refresh_btn.setVisibility(View.GONE);

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
	/**
	 * 添加删除数据（广播）
	 * @author Administrator
	 *
	 */
	public class UpdateListViewReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context content, Intent intent) {
			Bundle bundle=intent.getExtras();
			int contentId=bundle.getInt("contentId");
			FavList.EnumContentType contentType=FavList.EnumContentType.values()[bundle.getInt("contentType",0)];
			try{
				boolean isFav=bundle.getBoolean("isfav");
				
				Object obj=FavListHelper.GetFavRefEntity(contentId, contentType, getApplicationContext());

				if(isFav){
					List<Blog> list=new ArrayList<Blog>();
					list.add((Blog)obj);
					adapter.AddMoreData(list);
				}else{
					adapter.RemoveData((Blog)obj);
				}
			}catch(Exception ex){
				Log.e("favActivity", ex.getMessage());
			}
		}		
	}
}
