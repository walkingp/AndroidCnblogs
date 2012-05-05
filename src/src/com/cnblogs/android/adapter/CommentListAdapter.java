package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.HtmlRegexpUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CommentListAdapter extends BaseAdapter {
	private List<Comment> list;
	private LayoutInflater mInflater;
	private int pageIndex;

	public CommentListAdapter(Context context, List<Comment> list, int pageIndex) {
		this.list = list;
		this.pageIndex = pageIndex;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Comment entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.comment_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.comment_list_item, null);
			viewHolder.comment_id = (TextView) convertView
					.findViewById(R.id.comment_id);
			viewHolder.comment_user_name = (TextView) convertView
					.findViewById(R.id.comment_user_name);
			viewHolder.comment_user_url = (TextView) convertView
					.findViewById(R.id.comment_user_url);
			viewHolder.comment_content = (TextView) convertView
					.findViewById(R.id.comment_content);
			viewHolder.comment_format_date = (TextView) convertView
					.findViewById(R.id.comment_format_date);
			viewHolder.comment_date = (TextView) convertView
					.findViewById(R.id.comment_date);
			viewHolder.comment_floor = (TextView) convertView
					.findViewById(R.id.comment_floor);
		}

		viewHolder.comment_id.setText(String.valueOf(entity.GetCommentId()));
		viewHolder.comment_user_name.setText(entity.GetPostUserName());
		viewHolder.comment_user_url.setText(entity.GetPostUserUrl());
		// 替换掉所有的html标签
		String content = entity.GetContent();
		content = content.replace("<br />", "\r\n");
		content = HtmlRegexpUtil.filterHtml(content);
		viewHolder.comment_content.setText(content);

		viewHolder.comment_date.setText(AppUtil.ParseDateToString(entity
				.GetAddTime()));
		String simpleDateString = AppUtil.DateToChineseString(entity
				.GetAddTime());
		viewHolder.comment_format_date.setText(simpleDateString);
		// 楼层
		int floorNum = (pageIndex - 1) * Config.COMMENT_PAGE_SIZE + position
				+ 1;
		viewHolder.comment_floor.setText(String.valueOf(floorNum) + "楼：");

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<Comment> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<Comment> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<Comment> list) {
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
		TextView comment_id;
		TextView comment_user_name;
		TextView comment_user_url;
		TextView comment_content;
		TextView comment_date;
		TextView comment_format_date;
		TextView comment_floor;
	}
}
