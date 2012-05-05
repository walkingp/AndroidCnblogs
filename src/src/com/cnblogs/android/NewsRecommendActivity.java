package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
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

import com.cnblogs.android.adapter.NewsListAdapter;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.NetHelper;
/**
 * 编辑推荐新闻
 * @author walkingp
 * @date:2012-3
 *
 */
public class NewsRecommendActivity extends BaseActivity {
	List<News> listNews = new ArrayList<News>();

	NewsListAdapter adapter;

	int pageIndex = 1;// 页码

	ListView listView;
	TextView txtAppTitle;

	private ImageButton refresh_btn; // 头部刷新按钮
	ProgressBar news_progress_bar; // 头部加载按钮

	ProgressBar newsBody_progressBar;// 主题进度
	Button btnBack;

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
		this.setContentView(R.layout.author_order_recommend_news_layout);

		res = this.getResources();
		InitialControls();
		InitialNewsList();
		BindEvent();
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls() {
		txtAppTitle=(TextView)findViewById(R.id.txtAppTitle);
		txtAppTitle.setText("推荐新闻");
		btnBack=(Button)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
		listView = (ListView) findViewById(R.id.blog_list);
		listView.removeAllViewsInLayout();
		newsBody_progressBar = (ProgressBar) findViewById(R.id.blogList_progressBar);
		newsBody_progressBar.setVisibility(View.VISIBLE);
		// 刷新
		refresh_btn = (ImageButton) findViewById(R.id.blog_refresh_btn);
		news_progress_bar = (ProgressBar) findViewById(R.id.blog_progressBar);
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
			boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
			int _pageIndex = curPageIndex;
			if (_pageIndex <= 0) {
				_pageIndex = 1;
			}

			if (isNetworkAvailable) {// 有网络情况
				List<News> listNewsNew = NewsHelper.GetRecommendNewsList(_pageIndex);
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
				if (!NetHelper.networkIsAvailable(getApplicationContext()) && curPageIndex > 1) {// 下拉并且没有网络
					Toast.makeText(getApplicationContext(),R.string.sys_network_error, Toast.LENGTH_SHORT)
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
	/**
	 * 查看详情
	 * 
	 * @param v
	 */
	private void RedirectDetailActivity(View v) {
		Intent intent = new Intent();
		try {
			// 传递参数
			intent.setClass(NewsRecommendActivity.this, NewsDetailActivity.class);
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
}
