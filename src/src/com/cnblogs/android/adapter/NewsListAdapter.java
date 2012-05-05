package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.AppUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsListAdapter extends BaseAdapter {
	private List<News> list;
	private LayoutInflater mInflater;

	public NewsListAdapter(Context context, List<News> list) {
		this.list = list;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		News entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.news_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.news_list_item, null);

			viewHolder.news_text_id = (TextView) convertView.findViewById(R.id.news_text_id);
			viewHolder.news_text_title = (TextView) convertView.findViewById(R.id.news_text_title);
			viewHolder.news_text_summary = (TextView) convertView.findViewById(R.id.news_text_summary);
			viewHolder.news_text_diggs = (TextView) convertView.findViewById(R.id.news_text_diggs);
			viewHolder.news_text_view = (TextView) convertView.findViewById(R.id.news_text_view);
			viewHolder.news_text_comments = (TextView) convertView.findViewById(R.id.news_text_comments);
			viewHolder.news_text_date = (TextView) convertView.findViewById(R.id.news_text_date);
			viewHolder.news_text_format_date = (TextView) convertView.findViewById(R.id.news_text_format_date);
			viewHolder.news_text_url = (TextView) convertView.findViewById(R.id.news_text_url);
			viewHolder.icon_downloaded=(ImageView)convertView.findViewById(R.id.icon_downloaded);
		}

		viewHolder.news_text_title.setText(entity.GetNewsTitle());
		// 是否已读
		boolean isReaded = entity.GetIsReaded();
		if (isReaded) {
			viewHolder.news_text_title.setTextColor(R.color.gray);
		}
		viewHolder.news_text_summary.setText(entity.GetSummary());
		viewHolder.news_text_id.setText(String.valueOf(entity.GetNewsId()));
		viewHolder.news_text_comments.setText(String.valueOf(entity
				.GetCommentNum()));
		viewHolder.news_text_view.setText(String.valueOf(entity.GetViewNum()));
		viewHolder.news_text_diggs
				.setText(String.valueOf(entity.GetDiggsNum()));
		viewHolder.news_text_date.setText(AppUtil.ParseDateToString(entity
				.GetAddTime()));
		String simpleDateString = AppUtil.DateToChineseString(entity
				.GetAddTime());
		viewHolder.news_text_format_date.setText(simpleDateString);
		viewHolder.news_text_url.setText(entity.GetNewsUrl());
		
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
	public List<News> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<News> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<News> list) {
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
		TextView news_text_id;
		TextView news_text_title;
		TextView news_text_summary;
		TextView news_text_diggs;
		TextView news_text_view;
		TextView news_text_comments;
		TextView news_text_date;
		TextView news_text_format_date;
		TextView news_text_url;
		ImageView icon_downloaded;
	}
}
