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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.cnblogs.android.adapter.RssListAdapter;
import com.cnblogs.android.dal.RssListDalHelper;
import com.cnblogs.android.entity.RssList;
/**
 * 我的订阅
 * @author walkingp
 * @date:2012-3-22
 *
 */
public class MyRssActivity extends BaseMainActivity {
	List<RssList> listRss = new ArrayList<RssList>();

	int pageIndex = 1;// 页码

	ListView listView;
	private RssListAdapter adapter;// 数据源

	ProgressBar bodyProgressBar;// 主题ListView加载框
	ImageButton btnRefresh;// 刷新按钮
	ProgressBar topProgressBar;// 加载按钮

	TextView txtNoData;// 没有数据
	private ProgressDialog progressDialog;  
	int lastItemPosition;
	Resources res;// 资源
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.my_rss_layout);

		res = this.getResources();
		InitialControls();
		BindControls();
		new PageTask().execute();
		
		//注册广播
		UpdateListViewReceiver receiver=new UpdateListViewReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("android.cnblogs.com.update_rsslist");
		registerReceiver(receiver, filter);
	}
	/**
	 * 初始化列表
	 */
	private void InitialControls() {
		listView = (ListView) findViewById(R.id.rss_list);
		bodyProgressBar = (ProgressBar) findViewById(R.id.rssList_progressBar);

		btnRefresh = (ImageButton) findViewById(R.id.btn_refresh);
		topProgressBar = (ProgressBar) findViewById(R.id.rss_progressBar);
		txtNoData = (TextView) findViewById(R.id.txtNoData);
	}
	/**
	 * 绑定事件
	 */
	private void BindControls() {
		// 跳转到分类
		btnRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MyRssActivity.this, RssCateActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		// 点击跳转
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				TextView tvTitle = (TextView) v
						.findViewById(R.id.rss_item_title);
				TextView tvUrl = (TextView) v.findViewById(R.id.rss_item_url);
				TextView tvIsCnblogs = (TextView) v
						.findViewById(R.id.rss_item_is_cnblogs);
				TextView tvAuthor = (TextView) v
						.findViewById(R.id.rss_item_author);
				String title = tvTitle.getText().toString();
				String url = tvUrl.getText().toString();
				boolean isCnblogs = tvIsCnblogs.getText().toString()
						.equals("1");
				String author = tvAuthor.getText().toString();

				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				if (isCnblogs) {// 博客园
					intent.setClass(MyRssActivity.this,
							AuthorBlogActivity.class);
					bundle.putString("blogName", title);
					bundle.putString("author", author);
				} else {
					intent.setClass(MyRssActivity.this, RssItemsActivity.class);
					bundle.putString("title", title);
					bundle.putString("url", url);
				}
				lastItemPosition=listView.getSelectedItemPosition();
				intent.putExtras(bundle);

				startActivity(intent);
			}
		});
		// 长按事件
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.my_rss_contextmenu, menu);
				menu.setHeaderTitle(R.string.menu_bar_title);
			}
		});
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
	            progressDialog = ProgressDialog.show(MyRssActivity.this, "退订栏目", "正在处理退订中，请稍候", true, false);
				TextView tvLink=(TextView)v.findViewById(R.id.rss_item_url);
				String link=tvLink.getText().toString();
				RssListDalHelper helper=new RssListDalHelper(getApplicationContext());
				try{
					helper.Delete(link);
					Toast.makeText(getApplicationContext(), R.string.unrss_succ, Toast.LENGTH_SHORT).show();
				}catch(Exception ex){
					Toast.makeText(getApplicationContext(), R.string.unrss_fail, Toast.LENGTH_SHORT).show();
				}
				progressDialog.dismiss();
				// 广播
				TextView tvTitle = (TextView) v.findViewById(R.id.rss_item_title);
				TextView tvSummary = (TextView) v.findViewById(R.id.rss_item_summary);
				TextView tvId = (TextView) v.findViewById(R.id.rss_item_id);
				TextView tvIsCnblogs = (TextView) v.findViewById(R.id.rss_item_is_cnblogs);
				TextView tvAuthor = (TextView) v.findViewById(R.id.rss_item_author);
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putStringArray("rsslist", 
						new String[]{tvAuthor.getText().toString() ,tvSummary.getText().toString(),
							tvId.getText().toString(),tvTitle.getText().toString(),
							"",link,
							tvIsCnblogs.getText().toString()
						});
				bundle.putBoolean("isrss", false);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_rsslist");
				sendBroadcast(intent);
		}
		return super.onContextItemSelected(item);
	}
	/**
	 * 加载数据
	 * 
	 * @author walkingp
	 * @date 2012-3-13
	 */
	public class PageTask extends AsyncTask<String, Integer, List<RssList>> {
		@Override
		protected List<RssList> doInBackground(String... params) {
			RssListDalHelper helper = new RssListDalHelper(
					getApplicationContext());
			return helper.GetRssList();
		}
		@Override
		protected void onPostExecute(List<RssList> result) {
			if (listView.getCount() == 0) {
				bodyProgressBar.setVisibility(View.GONE);
				topProgressBar.setVisibility(View.GONE);
				btnRefresh.setVisibility(View.VISIBLE);
			}
			if (result == null || result.size() == 0) {
				txtNoData.setVisibility(View.VISIBLE);
			} else {
				adapter = new RssListAdapter(getApplicationContext(), result,
						listView, RssListAdapter.EnumSource.MyRss, adapter);
				listView.setAdapter(adapter);
				listView.setSelection(lastItemPosition);
			}
		}
		@Override
		protected void onPreExecute() {
			if (listView.getCount() == 0) {
				bodyProgressBar.setVisibility(View.VISIBLE);
				topProgressBar.setVisibility(View.VISIBLE);
				btnRefresh.setVisibility(View.GONE);
			}
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
			String[] arr=bundle.getStringArray("rsslist");
			if(arr!=null){
				boolean isRss=bundle.getBoolean("isrss");
				
				RssList entity=new RssList();
				entity.SetAuthor(arr[0]);
				entity.SetDescription(arr[1]);
				entity.SetGuid(arr[2]);
				entity.SetTitle(arr[3]);
				entity.SetImage(arr[4]);
				entity.SetLink(arr[5]);
				entity.SetIsCnblogs(arr[6].equals("1"));

				if(isRss){
					List<RssList> list=new ArrayList<RssList>();
					list.add(entity);
					adapter.AddMoreData(list);
				}else{
					adapter.RemoveData(entity);
				}
			}
		}		
	}
}
