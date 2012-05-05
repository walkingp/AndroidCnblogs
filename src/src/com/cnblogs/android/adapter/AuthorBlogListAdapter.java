package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.utility.AppUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
public class AuthorBlogListAdapter extends BaseAdapter {
	private List<Blog> list;
	private LayoutInflater mInflater;
	Context context;

	public AuthorBlogListAdapter(Context context, List<Blog> list) {
		this.list = list;
		this.context=context;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		Blog entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.author_blog_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.author_blog_list_item,
					null);

			viewHolder.recommend_text_id = (TextView) convertView
					.findViewById(R.id.recommend_text_id);
			viewHolder.recommend_text_title = (TextView) convertView
					.findViewById(R.id.recommend_text_title);
			viewHolder.recommend_text_desc = (TextView) convertView
					.findViewById(R.id.recommend_text_desc);
			viewHolder.recommend_text_diggs = (TextView) convertView
					.findViewById(R.id.recommend_text_diggs);
			viewHolder.recommend_text_view = (TextView) convertView
					.findViewById(R.id.recommend_text_view);
			viewHolder.recommend_text_comments = (TextView) convertView
					.findViewById(R.id.recommend_text_comments);
			viewHolder.recommend_text_author = (TextView) convertView
					.findViewById(R.id.recommend_text_author);
			viewHolder.recommend_text_date = (TextView) convertView
					.findViewById(R.id.recommend_text_date);
			viewHolder.recommend_text_formatdate = (TextView) convertView
					.findViewById(R.id.recommend_text_formatdate);
			viewHolder.recommend_text_url = (TextView) convertView
					.findViewById(R.id.recommend_text_url);
		}

		viewHolder.recommend_text_title.setText(entity.GetBlogTitle());
		// 是否已读
		boolean isReaded = entity.GetIsReaded();
		if (isReaded) {
			viewHolder.recommend_text_title.setTextColor(R.color.gray);
		}
		viewHolder.recommend_text_desc.setText(entity.GetSummary());
		viewHolder.recommend_text_diggs.setText(String.valueOf(entity
				.GetDiggsNum()));
		viewHolder.recommend_text_author.setText(entity.GetAuthor());
		viewHolder.recommend_text_comments.setText(String.valueOf(entity
				.GetCommentNum()));
		viewHolder.recommend_text_view.setText(String.valueOf(entity
				.GetViewNum()));
		viewHolder.recommend_text_date.setText(AppUtil.ParseDateToString(entity
				.GetAddTime()));
		String simpleDateString = AppUtil.DateToChineseString(entity
				.GetAddTime());
		viewHolder.recommend_text_formatdate.setText(simpleDateString);
		viewHolder.recommend_text_url.setText(entity.GetBlogUrl());
		viewHolder.recommend_text_id
				.setText(String.valueOf(entity.GetBlogId()));		


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
		TextView recommend_text_id;
		TextView recommend_text_title;
		TextView recommend_text_desc;
		TextView recommend_text_diggs;
		TextView recommend_text_view;
		TextView recommend_text_comments;
		TextView recommend_text_author;
		TextView recommend_text_formatdate;
		TextView recommend_text_date;
		TextView recommend_text_url;
	}
}
