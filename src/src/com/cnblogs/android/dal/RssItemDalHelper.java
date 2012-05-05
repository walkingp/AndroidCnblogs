package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.RssItem;
import com.cnblogs.android.utility.AppUtil;

public class RssItemDalHelper {
	private DBHelper.DatabaseHelper		dbHelper;
	private SQLiteDatabase		db;
	public final static byte[] _writeLock = new byte[0];
	
	public RssItemDalHelper(Context context){
		dbHelper=new DBHelper.DatabaseHelper(context);
		db=dbHelper.getWritableDatabase();
	}
	/**
	 * 判断是否已经存在
	 * @param blogId
	 * @return
	 */
	private boolean Exist(String link){
		String where="Link=?";
		String[] args={link};
		Cursor cursor= db.query(Config.DB_RSSITEM_TABLE, null, where, args, null, null, null);
		boolean isExist= cursor!=null && cursor.moveToNext();
		cursor.close();
		
		return isExist;
	}
	/**
	 * 得到头条
	 * @return
	 */
	public List<RssItem> GetTopNewsList(){
		String limit="10";
		
		return GetRssItemsByWhere(limit,null,null);
	}
	/*
	 * 分页
	 */
	public List<RssItem> GetRssItemByPage(int pageIndex,int pageSize){
		String limit= String.valueOf((pageIndex-1)*pageSize) + "," + String.valueOf(pageSize);
		List<RssItem> list=GetRssItemsByWhere(limit,null,null);
		
		return list;
	}
	/*
	 * 得到对象
	 */
	public RssItem GetRssItemEntity(int id){
		String limit="1";
		String where="Id=?";
		String[] args={String.valueOf(id)};
		List<RssItem> list=GetRssItemsByWhere(limit,where,args);
		if(list.size()>0){
			return list.get(0);
		}
		
		return null;
	}
	/**
	 * 根据条件得到
	 * @param top
	 * @param where
	 */
	public List<RssItem> GetRssItemsByWhere(String limit,String where,String[] args){
		List<RssItem> listRss=new ArrayList<RssItem>();
		String orderBy="Id desc";
		Cursor cursor=db.query(Config.DB_RSSITEM_TABLE, null, where, args, null, null, orderBy,limit);
		while(cursor!=null && cursor.moveToNext()){
			RssItem entity=new RssItem();
			String addTimeStr=cursor.getString(cursor.getColumnIndex("AddDate"));
			Date addTime=AppUtil.ParseDate(addTimeStr);
			entity.SetAddDate(addTime);		
			entity.SetDescription(cursor.getString(cursor.getColumnIndex("Description")));
			entity.SetTitle(cursor.getString(cursor.getColumnIndex("Title")));
			entity.SetId(cursor.getInt(cursor.getColumnIndex("Id")));
			String link="";
			if(cursor.getString(cursor.getColumnIndex("Link"))!=null){
				link=cursor.getString(cursor.getColumnIndex("NewsUrl"));
			}
			entity.SetLink(link);
			entity.SetIsReaded(Boolean.getBoolean(cursor.getString(cursor.getColumnIndex("IsReaded"))));
			entity.SetAuthor(cursor.getString(cursor.getColumnIndex("Author")));
			boolean isDigg=cursor.getString(cursor.getColumnIndex("IsDigg")).equals("1");
			entity.SetIsReaded(isDigg);
			
			listRss.add(entity);
		}
		cursor.close();
		
		return listRss;
	}
	/**
	 * 是否已读
	 * @param blogId
	 * @return
	 */
	public boolean GetIsReaded(int id){
		RssItem entity=GetRssItemEntity(id);
		if(entity!=null){
			return entity.GetIsReaded();
		}
		return false;
	}
	/**
	 * 标志为已读
	 * @param blogId
	 */
	public void MarkAsReaded(int id){
		String sql="update RssItem set IsReaded=1 where Id=?";
		String[] args={String.valueOf(id)};
		db.execSQL(sql,args);
	}
	/**
	 * 插入
	 * @param list
	 */
	public void SynchronyData2DB(List<RssItem> rssItems){
		List<ContentValues> list = new ArrayList<ContentValues>();
		for(int i=0,len=rssItems.size();i<len;i++){
			ContentValues contentValues = new ContentValues();
			contentValues.put("Id",rssItems.get(i).GetId());
			contentValues.put("Title",rssItems.get(i).GetTitle());
			contentValues.put("Description",rssItems.get(i).GetDescription());
		    String link="";
		    if(rssItems.get(i).GetLink()!=null){
		    	link=rssItems.get(i).GetLink();
		    }
			contentValues.put("Link",link);
			contentValues.put("AddDate",AppUtil.ParseDateToString(rssItems.get(i).GetAddDate()));
			contentValues.put("Category",rssItems.get(i).GetCategory());
			contentValues.put("Author",rssItems.get(i).GetAuthor());
			contentValues.put("IsReaded",false);
			contentValues.put("IsDigg",rssItems.get(i).GetIsDigg());
			
			list.add(contentValues);
		}
		synchronized(_writeLock){
			db.beginTransaction();
			try{
				//清除已有
				//String where="IsFull=?";
				//String[] args={"0"};
				//db.delete(DB_NEWS_TABLE, where, args);
				for(int i=0, len=list.size(); i<len; i++){
					boolean isExist=Exist(list.get(i).getAsString("Link"));
					if(!isExist){
						db.insert(Config.DB_RSSITEM_TABLE, null, list.get(i));
					}
				}
				db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
		}
	}}
