package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.cache.AsyncImageLoader;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.cache.AsyncImageLoader.ImageCallback;
import com.cnblogs.android.entity.RssCate;
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

public class RssCateListAdapter extends BaseAdapter {
	private List<RssCate> list;
	private LayoutInflater mInflater;
	private ListView listView;
	private AsyncImageLoader asyncImageLoader;

	public RssCateListAdapter(Context context, List<RssCate> list,
			ListView listView) {
		this.list = list;
		this.listView = listView;
		asyncImageLoader = new AsyncImageLoader(context);
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		RssCate entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.rss_cate_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.rsscate_list_item, null);
			viewHolder.rss_cate_icon = (ImageView) convertView
					.findViewById(R.id.rss_cate_icon);
			viewHolder.rss_cate_title = (TextView) convertView
					.findViewById(R.id.rss_cate_title);
			viewHolder.rss_cate_summary = (TextView) convertView
					.findViewById(R.id.rss_cate_summary);
			viewHolder.rss_cate_id = (TextView) convertView
					.findViewById(R.id.rss_cate_id);
		}
		String tag = entity.GetIcon();
		if (tag.contains("?")) {// 截断?后的字符串，避免无效图片
			tag = tag.substring(0, tag.indexOf("?"));
		}

		viewHolder.rss_cate_icon.setTag(tag);
		Drawable cachedImage = asyncImageLoader.loadDrawable(
				ImageCacher.EnumImageType.RssIcon, tag, new ImageCallback() {
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
		if (cachedImage != null) {
			viewHolder.rss_cate_icon.setImageDrawable(cachedImage);
		}

		viewHolder.rss_cate_title.setText(entity.GetCateName());
		viewHolder.rss_cate_summary.setText(entity.GetSummary());
		viewHolder.rss_cate_id.setText(String.valueOf(entity.GetCateId()));

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<RssCate> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<RssCate> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<RssCate> list) {
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
		ImageView rss_cate_icon;
		TextView rss_cate_title;
		TextView rss_cate_summary;
		TextView rss_cate_id;
	}
}
