package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.News;
import com.cnblogs.android.utility.AppUtil;

public class NewsDalHelper {
	private DBHelper.DatabaseHelper		dbHelper;
	private SQLiteDatabase		db;
	public final static byte[] _writeLock = new byte[0];
	
	public NewsDalHelper(Context context){
		dbHelper=new DBHelper.DatabaseHelper(context);
		db=dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 判断是否已经存在
	 * @param blogId
	 * @return
	 */
	private boolean Exist(int newsId){
		String where="NewsId=" + newsId;
		Cursor cursor= db.query(Config.DB_NEWS_TABLE, null, where, null, null, null, null);
		boolean isExist= cursor!=null && cursor.moveToNext();
		cursor.close();
		
		return isExist;
	}
	/**
	 * 得到头条
	 * @return
	 */
	public List<News> GetTopNewsList(){
		String limit="10";
		String where="IsFull=?";
		String[] args={"0"};
		
		return GetNewsListByWhere(limit, where, args);
	}
	/*
	 * 分页
	 */
	public List<News> GetNewsListByPage(int pageIndex,int pageSize){
		String limit= String.valueOf((pageIndex-1)*pageSize) + "," + String.valueOf(pageSize);
		List<News> list=GetNewsListByWhere(limit,null,null);
		
		return list;
	}
	/*
	 * 得到对象
	 */
	public News GetNewsEntity(int newsId){
		String limit="1";
		String where="NewsId=?";
		String[] args={String.valueOf(newsId)};
		List<News> list=GetNewsListByWhere(limit,where,args);
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
	public List<News> GetNewsListByWhere(String limit,String where,String[] args){
		List<News> listNews=new ArrayList<News>();
		String orderBy="NewsID desc";
		Cursor cursor=db.query(Config.DB_NEWS_TABLE, null, where, args, null, null, orderBy,limit);
		while(cursor!=null && cursor.moveToNext()){
			News entity=new News();
			String addTimeStr=cursor.getString(cursor.getColumnIndex("Published"));
			Date addTime=AppUtil.ParseDate(addTimeStr);
			entity.SetAddTime(addTime);		
			entity.SetNewsContent(cursor.getString(cursor.getColumnIndex("Content")));
			entity.SetNewsTitle(cursor.getString(cursor.getColumnIndex("NewsTitle")));
			entity.SetNewsId(cursor.getInt(cursor.getColumnIndex("NewsId")));
			String blogUrl="";
			if(cursor.getString(cursor.getColumnIndex("NewsUrl"))!=null){
				blogUrl=cursor.getString(cursor.getColumnIndex("NewsUrl"));
			}
			entity.SetNewsUrl(blogUrl);
			entity.SetCommentNum(cursor.getInt(cursor.getColumnIndex("Comments")));
			entity.SetDiggsNum(cursor.getInt(cursor.getColumnIndex("Digg")));
			boolean isFull = cursor.getString(cursor.getColumnIndex("IsFull")).equals("1");
			entity.SetIsFullText(isFull);
			entity.SetSummary(cursor.getString(cursor.getColumnIndex("Summary")));
			entity.SetViewNum(cursor.getInt(cursor.getColumnIndex("View")));
			boolean isRead=cursor.getString(cursor.getColumnIndex("IsReaded")).equals("1");
			entity.SetIsReaded(isRead);
			
			listNews.add(entity);
		}
		cursor.close();
		
		return listNews;
	}
	/**
	 * 是否已读
	 * @param blogId
	 * @return
	 */
	public boolean GetIsReaded(int newsId){
		News entity=GetNewsEntity(newsId);
		if(entity!=null){
			return entity.GetIsReaded();
		}
		return false;
	}
	/**
	 * 判断是否已经写入内容
	 * @param blogId
	 * @return
	 */
	private boolean IsFull(int newsId){
		String where="NewsId=?";
		String[] args={String.valueOf(newsId)};
		Cursor cursor = db.query(Config.DB_NEWS_TABLE, null, where, args, null,
				null, null);
		if(!cursor.moveToNext() || cursor.getColumnIndex("IsFull")<0){
			cursor.close();
			return false;
		}
		boolean isFull=cursor.getString(cursor.getColumnIndex("IsFull")).equals("1");
		cursor.close();
		
		return isFull;
	}
	/**
	 * 将新闻内容同步到数据库
	 * @param newsId
	 * @param newsContent
	 */
	public void SynchronyContent2DB(int newsId,String newsContent){
		if(newsContent.equals("")){
			return;
		}
		String sql="update NewsList set Content=?,IsFull=1 where NewsId=?";
		String[] args={newsContent,String.valueOf(newsId)};
		db.execSQL(sql,args);
	}
	/**
	 * 标志为已读
	 * @param blogId
	 */
	public void MarkAsReaded(int newsId){
		String sql="update NewsList set IsReaded=1 where NewsId=?";
		String[] args={String.valueOf(newsId)};
		db.execSQL(sql,args);
	}
	/**
	 * 插入
	 * @param list
	 */
	public void SynchronyData2DB(List<News> newsList){
		List<ContentValues> list = new ArrayList<ContentValues>();
		for(int i=0,len=newsList.size();i<len;i++){
			ContentValues contentValues = new ContentValues();
			contentValues.put("NewsId",newsList.get(i).GetNewsId());
			contentValues.put("NewsTitle",newsList.get(i).GetNewsTitle());
			contentValues.put("Summary",newsList.get(i).GetSummary());
		    String content="";
		    if(newsList.get(i).GetNewsContent()!=null){
		    	content=newsList.get(i).GetNewsContent();
		    }
			contentValues.put("Content",content);
			contentValues.put("Published",AppUtil.ParseDateToString(newsList.get(i).GetAddTime()));
			contentValues.put("View",newsList.get(i).GetViewNum());
			contentValues.put("Comments",newsList.get(i).GetCommentNum());
			contentValues.put("Digg",newsList.get(i).GetDiggsNum());
			contentValues.put("IsReaded",false);
			contentValues.put("IsFull",newsList.get(i).GetIsFullText());
			contentValues.put("NewsUrl", newsList.get(i).GetNewsUrl());
			
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
					int newsId=list.get(i).getAsInteger("NewsId");
					boolean isExist=Exist(newsId);
					boolean isFull=IsFull(newsId);
					if(!isExist){
						db.insert(Config.DB_NEWS_TABLE, null, list.get(i));
					}else if(!isFull){//
						SynchronyContent2DB(list.get(i).getAsInteger("NewsId"),list.get(i).getAsString("Content"));
					}
				}
				db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
		}
	}}
