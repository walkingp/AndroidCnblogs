package com.cnblogs.android.adapter;

import java.util.List;

import com.cnblogs.android.R;
import com.cnblogs.android.dal.RssListDalHelper;
import com.cnblogs.android.entity.RssList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RssListAdapter extends BaseAdapter {
	private List<RssList> list;
	private LayoutInflater mInflater;
	// private ListView listView;
	Context context;
	RssListAdapter adapter;
	private EnumSource source;
	public enum EnumSource {
		MyRss, RssList
	}
	public RssListAdapter(Context context, List<RssList> list,
			ListView listView, EnumSource source, RssListAdapter adapter) {
		this.source = source;
		this.list = list;
		this.context = context;
		this.adapter = adapter;
		// this.listView=listView;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		RssList entity = list.get(position);
		if (convertView != null && convertView.getId() == R.id.rss_list) {
			viewHolder = (ViewHolder) convertView.getTag();
		} else {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.rsslist_list_item, null);
			viewHolder.list_btn_rss = (Button) convertView.findViewById(R.id.list_btn_rss);
			viewHolder.rss_item_title = (TextView) convertView.findViewById(R.id.rss_item_title);
			viewHolder.rss_item_summary = (TextView) convertView.findViewById(R.id.rss_item_summary);
			viewHolder.rss_item_id = (TextView) convertView.findViewById(R.id.rss_item_id);
			viewHolder.rss_item_url = (TextView) convertView.findViewById(R.id.rss_item_url);
			viewHolder.rss_item_is_cnblogs = (TextView) convertView.findViewById(R.id.rss_item_is_cnblogs);
			viewHolder.rss_item_author = (TextView) convertView.findViewById(R.id.rss_item_author);
		}
		// 是否已经订阅
		RssListDalHelper helper = new RssListDalHelper(context);
		final boolean isRssed = helper.Exist(entity.GetLink());
		viewHolder.list_btn_rss.setTag(isRssed);
		// 上一级Activity
		if (source.equals(EnumSource.MyRss)) {// 如果是我的订阅
			viewHolder.list_btn_rss.setVisibility(View.GONE);
		} else {// 如果是从订阅分类
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 订阅 取消订阅
					if (v == viewHolder.list_btn_rss) {
						String url = viewHolder.rss_item_url.getText()
								.toString();
						RssList entity = new RssList();
						// entity.SetAddTime(new java.util.Date());
						entity.SetAuthor(viewHolder.rss_item_author.getText()
								.toString());
						entity.SetCateId(0);
						entity.SetCateName("");
						entity.SetDescription(viewHolder.rss_item_summary
								.getText().toString());
						entity.SetGuid(viewHolder.rss_item_id.getText()
								.toString());
						entity.SetImage("");
						entity.SetIsActive(true);
						entity.SetIsCnblogs(viewHolder.rss_item_is_cnblogs
								.getText().equals("1"));
						entity.SetLink(url);
						entity.SetOrderNum(0);
						entity.SetTitle(viewHolder.rss_item_title.getText()
								.toString());

						RssListDalHelper helper = new RssListDalHelper(context);

						// 广播
						Intent intent = new Intent();
						Bundle bundle = new Bundle();
						bundle.putStringArray("rsslist", 
								new String[]{entity.GetAuthor(),entity.GetDescription(),entity.GetGuid(),
									entity.GetTitle(),entity.GetImage(),entity.GetLink(),
									entity.GetIsCnblogs() ? "1" : "0"
								});
						
						boolean _isRssed = Boolean.parseBoolean(viewHolder.list_btn_rss.getTag().toString());
						if (_isRssed) {// 退订
							helper.Delete(entity.GetLink());

							viewHolder.list_btn_rss
									.setBackgroundResource(R.drawable.drawable_btn_rss);
							viewHolder.list_btn_rss.setText(R.string.btn_rss);
							viewHolder.list_btn_rss.setTextColor(R.color.gray);
							viewHolder.list_btn_rss.setTag(false);
							
							bundle.putBoolean("isrss", false);

							Toast.makeText(context, R.string.unrss_succ, Toast.LENGTH_SHORT)
									.show();
						} else {// 订阅
							helper.Insert(entity);

							viewHolder.list_btn_rss
									.setBackgroundResource(R.drawable.btn_rssed);
							viewHolder.list_btn_rss.setText(R.string.btn_unrss);
							viewHolder.list_btn_rss
									.setTextColor(R.color.darkblue);
							viewHolder.list_btn_rss.setTag(true);

							bundle.putBoolean("isrss", true);
							
							Toast.makeText(context, R.string.rss_succ, Toast.LENGTH_SHORT)
									.show();
						}
						// 发送广播
						intent.putExtras(bundle);
						intent.setAction("android.cnblogs.com.update_rsslist");
						context.sendBroadcast(intent);
					}
				}
			};
			viewHolder.list_btn_rss.setOnClickListener(listener);
		}
		if (isRssed) {
			viewHolder.list_btn_rss.setBackgroundResource(R.drawable.btn_rssed);
			viewHolder.list_btn_rss.setText(R.string.btn_unrss);
			viewHolder.list_btn_rss.setTextColor(R.color.gray);
		}

		viewHolder.rss_item_title.setText(entity.GetTitle().trim());
		viewHolder.rss_item_summary.setText(entity.GetDescription().trim());
		viewHolder.rss_item_id.setText(String.valueOf(entity.GetRssId()));
		viewHolder.rss_item_url.setText(entity.GetLink());
		// 是否博客园，如果是，则跳转到该用户博客园文章列表
		viewHolder.rss_item_is_cnblogs.setText(entity.GetIsCnblogs()
				? "1"
				: "0");
		viewHolder.rss_item_author.setText(entity.GetAuthor());

		convertView.setTag(viewHolder);
		return convertView;
	}

	/**
	 * 得到数据
	 * 
	 * @return
	 */
	public List<RssList> GetData() {
		return list;
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void InsertData(List<RssList> list) {
		this.list.addAll(0, list);
		this.notifyDataSetChanged();
	}
	/**
	 * 增加数据
	 * 
	 * @param list
	 */
	public void AddMoreData(List<RssList> list) {
		this.list.addAll(list);
		this.notifyDataSetChanged();
	}
	/**
	 * 移除数据
	 * @param entity
	 */
	public void RemoveData(RssList entity){
		for(int i=0,len=this.list.size();i<len;i++){
			if(this.list.get(i).GetLink().equals(entity.GetLink())){
				this.list.remove(i);
				this.notifyDataSetChanged();
				break;
			}
		}
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
		Button list_btn_rss;
		TextView rss_item_title;
		TextView rss_item_summary;
		TextView rss_item_id;
		TextView rss_item_url;
		TextView rss_item_is_cnblogs;
		TextView rss_item_author;
	}
}
