package com.cnblogs.android;

import java.io.InputStream;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.dal.NewsDalHelper;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * 新闻详情
 * @author walkingp
 * @date 2011-12
 */
public class NewsDetailActivity extends BaseActivity implements OnGestureListener{
	private int newsId;//博客编号
	private String newsTitle;//标题
	private String newsDate;//发表时间
	private String newsUrl;//文章链接
	private int newsViews;//浏览次数
	private int newsComemnt;//评论次数
	
	static final int I_MENU_BACK=Menu.FIRST;//返回
	static final int I_MENU_REFRESH=Menu.FIRST+1;//刷新
	static final int I_MENU_COMMENT=Menu.FIRST+2;//查看评论	
	static final int I_MENU_VIEW_BROWSER=Menu.FIRST+3;//查看网页
	static final int I_MENU_SHARE=Menu.FIRST+4;//分享到	
	
	final String mimeType = "text/html";  
    final String encoding = "utf-8";  
    
    private Button comment_btn;//评论按钮
    private Button new_button_back;//返回
    
    boolean isFullScreen=false;//是否全屏
    
    WebView webView;
    ProgressBar newsBody_progressBar;
    RelativeLayout rl_news_detail;//头部导航
    
    private GestureDetector gestureScanner;//手势
    
    Resources res;//资源
    SharedPreferences sharePreferencesSettings;//设置
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		//防止休眠
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.news_detail);
		
		res = this.getResources();
		sharePreferencesSettings = getSharedPreferences(res.getString(R.string.preferences_key), MODE_PRIVATE);
		InitialData();
	}
	/**
	 * 操作数据库
	 */
	private void OperateDatabase(){
		//更新为已读
		NewsDalHelper helper=new NewsDalHelper(getApplicationContext());
		helper.MarkAsReaded(newsId);
		helper.Close();
		// 广播
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putIntArray("newsIdArray",new int[]{newsId});
		intent.putExtras(bundle);
		intent.setAction("android.cnblogs.com.update_newslist");
		this.sendBroadcast(intent);
	}
	/**
	 * 初始化
	 */
	private void InitialData(){
		newsId=Integer.parseInt(getIntent().getStringExtra("newsId"));
		newsTitle=getIntent().getStringExtra("newsTitle");
		newsDate=getIntent().getStringExtra("date");
		newsUrl=getIntent().getStringExtra("newsUrl");
		newsViews=getIntent().getIntExtra("view", 0);
		newsComemnt=getIntent().getIntExtra("comment", 0);
		
		//打开评论
		comment_btn = (Button)findViewById(R.id.news_comment_btn);
		String commentsCountString= (newsComemnt==0) ? "暂无" : newsComemnt +"条"; 
		comment_btn.setText(commentsCountString + "评论");
		comment_btn.setOnClickListener(new OnClickListener(){
		public void onClick(View v) {
			RedirectCommentActivity();
		}});
		//头部
		rl_news_detail=(RelativeLayout)findViewById(R.id.rl_news_detail);
		//双击全屏
		rl_news_detail.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureScanner.onTouchEvent(event);
			}	    		
    	});
		//返回
		new_button_back=(Button)findViewById(R.id.new_button_back);
		new_button_back.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				NewsDetailActivity.this.finish();
			}
		});		
		try{			
			webView=(WebView)findViewById(R.id.news_body_webview_content);
			webView.getSettings().setDefaultTextEncodingName("utf-8");//避免中文乱码
			webView.addJavascriptInterface(this, "javatojs");
			webView.setScrollBarStyle(0);
			WebSettings webSetting = webView.getSettings();
	    	webSetting.setJavaScriptEnabled(true);
	    	webSetting.setPluginsEnabled(true);
	    	webSetting.setNeedInitialFocus(false);
	    	webSetting.setSupportZoom(true);
	    	webSetting.setCacheMode(WebSettings.LOAD_DEFAULT|WebSettings.LOAD_CACHE_ELSE_NETWORK);
	    	//双击全屏
	    	webView.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return gestureScanner.onTouchEvent(event);
				}	    		
	    	});
	    	//上一次保存的缩放比例
	    	int scalePercent=110;
	    	float webviewScale=sharePreferencesSettings.getFloat(res.getString(R.string.preferences_webview_zoom_scale), (float) 1.1);
	    	scalePercent=(int)(webviewScale*100);
	    	webView.setInitialScale(scalePercent);
	    	
			newsBody_progressBar=(ProgressBar)findViewById(R.id.newsBody_progressBar);
			
			String url=Config.URL_GET_BLOG_DETAIL.replace("{0}",String.valueOf(newsId));//网址
			PageTask task = new PageTask();
	        task.execute(url);
		}catch(Exception ex){
			Log.e("NewsDetail","+++++++++++++++++加载数据时出错++++++++++++++");
			Toast.makeText(getApplicationContext(), R.string.sys_error,Toast.LENGTH_SHORT).show();
		}
		
		// 监听屏幕动作事件  
		gestureScanner = new GestureDetector(this);   
	    gestureScanner.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener(){  
			public boolean onDoubleTap(MotionEvent e) {  
			if(!isFullScreen){
				setFullScreen();
			}else{
				quitFullScreen();
			}
			isFullScreen=!isFullScreen; 
			//保存配置
			sharePreferencesSettings.edit().putBoolean(res.getString(R.string.preferences_is_fullscreen), isFullScreen)
				.commit();
	        return false;  
	      }
	      public boolean onDoubleTapEvent(MotionEvent e) {
	        return false;
	      }  
	      public boolean onSingleTapConfirmed(MotionEvent e) {
	        return false;  
	      }  
	    }); 
	    //上一次全屏保存状态		
		isFullScreen=sharePreferencesSettings.getBoolean(res.getString(R.string.preferences_is_fullscreen), false);
		//初始是否全屏
		if(isFullScreen){
			setFullScreen();
		}
	}
	/**
	 * 保存缩放比例
	 */
	public void onDestroy(){
		float webviewScale=webView.getScale();
		sharePreferencesSettings.edit().putFloat(res.getString(R.string.preferences_webview_zoom_scale), webviewScale)
		.commit();
		super.onDestroy(); 
	}
	/**
	 * 打开评论
	 */
	private void RedirectCommentActivity(){
		//还没有评论
		if(newsComemnt==0){
			Toast.makeText(getApplicationContext(), R.string.sys_empty_comment, Toast.LENGTH_SHORT).show();
			return;
		}
		
		Intent intent = new Intent();
		intent.setClass(NewsDetailActivity.this,CommentActivity.class);
		Bundle bundle=new Bundle();
		bundle.putInt("contentId", newsId);
		bundle.putInt("commentType",1);//Comment.EnumCommentType.News.ordinal());
		bundle.putString("title",newsTitle);
		bundle.putString("url",newsUrl);
		
		intent.putExtras(bundle);
		
		startActivityForResult(intent, 0);
	}
	/**
	 * 多线程启动
	 * @author walkingp
	 *
	 */
    public class PageTask extends AsyncTask<String, Integer, String> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected String doInBackground(String... params) {

            try{
            	String _newsContent=NewsHelper.GetNewsContentById(newsId, getApplicationContext());
				//下载图片（只有本地完整保存图片时才下载）
            	//NewsDalHelper helper = new NewsDalHelper(context);
            	//Context context=getApplicationContext();
            	//News entity = helper.GetNewsEntity(newsId);
				/*boolean isNetworkAvailable = NetHelper.networkIsAvailable(getApplicationContext());
				if(entity==null || !entity.GetIsFullText()){
					ImageCacher imageCacher=new ImageCacher(getApplicationContext());
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.News, _newsContent);
	            	_newsContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.News, _newsContent);
				}*/
            	return _newsContent;
            } catch(Exception e) {
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
        protected void onPostExecute(String _newsContent) {
        	String htmlContent="";
			try{
				InputStream in = getAssets().open("NewsDetail.html");
				byte[] temp = NetHelper.readInputStream(in);
				htmlContent = new String(temp);
			}
			catch(Exception e)
			{
				Log.e("error", e.toString());
			}
			
			//阅读模式
			_newsContent=AppUtil.FormatContent(getApplicationContext(), _newsContent);
			
			String newsInfo= "发表时间:" + newsDate + " 查看:" + newsViews;
			
        	webView.loadDataWithBaseURL(Config.LOCAL_PATH, htmlContent.replace("#title#",newsTitle).replace("#time#", newsInfo)
					.replace("#content#", _newsContent), "text/html", "utf-8", null);
        	newsBody_progressBar.setVisibility(View.GONE);

        	if(!_newsContent.equals("")){
	        	//更新为已读
	    		OperateDatabase();
        	}
        }

        @Override
        protected void onPreExecute() {
			newsBody_progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
        }
     }

	/**
	 * 菜单
	 */
	@Override  
    public boolean onCreateOptionsMenu(Menu menu) { 
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.news_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);         
    }
   @Override  
    public boolean onOptionsItemSelected(MenuItem item) { 
        switch(item.getItemId()){  
            case R.id.menu_news_back://返回列表
            	NewsDetailActivity.this.setResult(0,getIntent());
            	NewsDetailActivity.this.finish();
				break;
            case R.id.menu_news_comment://打开评论
            	RedirectCommentActivity();
            	break;
            case R.id.menu_news_share://分享
            	Intent intent=new Intent(Intent.ACTION_SEND);    
            	intent.setType("text/plain");
            	intent.putExtra(Intent.EXTRA_SUBJECT, newsTitle);
            	String shareContent="《" + newsTitle + "》,原文链接：" + newsUrl + " 分享自：" + res.getString(R.string.app_name)
            			+ "Android客户端(" + res.getString(R.string.app_homepage) + ")";
            	intent.putExtra(Intent.EXTRA_TEXT, shareContent);
            	startActivity(Intent.createChooser(intent, newsTitle)); 
            	break;
            case R.id.menu_news_refresh://刷新
            	InitialData();
            	break;
            case R.id.menu_news_fontsize://字体大小
            	InitialData();
            	break;
            case R.id.menu_news_browser://查看网页
    	    	Uri newsUri=Uri.parse(newsUrl);
    	    	Intent it = new Intent(Intent.ACTION_VIEW, newsUri);
    	    	startActivity(it);
                break;
        }  
        return super.onOptionsItemSelected(item);  
    } 
	/**
	 * 双击全屏
	 */
	public void OnDoubleTapListener(){		
		if(!isFullScreen){
			setFullScreen();
		}else{
			quitFullScreen();
		}
		isFullScreen=!isFullScreen;		
	}
	/**
	 * 全屏
	 */
	private void setFullScreen(){
       getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
       		WindowManager.LayoutParams.FLAG_FULLSCREEN);
       //隐藏导航
       rl_news_detail.setVisibility(View.GONE);
   }
	/**
	 * 退出全屏
	 */
   private void quitFullScreen(){
       final WindowManager.LayoutParams attrs = getWindow().getAttributes();
       attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
       getWindow().setAttributes(attrs);
       getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
       //显示导航
       rl_news_detail.setVisibility(View.VISIBLE);
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
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
 }
