package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.FavList;
import com.cnblogs.android.enums.EnumResultType;
import com.cnblogs.android.utility.AppUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class FavDalHelper {
	private DBHelper.DatabaseHelper		dbHelper;
	private SQLiteDatabase		db;
	public final static byte[] _writeLock = new byte[0];
	
	public FavDalHelper(Context context){
		dbHelper=new DBHelper.DatabaseHelper(context);
		db=dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 判断是否已经存在
	 * @param commentId
	 * @return
	 */
	private boolean Exist(int contentId,FavList.EnumContentType contentType){
		String where="ContentId=? and ContentType=?";
		String[] args={String.valueOf(contentId),String.valueOf(contentType.ordinal())};
		Cursor cursor= db.query(Config.DB_FAV_TABLE, null, where, args, null, null, null);
		boolean isExist= cursor!=null && cursor.moveToNext();
		cursor.close();
		
		return isExist;
	}
	/*
	 * 分页
	 */
	public List<FavList> GetFavListByPage(int pageIndex,int pageSize,FavList.EnumContentType contentType){
		String where="ContentType=?";
		String[] args={String.valueOf(contentType.ordinal())};
		String limit= String.valueOf((pageIndex-1)*pageSize) + "," + String.valueOf(pageSize);
		List<FavList> list=GetFavListByWhere(limit,where,args);
		
		return list;
	}
	/*
	 * 得到对象
	 */
	public FavList GetFavEntity(int favId){
		String limit="1";
		String where="FavId=?";
		String[] args={String.valueOf(favId)};
		List<FavList> list=GetFavListByWhere(limit,where,args);
		if(list.size()>0){
			return list.get(0);
		}
		
		return null;
	}
	/*
	 * 得到对象
	 */
	public FavList GetFavEntity(int contentId,FavList.EnumContentType contentType){
		String limit="1";
		String where="ContentId=? and ContentType=?";
		String[] args={String.valueOf(contentId),String.valueOf(contentType.ordinal())};
		List<FavList> list=GetFavListByWhere(limit,where,args);
		if(list.size()>0){
			return list.get(0);
		}
		
		return null;
	}
	/**
	 * 得到
	 * @param top
	 * @param where
	 */
	public List<FavList> GetFavListByWhere(String limit,String where,String[] args){
		List<FavList> listFav=new ArrayList<FavList>();
		String orderBy="FavID desc";
		Cursor cursor=db.query(Config.DB_FAV_TABLE, null, where, args, null, null, orderBy,limit);
		while(cursor!=null && cursor.moveToNext()){
			FavList entity=new FavList();
			String addTimeStr=cursor.getString(cursor.getColumnIndex("AddTime"));
			Date addTime=AppUtil.ParseDate(addTimeStr);
			entity.SetAddTime(addTime);
			entity.SetFavId(cursor.getInt(cursor.getColumnIndex("FavId")));
			entity.SetContentType(FavList.EnumContentType.values()[cursor.getInt(cursor.getColumnIndex("ContentType"))]);
			entity.SetContentId(cursor.getInt(cursor.getColumnIndex("ContentId")));
			
			listFav.add(entity);
		}
		cursor.close();
		
		return listFav;
	}
	/**
	 * 插入
	 * @param list
	 */
	public EnumResultType.EnumActionResultType SynchronyData2DB(List<FavList> favList){
		List<ContentValues> list = new ArrayList<ContentValues>();
		for(int i=0,len=favList.size();i<len;i++){
			ContentValues contentValues = new ContentValues();
			contentValues.put("ContentId",favList.get(i).GetContentId());
			contentValues.put("ContentType",favList.get(i).GetContentType().ordinal());
			contentValues.put("AddTime",AppUtil.ParseDateToString(favList.get(i).GetAddTime()));
			
			list.add(contentValues);
		}
		synchronized(_writeLock){
			db.beginTransaction();
			try{
				for(int i=0, len=list.size(); i<len; i++){
					boolean isExist=Exist(favList.get(i).GetContentId(),favList.get(i).GetContentType());
					if(isExist){
						return EnumResultType.EnumActionResultType.Exist;
					}
					db.insert(Config.DB_FAV_TABLE, null, list.get(i));
				}
				db.setTransactionSuccessful();
				return EnumResultType.EnumActionResultType.Succ;
			}catch(Exception ex){
				Log.e("fav_insert", ex.getMessage());
				return EnumResultType.EnumActionResultType.Fail;
			}finally{
				db.endTransaction();
			}
		}
	}
	/**
	 * 删除 
	 * @param favId
	 */
	public boolean Delete(int favId) {
		String where = "FavId=?";
		String[] args = {String.valueOf(favId)};
		try{
			db.delete(Config.DB_FAV_TABLE, where, args);
			return true;
		}catch(Exception ex){
			Log.e("fav_delete", ex.getMessage());
			return false;
		}
	}
	/**
	 * 删除 
	 * @param favId
	 */
	public boolean Delete(int contentId,FavList.EnumContentType contentType) {
		String where = "ContentId=? and ContentType=?";
		String[] args = {String.valueOf(contentId),String.valueOf(contentType.ordinal())};
		try{
			db.delete(Config.DB_FAV_TABLE, where, args);
			return true;
		}catch(Exception ex){
			Log.e("fav_delete", ex.getMessage());
			return false;
		}
	}
}
