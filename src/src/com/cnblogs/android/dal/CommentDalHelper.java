package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.utility.AppUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CommentDalHelper {
	private DBHelper.DatabaseHelper		dbHelper;
	private SQLiteDatabase		db;
	public final static byte[] _writeLock = new byte[0];
	
	public CommentDalHelper(Context context){
		dbHelper=new DBHelper.DatabaseHelper(context);
		db=dbHelper.getWritableDatabase();
	}
	/**
	 * 判断是否已经存在
	 * @param commentId
	 * @return
	 */
	private boolean Exist(int commentId,Comment.EnumCommentType commentType){
		String where="CommentId=? and CommentType=?";
		String[] args={String.valueOf(commentId),String.valueOf(commentType.ordinal())};
		Cursor cursor= db.query(Config.DB_COMMENT_TABLE, null, where, args, null, null, null);
		boolean isExist= cursor!=null && cursor.moveToNext();
		cursor.close();
		
		return isExist;
	}
	/*
	 * 分页
	 */
	public List<Comment> GetCommentListByPage(int pageIndex,int pageSize,int contentId,Comment.EnumCommentType commentType){
		String where="ContentId=? and CommentType=?";
		String[] args={String.valueOf(contentId),String.valueOf(commentType.ordinal())};
		String limit= String.valueOf((pageIndex-1)*pageSize) + "," + String.valueOf(pageSize);
		List<Comment> list=GetCommentListByWhere(limit,where,args);
		
		return list;
	}
	/*
	 * 得到对象
	 */
	public Comment GetCommentEntity(int contentId){
		String limit="1";
		String where="CommentId=?";
		String[] args={String.valueOf(contentId)};
		List<Comment> list=GetCommentListByWhere(limit,where,args);
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
	public List<Comment> GetCommentListByWhere(String limit,String where,String[] args){
		List<Comment> listComment=new ArrayList<Comment>();
		String orderBy="CommentID desc";
		Cursor cursor=db.query(Config.DB_COMMENT_TABLE, null, where, args, null, null, orderBy,limit);
		while(cursor!=null && cursor.moveToNext()){
			Comment entity=new Comment();
			String addTimeStr=cursor.getString(cursor.getColumnIndex("AddTime"));
			Date addTime=AppUtil.ParseDate(addTimeStr);
			entity.SetAddTime(addTime);
			entity.SetCommentId(cursor.getInt(cursor.getColumnIndex("CommentId")));
			entity.SetContent(cursor.getString(cursor.getColumnIndex("Content")));
			entity.SetPostUserName(cursor.getString(cursor.getColumnIndex("PostUserName")));
			entity.SetPostUserUrl(cursor.getString(cursor.getColumnIndex("PostUserUrl")));
			entity.SetContentId(cursor.getInt(cursor.getColumnIndex("ContentId")));

			listComment.add(entity);
		}
		cursor.close();
		
		return listComment;
	}
	/**
	 * 插入
	 * @param list
	 */
	public void SynchronyData2DB(List<Comment> commentList){
		List<ContentValues> list = new ArrayList<ContentValues>();
		for(int i=0,len=commentList.size();i<len;i++){
			ContentValues contentValues = new ContentValues();
			contentValues.put("CommentId",commentList.get(i).GetCommentId());
			contentValues.put("PostUserUrl",commentList.get(i).GetPostUserUrl());
			contentValues.put("PostUserName",commentList.get(i).GetPostUserName());
		    String content="";
		    if(commentList.get(i).GetContent()!=null){
		    	content=commentList.get(i).GetContent();
		    }
			contentValues.put("Content",content);
			contentValues.put("AddTime",AppUtil.ParseDateToString(commentList.get(i).GetAddTime()));
			contentValues.put("CommentType",commentList.get(i).GetCommentType().ordinal());
			contentValues.put("ContentId",commentList.get(i).GetContentId());
			
			list.add(contentValues);
		}
		synchronized(_writeLock){
			db.beginTransaction();
			try{
				for(int i=0, len=list.size(); i<len; i++){
					boolean isExist=Exist(commentList.get(i).GetCommentId(),commentList.get(i).GetCommentType());
					if(!isExist){
						db.insert(Config.DB_COMMENT_TABLE, null, list.get(i));
					}
				}
				db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
		}
	}
}
