package com.cnblogs.android;

import java.util.ArrayList;
import java.util.List;
import com.cnblogs.android.adapter.UserListAdapter;
import com.cnblogs.android.core.UserHelper;
import com.cnblogs.android.entity.Users;
import com.cnblogs.android.utility.NetHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
/**
 * 搜索
 * @author walkingp
 * @date:2011-12
 *
 */
public class SearchActivity extends BaseMainActivity {
	EditText txtSearch;
	List<Users> listUser = new ArrayList<Users>();

	UserListAdapter adapter;

	private String q;// 搜索词

	ListView listView;
	ImageButton search_btn;// 搜索按钮

	ProgressBar progressBar;// 加载
	Resources res;// 资源
	SharedPreferences sharePreferencesSettings;// 设置
	
	ImageButton btnItem;//按钮
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.search_layout);
		res = this.getResources();

		boolean isShowQuitHints=getIntent().getBooleanExtra("isShowQuitHints", true);
		super.IsShowQuitHints=isShowQuitHints;
		BindControls();
		BindEvent();
	}
	/**
	 * 找到控件
	 */
	private void BindControls() {
		txtSearch = (EditText) findViewById(R.id.txtSearch);
		txtSearch.clearFocus();
		listView = (ListView) findViewById(R.id.search_list);
		progressBar = (ProgressBar) findViewById(R.id.searchList_progressBar);
		search_btn = (ImageButton) findViewById(R.id.search_btn);
		btnItem=(ImageButton)findViewById(R.id.btnItem);
		// 上一次查询
		sharePreferencesSettings = getSharedPreferences(
				res.getString(R.string.preferences_key), MODE_PRIVATE);
		String lastSearch = sharePreferencesSettings.getString(
				res.getString(R.string.preference_last_search_keyword), "");
		txtSearch.setText(lastSearch);
		// 回车搜索
		txtSearch.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (KeyEvent.KEYCODE_ENTER == keyCode) {
					StartSearch();
					return true;
				}
				return false;
			}
		});
		//跳转到排行
		btnItem.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(SearchActivity.this,OrderActivity.class);
				startActivity(intent);
			}			
		});
	}
	/**
	 * 绑定事件
	 */
	private void BindEvent() {
		// 查询
		search_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				StartSearch();
			}
		});
		// 点击跳转
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				Intent intent = new Intent();
				try {
					// 传递参数
					intent.setClass(SearchActivity.this,
							AuthorBlogActivity.class);
					Bundle bundle = new Bundle();
					TextView tvBlogTitle = (TextView) (v
							.findViewById(R.id.author_list_title));
					TextView tvBlogAuthor = (TextView) (v
							.findViewById(R.id.author_list_username));

					String blogTitle = tvBlogTitle.getText().toString();
					String blogAuthor = tvBlogAuthor.getText().toString();

					bundle.putString("blogName", blogTitle);
					bundle.putString("author", blogAuthor);

					intent.putExtras(bundle);

					startActivity(intent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}
	/**
	 * 执行搜索
	 */
	private void StartSearch() {
		q = txtSearch.getText().toString();
		if (q.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.sys_input_empty,
					Toast.LENGTH_SHORT);
			txtSearch.setFocusable(true);
			return;
		}
		// 保存配置
		sharePreferencesSettings
				.edit()
				.putString(
						res.getString(R.string.preference_last_search_keyword),
						q).commit();
		new PageTask().execute();
	}
	/**
	 * 多线程启动（用于上拉加载、初始化、下载加载、刷新）
	 * 
	 */
	public class PageTask extends AsyncTask<String, Integer, List<Users>> {
		public PageTask() {
		}

		protected List<Users> doInBackground(String... params) {

			try {
				List<Users> listTmp=new ArrayList<Users>();
				List<Users> listUserNew = UserHelper.GetUserList(q);
				int size = listUserNew.size();
				for (int i = 0; i < size; i++) {
					if (!listUser.contains(listUserNew.get(i))) {// 避免出现重复
						listTmp.add(listUserNew.get(i));
					}
				}
				return listTmp;
			} catch (Exception e) {
				e.printStackTrace();
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
		protected void onPostExecute(List<Users> result) {
			// 网络不可用
			if (!NetHelper.networkIsAvailable(getApplicationContext())) {
				Toast.makeText(getApplicationContext(),
						R.string.sys_network_error, Toast.LENGTH_SHORT).show();
				return;
			}

			if (result == null || result.size() == 0) {// 没有新数据
				return;
			}

			listUser = result;

			progressBar.setVisibility(View.GONE);
			adapter = new UserListAdapter(getApplicationContext(), listUser,
					listView);
			listView.setAdapter(adapter);
		}
		@Override
		protected void onPreExecute() {
			// 主体进度条
			if (listView.getCount() == 0) {
				progressBar.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
		}
	}
}
