package com.cnblogs.android;

import com.cnblogs.android.utility.AppUtil;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * 此页包含新浪微博加关注功能
 * @author walkingp
 * @date:2011-12
 *
 */
public class AboutActivity extends BaseActivity{
	
	Button btnWeibo;//关注
	SharedPreferences sharePreferences;//设置
	String CONFIG_CURRENT_WEIBO_USER_TOKEN="config_current_weibo_user_token";//当前微博用户key
	Resources res;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//隐藏标题栏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
				WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		this.setContentView(R.layout.about_layout);
				
		sharePreferences = getSharedPreferences(CONFIG_CURRENT_WEIBO_USER_TOKEN, MODE_PRIVATE);
		res=this.getResources();
		InitialControl();
	}
	/**
	 * 初始化控件
	 */
	private void InitialControl(){
		/*View layout = getLayoutInflater().inflate(R.layout.about_layout, null); 
		RelativeLayout body=(RelativeLayout)layout.findViewById(R.id.linearAbout);
		body.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				AboutActivity.this.finish();
			}
		});*/
		btnWeibo=(Button)findViewById(R.id.about_weibo_btn);
		btnWeibo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				GotoMarket();
			}
		});
		//当前版本
		TextView txtAppVersion=(TextView)findViewById(R.id.txtAppVersion);
		String versionName=AppUtil.GetVersionName(getApplicationContext());
		txtAppVersion.setText(versionName);
		//链接
		TextView txtAppAuthor=(TextView)findViewById(R.id.txtAppAuthor);
		txtAppAuthor.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				RedirectToAuthor();
			}			
		});

		String currentUserToken=sharePreferences.getString(CONFIG_CURRENT_WEIBO_USER_TOKEN, "");
		if(!currentUserToken.equalsIgnoreCase("")){
			//btnWeibo.setText("已经关注作者");
		}
	}
	/**
	 * 启动电子市场
	 */
	private void GotoMarket(){
		Uri blogUri=Uri.parse(res.getString(R.string.app_market_url));
    	Intent it = new Intent(Intent.ACTION_VIEW, blogUri);
    	startActivity(it);
	}
	/**
	 * 跳转到个人主页 
	 */
	private void RedirectToAuthor(){
		//传递参数
		Intent intent = new Intent();
		intent.setClass(AboutActivity.this,AuthorBlogActivity.class);
		Bundle bundle=new Bundle();
		bundle.putString("blogName", res.getString(R.string.app_author_cnblogs_title));
		bundle.putString("author",res.getString(R.string.app_author));
		
		intent.putExtras(bundle);
		
		startActivity(intent);
		finish();
	}
	@Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
	}
}
