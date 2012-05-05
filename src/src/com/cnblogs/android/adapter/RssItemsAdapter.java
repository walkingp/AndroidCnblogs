package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.entity.RssItem;
import com.cnblogs.android.utility.AppUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RssItemsAdapter extends BaseAdapter {
	private List<RssItem> list;
	private LayoutInflater mInflater;

	public RssItemsAdapter(Context context, List<RssItem> list) {
		this.list = list;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		RssItem entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.rss_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.rssitems_list_item, null);

			viewHolder.text_title = (TextView) convertView
					.findViewById(R.id.recommend_text_title);
			viewHolder.text_desc = (TextView) convertView
					.findViewById(R.id.recommend_text_desc);
			viewHolder.text_full_desc = (TextView) convertView
					.findViewById(R.id.recommend_text_full_text);
			viewHolder.text_author = (TextView) convertView
					.findViewById(R.id.recommend_text_author);
			viewHolder.text_date = (TextView) convertView
					.findViewById(R.id.recommend_text_date);
			viewHolder.text_formatdate = (TextView) convertView
					.findViewById(R.id.recommend_text_formatdate);
			viewHolder.text_url = (TextView) convertView
					.findViewById(R.id.recommend_text_url);
			viewHolder.text_blog_id = (TextView) convertView
					.findViewById(R.id.recommend_text_id);
			viewHolder.recommend_text_cate=(TextView)convertView.findViewById(R.id.recommend_text_cate);
		}

		viewHolder.text_title.setText(entity.GetTitle());
		// 是否已读
		boolean isReaded = entity.GetIsReaded();
		if (isReaded) {
			viewHolder.text_title.setTextColor(R.color.gray);
		}
		viewHolder.text_full_desc.setText(entity.GetDescription());
		String summary = AppUtil.RemoveHtmlTag(entity.GetDescription());
		summary = summary.replaceAll("\n|\t|\r|\\s", "").trim();
		viewHolder.text_desc.setText(summary);
		viewHolder.text_author.setText(entity.GetAuthor());
		if (entity.GetAddDate() != null) {
			viewHolder.text_date.setText(AppUtil.ParseDateToString(entity
					.GetAddDate()));
			String simpleDateString = AppUtil.DateToChineseString(entity
					.GetAddDate());
			viewHolder.text_formatdate.setText(simpleDateString);
		}else{
			viewHolder.text_date.setText("未知时间");
			viewHolder.text_formatdate.setText("未知时间");
		}
		if (entity.GetLink() != null) {
			viewHolder.text_url.setText(entity.GetLink());
		}
		viewHolder.text_blog_id.setText(String.valueOf(entity.GetId()));
		
		String cate="无";
		if(entity.GetCategory()!=null && !entity.GetCategory().equals("")){
			cate=entity.GetCategory();
		}
		viewHolder.recommend_text_cate.setText(cate);

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<RssItem> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<RssItem> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<RssItem> list) {
		this.list.addAll(list);
		this.notifyDataSetChanged();
	}
	public int getCount() {
		if (list == null) {
			return 0;
		}
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
		TextView text_author;
		TextView text_date;
		TextView text_formatdate;
		TextView text_url;
		TextView text_blog_id;
		TextView text_full_desc;
		TextView recommend_text_cate;
		TextView recommend_text_date;
	}
}
