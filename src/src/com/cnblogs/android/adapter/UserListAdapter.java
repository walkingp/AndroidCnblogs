package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.SettingActivity;
import com.cnblogs.android.cache.AsyncImageLoader;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.cache.AsyncImageLoader.ImageCallback;
import com.cnblogs.android.entity.Users;
import com.cnblogs.android.utility.AppUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class UserListAdapter extends BaseAdapter {
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	private List<Users> list;
	private LayoutInflater mInflater;
	private Context currentContext;

	public UserListAdapter(Context context, List<Users> list,
			ListView listView) {
		currentContext = context;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader(context);
		this.list = list;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Users entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.blog_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.search_list_item, null);

			viewHolder.author_list_title = (TextView) convertView
					.findViewById(R.id.author_list_title);
			viewHolder.author_list_url = (TextView) convertView
					.findViewById(R.id.author_list_url);
			viewHolder.author_list_avatar = (ImageView) convertView
					.findViewById(R.id.author_list_avatar);
			viewHolder.author_list_username = (TextView) convertView
					.findViewById(R.id.author_list_username);
			viewHolder.author_list_blogcount = (TextView) convertView
					.findViewById(R.id.author_list_blogcount);
			viewHolder.author_list_update = (TextView) convertView
					.findViewById(R.id.author_list_update);
		}
		String tag = entity.GetAvator();
		if (tag.contains("?")) {// 截断?后的字符串，避免无效图片
			tag = tag.substring(0, tag.indexOf("?"));
		}

		viewHolder.author_list_avatar.setTag(tag);
		Drawable cachedImage = asyncImageLoader.loadDrawable(
				ImageCacher.EnumImageType.Avatar, tag, new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable, String tag) {
						Log.i("Drawable", tag);
						ImageView imageViewByTag = (ImageView) listView
								.findViewWithTag(tag);
						if (imageViewByTag != null && imageDrawable != null) {
							imageViewByTag.setImageDrawable(imageDrawable);
						}
					}
				});
		// 阅读模式
		boolean isPicReadMode = SettingActivity.IsPicReadMode(currentContext);
		if (isPicReadMode) {
			viewHolder.author_list_avatar
					.setImageResource(R.drawable.sample_face);
			if (cachedImage != null) {
				viewHolder.author_list_avatar.setImageDrawable(cachedImage);
			}
		} else {
			viewHolder.author_list_avatar.setVisibility(View.GONE);
		}

		viewHolder.author_list_title.setText(entity.GetBlogName());
		viewHolder.author_list_url.setText(entity.GetBlogUrl());
		viewHolder.author_list_username.setText(String.valueOf(entity
				.GetUserName()));
		// 时间
		String simpleDateString = AppUtil.DateToChineseString(entity
				.GetLastUpdate());//
		viewHolder.author_list_update.setText(simpleDateString);
		viewHolder.author_list_blogcount.setText(String.valueOf(entity
				.GetBlogCount()));

		convertView.setTag(viewHolder);
		return convertView;
	}
	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<Users> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<Users> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Users> list) {
		this.list.addAll(list);
		this.notifyDataSetChanged();
	}
	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	public class ViewHolder {
		TextView author_list_title;
		TextView author_list_url;
		ImageView author_list_avatar;
		TextView author_list_username;
		TextView author_list_blogcount;
		TextView author_list_update;
	}
}
