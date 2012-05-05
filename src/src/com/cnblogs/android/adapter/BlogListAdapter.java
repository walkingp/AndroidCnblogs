package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.SettingActivity;
import com.cnblogs.android.cache.AsyncImageLoader;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.cache.AsyncImageLoader.ImageCallback;
import com.cnblogs.android.entity.Blog;
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

public class BlogListAdapter extends BaseAdapter {
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;
	private List<Blog> list;
	private LayoutInflater mInflater;
	private Context currentContext;

	public BlogListAdapter(Context context, List<Blog> list, ListView listView) {
		currentContext = context;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader(context);
		this.list = list;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Blog entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.blog_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.blog_list_item, null);

			viewHolder.text_title = (TextView) convertView
					.findViewById(R.id.recommend_text_title);
			viewHolder.text_desc = (TextView) convertView
					.findViewById(R.id.recommend_text_desc);
			viewHolder.imageIcon = (ImageView) convertView
					.findViewById(R.id.recommend_image_icon);
			viewHolder.text_diggs = (TextView) convertView
					.findViewById(R.id.recommend_text_diggs);
			viewHolder.text_author = (TextView) convertView
					.findViewById(R.id.recommend_text_author);
			viewHolder.text_comments = (TextView) convertView
					.findViewById(R.id.recommend_text_comments);
			viewHolder.text_view = (TextView) convertView
					.findViewById(R.id.recommend_text_view);
			viewHolder.text_date = (TextView) convertView
					.findViewById(R.id.recommend_text_date);
			viewHolder.text_formatdate = (TextView) convertView
					.findViewById(R.id.recommend_text_formatdate);
			viewHolder.text_url = (TextView) convertView
					.findViewById(R.id.recommend_text_url);
			viewHolder.text_domain = (TextView) convertView
					.findViewById(R.id.recommend_text_domain);
			viewHolder.text_blog_id = (TextView) convertView
					.findViewById(R.id.recommend_text_id);
			viewHolder.text_user_name=(TextView)convertView.findViewById(R.id.recommend_user_name);
			viewHolder.icon_downloaded=(ImageView)convertView.findViewById(R.id.icon_downloaded);
		}
		if(entity.GetAvator()!=null){
			String tag = entity.GetAvator();
			if (tag.contains("?")) {// 截断?后的字符串，避免无效图片
				tag = tag.substring(0, tag.indexOf("?"));
			}
	
			viewHolder.imageIcon.setTag(tag);
			Drawable cachedImage = asyncImageLoader.loadDrawable(
					ImageCacher.EnumImageType.Avatar, tag, new ImageCallback() {
						public void imageLoaded(Drawable imageDrawable, String tag) {
							Log.i("Drawable", tag);
							ImageView imageViewByTag = (ImageView) listView
									.findViewWithTag(tag);
							if (imageViewByTag != null && imageDrawable != null) {
								imageViewByTag.setImageDrawable(imageDrawable);
							} else {
								try {
									imageViewByTag
											.setImageResource(R.drawable.sample_face);
								} catch (Exception ex) {
	
								}
							}
						}
					});
			// 阅读模式
			boolean isPicReadMode = SettingActivity.IsPicReadMode(currentContext);
			if (isPicReadMode) {
				viewHolder.imageIcon.setImageResource(R.drawable.sample_face);
				if (cachedImage != null) {
					viewHolder.imageIcon.setImageDrawable(cachedImage);
				}
			} else {
				viewHolder.imageIcon.setVisibility(View.GONE);
			}
		}

		viewHolder.text_title.setText(entity.GetBlogTitle());
		// 是否已读
		boolean isReaded = entity.GetIsReaded();
		Log.i("title", entity.GetBlogTitle());
		if (isReaded) {
			//viewHolder.text_title.setTextColor(R.color.gray);
		}
		viewHolder.text_desc.setText(entity.GetSummary());
		viewHolder.text_diggs.setText(String.valueOf(entity.GetDiggsNum()));
		viewHolder.text_author.setText(entity.GetAuthor());
		viewHolder.text_comments
				.setText(String.valueOf(entity.GetCommentNum()));
		viewHolder.text_view.setText(String.valueOf(entity.GetViewNum()));
		viewHolder.text_date.setText(AppUtil.ParseDateToString(entity
				.GetAddTime()));
		String simpleDateString = AppUtil.DateToChineseString(entity
				.GetAddTime());
		viewHolder.text_formatdate.setText(simpleDateString);
		viewHolder.text_url.setText(entity.GetBlogUrl());
		viewHolder.text_domain.setText(entity.GetAuthorUrl());
		viewHolder.text_blog_id.setText(String.valueOf(entity.GetBlogId()));
		viewHolder.text_user_name.setText(entity.GetUserName());
		
		if(!entity.GetIsFullText()){
			viewHolder.icon_downloaded.setVisibility(View.GONE);
		}

		convertView.setTag(viewHolder);
		return convertView;
	}
	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<Blog> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<Blog> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Blog> list) {
		this.list.addAll(list);
		this.notifyDataSetChanged();
	}
	/**
	 * 移除数据
	 * @param entity
	 */
	public void RemoveData(Blog entity){
		for(int i=0,len=this.list.size();i<len;i++){
			if(this.list.get(i).GetBlogId()==entity.GetBlogId()){
				this.list.remove(i);
				this.notifyDataSetChanged();
				break;
			}
		}
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
		TextView text_title;
		TextView text_desc;
		ImageView imageIcon;
		TextView text_diggs;
		TextView text_view;
		TextView text_comments;
		TextView text_author;
		TextView text_date;
		TextView text_formatdate;
		TextView text_url;
		TextView text_domain;
		TextView text_blog_id;
		TextView text_user_name;
		ImageView icon_downloaded;
	}
}
