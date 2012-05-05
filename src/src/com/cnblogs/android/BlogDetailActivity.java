package com.cnblogs.android;

import java.io.InputStream;

import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.FavListHelper;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.entity.FavList;
import com.cnblogs.android.enums.EnumResultType;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.NetHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.OnGestureListener;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 博客详细内容
 * @author walkingp
 * @date:2011-12
 *
 */
public class BlogDetailActivity extends BaseActivity
		implements
			OnGestureListener {
	private int blogId;// 博客编号
	private String blogTitle;// 标题
	private String blogAuthor;// 作者
	private String blogDate;// 发表时间
	private String blogUrl;// 文章链接
	private int blogViewCount;// 浏览次数
	private int blogCommentCount;// 评论次数

	static final int MENU_FORMAT_HTML = Menu.FIRST;// 格式化阅读
	static final int MENU_READ_MODE = Menu.FIRST + 1;// 切换阅读模式

	final String mimeType = "text/html";
	final String encoding = "utf-8";

	private Button comment_btn;// 评论按钮
	private Button blog_button_back;// 返回
	WebView webView;
	ProgressBar blogBody_progressBar;
	RelativeLayout rl_blog_detail;// 头部导航

	boolean isFullScreen = false;// 是否全屏

	private GestureDetector gestureScanner;// 手势

	Resources res;// 资源
	SharedPreferences sharePreferencesSettings;// 设置
	TextView tvSeekBar;// SeekBar显示文本框
	SeekBar seekBar;// SeekBar
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 防止休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.blog_detail);

		res = this.getResources();
		sharePreferencesSettings = getSharedPreferences(
				res.getString(R.string.preferences_key), MODE_PRIVATE);

		InitialData();
	}
	/**
	 * 操作数据库
	 */
	private void MarkAsReaded() {
		// 更新为已读
		BlogDalHelper helper = new BlogDalHelper(getApplicationContext());
		helper.MarkAsReaded(blogId);
		// 广播
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putIntArray("blogIdArray", new int[]{blogId});
		intent.putExtras(bundle);
		intent.setAction("android.cnblogs.com.update_bloglist");
		this.sendBroadcast(intent);
	}
	/**
	 * 初始化
	 */
	private void InitialData() {
		// 传递过来的值
		blogId = getIntent().getIntExtra("blogId", 0);
		blogTitle = getIntent().getStringExtra("blogTitle");
		blogAuthor = getIntent().getStringExtra("author");
		blogDate = getIntent().getStringExtra("date");
		blogUrl = getIntent().getStringExtra("blogUrl");
		blogViewCount = getIntent().getIntExtra("view", 0);
		blogCommentCount = getIntent().getIntExtra("comment", 0);
		// 头部
		rl_blog_detail = (RelativeLayout) findViewById(R.id.rl_blog_detail);
		// 双击全屏
		rl_blog_detail.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureScanner.onTouchEvent(event);
			}
		});
		// 打开评论
		comment_btn = (Button) findViewById(R.id.blog_comment_btn);
		String commentsCountString = (blogCommentCount == 0)
				? "暂无评论"
				: blogCommentCount + "条评论";
		comment_btn.setText(commentsCountString);
		comment_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				RedirectCommentActivity();
			}
		});
		// 返回
		blog_button_back = (Button) findViewById(R.id.blog_button_back);
		blog_button_back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				BlogDetailActivity.this.finish();
			}
		});
		try {
			webView = (WebView) findViewById(R.id.blog_body_webview_content);
			webView.getSettings().setDefaultTextEncodingName("utf-8");// 避免中文乱码
			webView.addJavascriptInterface(this, "javatojs");
			webView.setSelected(true);
			webView.setScrollBarStyle(0);
			WebSettings webSetting = webView.getSettings();
			webSetting.setJavaScriptEnabled(true);
			webSetting.setPluginsEnabled(true);
			webSetting.setNeedInitialFocus(false);
			webSetting.setSupportZoom(true);

			webSetting.setDefaultFontSize(14);
			webSetting.setCacheMode(WebSettings.LOAD_DEFAULT
					| WebSettings.LOAD_CACHE_ELSE_NETWORK);
			// 双击全屏
			webView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureScanner.onTouchEvent(event);
				}
			});
			int scalePercent = 110;
			// 上一次保存的缩放比例
			float webviewScale = sharePreferencesSettings.getFloat(
					res.getString(R.string.preferences_webview_zoom_scale),
					(float) 1.1);
			scalePercent = (int) (webviewScale * 100);
			webView.setInitialScale(scalePercent);

			blogBody_progressBar = (ProgressBar) findViewById(R.id.blogBody_progressBar);

			// 上一次全屏保存状态
			isFullScreen = sharePreferencesSettings.getBoolean(
					res.getString(R.string.preferences_is_fullscreen), false);
			// 初始是否全屏
			if (isFullScreen) {
				setFullScreen();
			}
			String url = Config.URL_GET_BLOG_DETAIL.replace("{0}",
					String.valueOf(blogId));// 网址
			PageTask task = new PageTask();
			task.execute(url);
		} catch (Exception ex) {
			Toast.makeText(getApplicationContext(), R.string.sys_error,
					Toast.LENGTH_SHORT).show();
		}

		// 监听屏幕动作事件 全屏
		gestureScanner = new GestureDetector(this);
		gestureScanner.setIsLongpressEnabled(true);
		gestureScanner
				.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
					public boolean onDoubleTap(MotionEvent e) {
						if (!isFullScreen) {
							setFullScreen();
						} else {
							quitFullScreen();
						}
						isFullScreen = !isFullScreen;
						// 保存配置
						sharePreferencesSettings
								.edit()
								.putBoolean(
										res.getString(R.string.preferences_is_fullscreen),
										isFullScreen).commit();
						return false;
					}
					public boolean onDoubleTapEvent(MotionEvent e) {
						return false;
					}
					public boolean onSingleTapConfirmed(MotionEvent e) {
						return false;
					}
				});
	}
	// 长按菜单
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.blog_body_webview_content) {
			menu.setHeaderTitle("请选择操作");
			menu.add(0, MENU_FORMAT_HTML, 0, "查看内容");
			menu.add(0, MENU_READ_MODE, 1, "切换到模式");
		}
	}
	/**
	 * 保存缩放比例
	 */
	public void onDestroy() {
		float webviewScale = webView.getScale();
		sharePreferencesSettings
				.edit()
				.putFloat(
						res.getString(R.string.preferences_webview_zoom_scale),
						webviewScale).commit();
		super.onDestroy();
	}
	/**
	 * 打开评论
	 * 
	 */
	private void RedirectCommentActivity() {
		// 还没有评论
		if (blogCommentCount == 0) {
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(BlogDetailActivity.this, CommentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("contentId", blogId);
		bundle.putInt("commentType", 0);// Comment.EnumCommentType.News.ordinal());
		bundle.putString("title", blogTitle);
		bundle.putString("url", blogUrl);

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * 跳转到博主
	 */
	private void RedirectAuthorActivity() {
		String userName = UserHelper.GetBlogUrlName(blogUrl);// 主页用户名
		if (userName.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.sys_no_author,
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		intent.setClass(BlogDetailActivity.this, AuthorBlogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("author", userName);// 用户名
		bundle.putString("blogName", blogAuthor);// 博客标题

		intent.putExtras(bundle);

		startActivityForResult(intent, 0);
	}
	/**
	 * 多线程启动
	 * 
	 * @author walkingp
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, String> {
		// 可变长的输入参数，与AsyncTask.exucute()对应
		@Override
		protected String doInBackground(String... params) {

			try {
				String _blogContent = BlogHelper.GetBlogById(blogId,getApplicationContext());
				//下载图片（只有本地完整保存图片时才下载）
				/*Context context=getApplicationContext();
				BlogDalHelper helper = new BlogDalHelper(context);				
				Blog entity = helper.GetBlogEntity(blogId);				
				boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
				if(isNetworkAvailable && (entity==null || !entity.GetIsFullText())){
					ImageCacher imageCacher=new ImageCacher(getApplicationContext());
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.Blog, _blogContent);
	
					_blogContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.Blog, _blogContent);
				}*/
				return _blogContent;
			} catch (Exception e) {
				e.printStackTrace();
			}

			return "";
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}
		/**
		 * 加载内容
		 */
		@Override
		protected void onPostExecute(String _blogContent) {
			String htmlContent = "";
			try {
				InputStream in = getAssets().open("NewsDetail.html");
				byte[] temp = NetHelper.readInputStream(in);
				htmlContent = new String(temp);
			} catch (Exception e) {
				Log.e("error", e.toString());
			}

			String blogInfo = "作者: " + blogAuthor + "   发表时间:" + blogDate
					+ "  查看:" + blogViewCount;
			// 格式化html
			_blogContent = AppUtil.FormatContent(getApplicationContext(),
					_blogContent);

			htmlContent = htmlContent.replace("#title#", blogTitle)
					.replace("#time#", blogInfo)
					.replace("#content#", _blogContent);
			LoadWebViewContent(webView, htmlContent);
			blogBody_progressBar.setVisibility(View.GONE);
			if(!_blogContent.equals("")){
				//更新为已读
				MarkAsReaded();
			}
		}

		@Override
		protected void onPreExecute() {
			blogBody_progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
	/**
	 * 加载内容
	 * 
	 * @param webView
	 * @param content
	 */
	private void LoadWebViewContent(WebView webView, String content) {
		webView.loadDataWithBaseURL(Config.LOCAL_PATH, content, "text/html",
				Config.ENCODE_TYPE, null);
	}
	/**
	 * 菜单
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.blog_detail_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	/**
	 * 全屏
	 */
	private void setFullScreen() {
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// 隐藏导航
		rl_blog_detail.setVisibility(View.GONE);
	}
	/**
	 * 退出全屏
	 */
	private void quitFullScreen() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setAttributes(attrs);
		getWindow()
				.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		// 显示导航
		rl_blog_detail.setVisibility(View.VISIBLE);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_blog_back :// 返回列表
				BlogDetailActivity.this.setResult(0, getIntent());
				BlogDetailActivity.this.finish();
				break;
			case R.id.menu_blog_comment :// 查看评论
				RedirectCommentActivity();
				break;
			case R.id.menu_blog_share :// 分享
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_SUBJECT, blogTitle);
				String shareContent = "《" + blogTitle + "》,作者：" + blogAuthor
						+ "，原文链接：" + blogUrl + " 分享自："
						+ res.getString(R.string.app_name) + "Android客户端("
						+ res.getString(R.string.app_homepage) + ")";
				intent.putExtra(Intent.EXTRA_TEXT, shareContent);
				startActivity(Intent.createChooser(intent, blogTitle));
				break;
			case R.id.menu_blog_add_fav:// 添加收藏
				new AddFavTask().execute(blogId);
				break;
			case R.id.menu_blog_author :// 博主
				RedirectAuthorActivity();
				break;
			case R.id.menu_blog_browser :// 查看网页
				Uri blogUri = Uri.parse(blogUrl);
				Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
				startActivity(it);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	/**
	 * 添加收藏
	 *
	 */
	public class AddFavTask extends AsyncTask<Integer,String,EnumResultType.EnumActionResultType>{
		int contentId;
		@Override
		protected EnumResultType.EnumActionResultType doInBackground(Integer... params) {
			contentId=params[0];
			EnumResultType.EnumActionResultType result= FavListHelper.AddFav(contentId, FavList.EnumContentType.Blog, getApplicationContext());
			return result;
		}
		@Override
		protected void onPostExecute(EnumResultType.EnumActionResultType result) {
			if(result.equals(EnumResultType.EnumActionResultType.Succ)){//成功
				// 广播
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("contentId",contentId);
				bundle.putInt("contentType", FavList.EnumContentType.Blog.ordinal());
				bundle.putBoolean("isfav", true);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_favlist");
				sendBroadcast(intent);
				Toast.makeText(getApplicationContext(), R.string.fav_succ, Toast.LENGTH_SHORT).show();
			}else if(result.equals(EnumResultType.EnumActionResultType.Exist)){
				Toast.makeText(getApplicationContext(), R.string.sys_fav_exist, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(), R.string.fav_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {

	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
}
