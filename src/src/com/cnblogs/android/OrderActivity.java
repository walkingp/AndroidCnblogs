package com.cnblogs.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cnblogs.android.enums.EnumActivityType;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
/**
 * 排行列表
 * @author walkingp
 * @date:2012-3
 *
 */
public class OrderActivity extends BaseActivity{
	Resources res;
	ListView listview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.order_layout);
        res=this.getResources();
       
        InitialControls();
	}
	/*
	 * 初始化控件
	 */
	void InitialControls(){
		Button btnBack=(Button)findViewById(R.id.btn_back);
		btnBack.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}			
		});
		SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.order_list_item, 
        		new String[]{"PIC", "TITLE","DESC","TAG"},
        		new int[]{R.id.more_tools_icon, R.id.more_tools_title,R.id.more_tools_desc,R.id.more_tools_url});
        	//使之可以加载图片
			adapter.setViewBinder(new ViewBinder(){
				public boolean setViewValue(View view,Object data,String textRepresentation){
					if(view instanceof ImageView && data instanceof Bitmap){
						ImageView iv=(ImageView)view;
						iv.setImageBitmap((Bitmap)data);
						return true;
					}
					return false;
				}
			});
        listview = (ListView)findViewById(R.id.order_list);
        listview.setAdapter(adapter);
        // 点击跳转
        listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,int position, long id) {
				TextView tvTag=(TextView)v.findViewById(R.id.more_tools_url);
				
				int type=Integer.parseInt(tvTag.getText().toString());
				EnumActivityType.EnumOrderActivityType activityType=EnumActivityType.EnumOrderActivityType.values()[type];
				
				Intent intent=new Intent();
				Bundle bundle=new Bundle();
				bundle.putInt("type", type);
				switch(activityType){
					case RecommendBlog://博客排行
						intent.setClass(getApplicationContext(), AuthorOrderActivity.class);						
						break;
					case TopViewBlogIn48Hours://48小时内阅读排行
					case TopDiggBlogIn10Days://10天内推荐排行
						intent.setClass(getApplicationContext(), BlogTopViewDiggActivity.class);
						break;
					case RecommendNews://推荐新闻
						intent.setClass(getApplicationContext(), NewsRecommendActivity.class);
						break;
				}
				intent.putExtras(bundle);						
				startActivity(intent);
			}
        });
	}
	/*
	 * 数据源
	 */
	private List<Map<String, Object>> getData() {
    	Integer[] images = { R.drawable.myspace, R.drawable.ember,R.drawable.digg_this,R.drawable.geotag};
		String[] texts = { "推荐博客排名","48小时阅读排行","10天内推荐排行","推荐新闻"};
		String[] descs={"按园友推荐的次数进行排名，均是园子里较有影响力的作者。",
				"在过去48小时内被阅读次数最多的博客排行。",
				"10天内被园友推荐次数最多的博客排行，代表了近期园子里最受欢迎的博客。",
				"博客园新闻编辑人工精选，近期最值得阅读的新闻资讯。"};		
		EnumActivityType.EnumOrderActivityType[] tags={EnumActivityType.EnumOrderActivityType.RecommendBlog,
				EnumActivityType.EnumOrderActivityType.TopViewBlogIn48Hours,
				EnumActivityType.EnumOrderActivityType.TopDiggBlogIn10Days,
				EnumActivityType.EnumOrderActivityType.RecommendNews};
    	List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < texts.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("PIC", images[i]);
			map.put("TITLE", texts[i]);
			map.put("DESC", descs[i]);
			map.put("TAG", tags[i].ordinal());
			
			items.add(map);
		}
        return items;
    }
}
