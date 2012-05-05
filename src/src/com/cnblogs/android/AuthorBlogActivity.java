package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.cnblogs.android.adapter.AuthorBlogListAdapter;
import com.cnblogs.android.cache.AsyncImageLoader;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.cache.AsyncImageLoader.ImageCallback;
import com.cnblogs.android.controls.PullToRefreshListView;
import com.cnblogs.android.controls.PullToRefreshListView.OnRefreshListener;
import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.dal.RssListDalHelper;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.entity.RssList;
import com.cnblogs.android.entity.Users;
import com.cnblogs.android.services.DownloadServices;
import com.cnblogs.android.utility.NetHelper;
/**
 * 此页包含作者的博客页
 * @author walkingp
 * @date:2011-11
 *
 */
public class AuthorBlogActivity extends BaseActivity{
	List<Blog> listBlog = new ArrayList<Blog>();
	private AsyncImageLoader asyncImageLoader;
	AuthorBlogListAdapter adapter;
	
	int pageIndex=1;//页码
	
	ListView listView;
	
	ProgressBar blogBody_progressBar;//加载
	ImageButton blog_refresh_btn;//刷新按钮
	private Button blog_button_back;//返回
	ProgressBar blog_progress_bar;//加载按钮
	
	private LinearLayout viewFooter;//footer view
	TextView tvFooterMore;//底部更多显示
	ProgressBar list_footer_progress;//底部进度条
	
	private String author;//博主用户名
	private String blogName;//博客名
	private int blogCount;//博客数量 

	Button btn_rss;//订阅按钮
	private int lastItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.author_blog_layout);
		 
		InitialControls();
		BindEvent();		
		
		new PageTask(0,true).execute();
	}
	/**
	 * 绑定事件
	 */
	private void BindEvent(){
		//刷新
		blog_refresh_btn.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				new PageTask(1,true).execute();
			}
		});
		//上拉刷新
		((PullToRefreshListView) listView).setOnRefreshListener(new OnRefreshListener() {
			@Override
            public void onRefresh() {
				new PageTask(-1,true).execute();
            }
		});
		//下拉刷新
		listView.setOnScrollListener(new OnScrollListener() {
			/**
			 * 下拉到最后一行
			 */
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (lastItem == adapter.getCount() && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					pageIndex=pageIndex+1;
					new PageTask(pageIndex,false).execute();
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

				Intent intent = new Intent();
				try{
					//传递参数
					intent.setClass(AuthorBlogActivity.this,BlogDetailActivity.class);
					Bundle bundle=new Bundle();
					TextView tvBlogId=(TextView)(v.findViewById(R.id.recommend_text_id));
					TextView tvBlogTitle=(TextView)(v.findViewById(R.id.recommend_text_title));
					TextView tvBlogAuthor=(TextView)(v.findViewById(R.id.recommend_text_author));
					TextView tvBlogDate=(TextView)(v.findViewById(R.id.recommend_text_date));
					TextView tvBlogUrl=(TextView)(v.findViewById(R.id.recommend_text_url));
					TextView tvBlogViewCount=(TextView)(v.findViewById(R.id.recommend_text_view));
					TextView tvBlogCommentCount=(TextView)(v.findViewById(R.id.recommend_text_comments));
					
					int blogId=Integer.parseInt(tvBlogId.getText().toString());
					String blogTitle=tvBlogTitle.getText().toString();
					String blogAuthor=tvBlogAuthor.getText().toString();
					String blogDate=tvBlogDate.getText().toString();
					String blogUrl=tvBlogUrl.getText().toString();
					int viewsCount=Integer.parseInt(tvBlogViewCount.getText().toString());
					int commentCount=Integer.parseInt(tvBlogCommentCount.getText().toString());
					
					bundle.putInt("blogId", blogId);
					bundle.putString("blogTitle", blogTitle);
					bundle.putString("author",blogAuthor );
					bundle.putString("date",blogDate);
					bundle.putString("blogUrl", blogUrl);
					bundle.putInt("view", viewsCount);
					bundle.putInt("comment", commentCount);
					
					Log.d("blogId", String.valueOf(blogId));
					intent.putExtras(bundle);
					
					startActivityForResult(intent, 0);
				}catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});		
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls(){
		//传递过来的值
		author=getIntent().getStringExtra("author");//博主
		blogName=getIntent().getStringExtra("blogName");//博客名
		
		listView = (ListView) findViewById(R.id.author_blog_list);
		blogBody_progressBar=(ProgressBar)findViewById(R.id.author_blogList_progressBar);
		blogBody_progressBar.setVisibility(View.VISIBLE);
		//刷新
		blog_refresh_btn=(ImageButton)findViewById(R.id.author_blog_refresh_btn);
		blog_progress_bar=(ProgressBar)findViewById(R.id.author_blog_progressBar);
		//返回
		blog_button_back=(Button)findViewById(R.id.author_blog_button_back);
		blog_button_back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				AuthorBlogActivity.this.finish();
			}
		});

		//底部view
		LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		viewFooter = (LinearLayout)mInflater.inflate(R.layout.listview_footer, null, false);
		
		//获得用户对象
		final Users entity=UserHelper.GetUserDetail(author);
		if(entity==null){
			Toast.makeText(getApplicationContext(), R.string.sys_no_author, Toast.LENGTH_SHORT).show();
			return;
		}
		//博主
		TextView txtAuthorName=(TextView)findViewById(R.id.author_name);
		txtAuthorName.setText(blogName);
		//博客地址
		TextView txtAuthorUrl=(TextView)findViewById(R.id.author_url);
		final String url=entity.GetBlogUrl();
		txtAuthorUrl.setText(url);
		txtAuthorUrl.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Uri blogUri=Uri.parse(url);
    	    	Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
    	    	startActivity(it);
			}			
		});
		//博文数量
		blogCount=entity.GetBlogCount();
		TextView txtBlogCount=(TextView)findViewById(R.id.author_blog_count);
		txtBlogCount.setText("(共有" + blogCount + "篇随笔)");
		//头像
		final ImageView imgAvatar=(ImageView)findViewById(R.id.author_image_icon);
		asyncImageLoader = new AsyncImageLoader(getApplicationContext());
		String tag = entity.GetAvator();
		if(tag!=null){
			if (tag.contains("?")) {// 截断?后的字符串，避免无效图片
				tag = tag.substring(0, tag.indexOf("?"));
			}
			Drawable cachedImage = asyncImageLoader.loadDrawable(
				ImageCacher.EnumImageType.Avatar, tag, new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable, String tag) {
						if (imageDrawable != null) {
							imgAvatar.setImageDrawable(imageDrawable);
						} else {
							try {
								imgAvatar.setImageResource(R.drawable.sample_face);
							} catch (Exception ex) {
		
							}
						}
					}
			});
			if (cachedImage != null) {
				imgAvatar.setImageDrawable(cachedImage);
			}
		}

		// 是否已经订阅
		btn_rss=(Button)findViewById(R.id.btn_rss);
		RssListDalHelper helper = new RssListDalHelper(this);
		final boolean isRssed = helper.ExistByAuthorName(author);
		
		btn_rss.setTag(isRssed);
		final Users userEntity=entity;
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 订阅 取消订阅
					String url = entity.GetBlogUrl();
					RssList entity = new RssList();
					// entity.SetAddTime(new java.util.Date());
					entity.SetAuthor(author);//注意此处是用户名，不是博客名
					entity.SetCateId(0);
					entity.SetCateName("");
					entity.SetDescription(userEntity.GetBlogUrl());
					entity.SetGuid(String.valueOf(userEntity.GetUserId()));
					entity.SetImage(userEntity.GetAvator());
					entity.SetIsActive(true);
					entity.SetIsCnblogs(true);
					entity.SetLink(url);
					entity.SetOrderNum(0);
					entity.SetTitle(blogName);
					
					// 广播
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putStringArray("rsslist", 
							new String[]{entity.GetAuthor(),entity.GetDescription(),entity.GetGuid(),
								entity.GetTitle(),entity.GetImage(),entity.GetLink(),
								entity.GetIsCnblogs() ? "1" : "0"
							});

					RssListDalHelper helper = new RssListDalHelper(getApplicationContext());

					boolean _isRssed =Boolean.parseBoolean(btn_rss.getTag().toString());
					if (_isRssed) {// 退订
						helper.Delete(entity.GetLink());

						btn_rss.setBackgroundResource(R.drawable.drawable_btn_rss);
						btn_rss.setText(R.string.btn_rss);
						btn_rss.setTextColor(R.color.gray);
						btn_rss.setTag(false);
						
						bundle.putBoolean("isrss", false);

						Toast.makeText(getApplicationContext(), "退订成功", Toast.LENGTH_SHORT)
								.show();
					} else {// 订阅
						helper.Insert(entity);

						btn_rss.setBackgroundResource(R.drawable.btn_rssed);
						btn_rss.setText(R.string.btn_unrss);
						btn_rss.setTextColor(R.color.darkblue);
						btn_rss.setTag(true);

						bundle.putBoolean("isrss", true);
						Toast.makeText(getApplicationContext(), "订阅成功", Toast.LENGTH_SHORT)
								.show();
					}
					// 发送广播
					intent.putExtras(bundle);
					intent.setAction("android.cnblogs.com.update_rsslist");
					AuthorBlogActivity.this.sendBroadcast(intent);
				}
		};
		btn_rss.setOnClickListener(listener);
		if (isRssed) {
			btn_rss.setBackgroundResource(R.drawable.btn_rssed);
			btn_rss.setText(R.string.btn_unrss);
			btn_rss.setTextColor(R.color.gray);
		}
	}
	/**
	 * 菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.author_blog_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.author_blog_offline://离线下载
				if (!NetHelper.networkIsAvailable(getApplicationContext())) {// 网络不可用
					Toast.makeText(getApplicationContext(),R.string.sys_network_error,Toast.LENGTH_SHORT).show();
					return false;
				}
				DownloadServices.EnumDataType dataType = DownloadServices.EnumDataType.AuthorBlog;
				Intent intent = new Intent(AuthorBlogActivity.this,DownloadServices.class);
				intent.putExtra("type", dataType.ordinal());
				intent.putExtra("author", author);
				intent.putExtra("size", blogCount);
				Toast.makeText(getApplicationContext(),R.string.offline_notification_start_toast,Toast.LENGTH_SHORT).show();
				startService(intent);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * 多线程启动（用于上拉加载、初始化、下载加载、刷新）
	 *
	 */
    public class PageTask extends AsyncTask<String, Integer, List<Blog>> {
    	boolean isRefresh=false;
    	int curPageIndex=0;
    	boolean isLocalData = false;// 是否是从本地读取的数据
		BlogDalHelper dbHelper = new BlogDalHelper(getApplicationContext());
        public PageTask(int page,boolean isRefresh)
        {
        	curPageIndex=page;
        	this.isRefresh=isRefresh;
        }
        
        protected List<Blog> doInBackground(String... params) {
        	boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
        	int _pageIndex=curPageIndex;
        	if(_pageIndex<=0){
        		_pageIndex=1;
        	}

			// 优先读取本地数据
			List<Blog> listBlogLocal = dbHelper.GetBlogListByAuthor(author,_pageIndex,Config.BLOG_PAGE_SIZE);
			if (isNetworkAvailable) {// 有网络情况
				List<Blog> listBlogNew = BlogHelper.GetAuthorBlogList(author, _pageIndex);
				switch (curPageIndex) {
					case -1 :// 上拉\
						List<Blog> listTmp = new ArrayList<Blog>();
						if (listBlog != null && listBlog.size() > 0) {// 避免首页无数据时
							if (listBlogNew != null && listBlogNew.size() > 0) {
								int size = listBlogNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listBlogNew.get(i))) {// 避免出现重复
										listTmp.add(listBlogNew.get(i));
									}
								}
							}
						}
						return listTmp;
					case 0 :// 首次加载
					case 1 :// 刷新
						if (listBlogNew != null && listBlogNew.size() > 0) {
							return listBlogNew;
						}
						break;
					default :// 下拉
						List<Blog> listT = new ArrayList<Blog>();
						if (listBlog != null && listBlog.size() > 0) {// 避免首页无数据时
							if (listBlogNew != null && listBlogNew.size() > 0) {
								int size = listBlogNew.size();
								for (int i = 0; i < size; i++) {
									if (!listBlog.contains(listBlogNew.get(i))) {// 避免出现重复
										listT.add(listBlogNew.get(i));
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
				return listBlogLocal;
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

			// 保存到数据库
			if (!isLocalData) {
				dbHelper.SynchronyData2DB(result);
			}

			if (curPageIndex == -1) {// 上拉刷新
				adapter.InsertData(result);
			} else if (curPageIndex == 0) {// 首次加载
				listBlog = result;// dbHelper.GetTopBlogList();

				blogBody_progressBar.setVisibility(View.GONE);
				adapter = new AuthorBlogListAdapter(getApplicationContext(),listBlog);
				listView.setAdapter(adapter);

				// 传递参数
				((PullToRefreshListView) listView).SetDataRow(listBlog.size());
				((PullToRefreshListView) listView)
						.SetPageSize(Config.BLOG_PAGE_SIZE);
			} else if (curPageIndex == 1) {// 刷新
				try {// 避免首页无网络加载，按刷新按钮
					if (adapter != null && adapter.GetData() != null) {
						adapter.GetData().clear();
						adapter.AddMoreData(result);
					} else if (result != null) {
						adapter = new AuthorBlogListAdapter(getApplicationContext(),result);
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
        	//主体进度条
    		if(listView.getCount()==0){
    			blogBody_progressBar.setVisibility(View.VISIBLE);
    		}
        	//右上角
    		blog_progress_bar.setVisibility(View.VISIBLE);
    		blog_refresh_btn.setVisibility(View.GONE);
    		
    		if(!isRefresh){//底部控件，刷新时不做处理
	    		TextView tvFooterMore=(TextView)findViewById(R.id.tvFooterMore);
	    		tvFooterMore.setText(R.string.pull_to_refresh_refreshing_label);
	    		tvFooterMore.setVisibility(View.VISIBLE);
	    		ProgressBar list_footer_progress=(ProgressBar)findViewById(R.id.list_footer_progress);
	    		list_footer_progress.setVisibility(View.VISIBLE);
    		}
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
     }
}
