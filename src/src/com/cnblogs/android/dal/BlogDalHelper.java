package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.utility.AppUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BlogDalHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	public final static byte[] _writeLock = new byte[0];
	Context context;
	public BlogDalHelper(Context context) {
		dbHelper = new DBHelper.DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	public void Close(){
		dbHelper.close();
	}
	/**
	 * 判断是否已经存在
	 * 
	 * @param blogId
	 * @return
	 */
	private boolean Exist(int blogId) {
		String where = "BlogId=?";
		String[] args = {String.valueOf(blogId)};
		Cursor cursor = db.query(Config.DB_BLOG_TABLE, null, where, args, null,
				null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();
		return isExist;
	}
	/**
	 * 判断是否已经写入内容
	 * @param blogId
	 * @return
	 */
	private boolean IsFull(int blogId){
		String where="BlogId=?";
		String[] args={String.valueOf(blogId)};
		Cursor cursor = db.query(Config.DB_BLOG_TABLE, null, where, args, null,
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
	 * 得到头条
	 * 
	 * @return
	 */
	public List<Blog> GetTopBlogList() {
		String limit = "10";
		String where = "";

		return GetBlogListByWhere(limit, where, null);
	}
	/**
	 * 分页
	 */
	public List<Blog> GetBlogListByPage(int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + ","
				+ String.valueOf(pageSize);
		List<Blog> list = GetBlogListByWhere(limit, null, null);

		return list;
	}
	/**
	 * 得到某个作者的离线数据
	 * @param author
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Blog> GetBlogListByAuthor(String author,int pageIndex,int pageSize){
		String limit = String.valueOf((pageIndex - 1) * pageSize) + "," + String.valueOf(pageSize);
		String where="AuthorName=?";
		String[] args={author};
		List<Blog> list = GetBlogListByWhere(limit, where, args);
		
		return list;
	}
	/**
	 * 得到对象
	 */
	public Blog GetBlogEntity(int blogId) {
		String limit = "1";
		String where = "BlogId=?";
		String[] args = {String.valueOf(blogId)};
		List<Blog> list = GetBlogListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	/**
	 * 得到
	 * 
	 * @param top
	 * @param where
	 */
	public List<Blog> GetBlogListByWhere(String limit, String where,
			String[] args) {
		List<Blog> listBlog = new ArrayList<Blog>();
		String orderBy = "BlogID desc";
		Cursor cursor = db.query(Config.DB_BLOG_TABLE, null, where, args, null,
				null, orderBy, limit);
		while (cursor != null && cursor.moveToNext()) {
			Blog entity = new Blog();
			String addTimeStr = cursor.getString(cursor
					.getColumnIndex("Published"));
			Date addTime = AppUtil.ParseDate(addTimeStr);
			entity.SetAddTime(addTime);
			entity.SetAuthor(cursor.getString(cursor
					.getColumnIndex("AuthorName")));
			entity.SetAuthorUrl(cursor.getString(cursor
					.getColumnIndex("AuthorUrl")));
			entity.SetAvator(cursor.getString(cursor
					.getColumnIndex("AuthorAvatar")));
			entity.SetBlogContent(cursor.getString(cursor
					.getColumnIndex("Content")));
			entity.SetBlogId(cursor.getInt(cursor.getColumnIndex("BlogId")));
			entity.SetBlogTitle(cursor.getString(cursor
					.getColumnIndex("BlogTitle")));
			String blogUrl = "";
			if (cursor.getString(cursor.getColumnIndex("BlogUrl")) != null) {
				blogUrl = cursor.getString(cursor.getColumnIndex("BlogUrl"));
			}
			entity.SetBlogUrl(blogUrl);
			entity.SetCateId(cursor.getInt(cursor.getColumnIndex("CateId")));
			String cateName = "";
			if (cursor.getString(cursor.getColumnIndex("CateName")) != null) {
				cateName = cursor.getString(cursor.getColumnIndex("CateName"));
			}
			entity.SetCateName(cateName);
			entity.SetCommentNum(cursor.getInt(cursor
					.getColumnIndex("Comments")));
			entity.SetDiggsNum(cursor.getInt(cursor.getColumnIndex("Digg")));
			boolean isFull = cursor.getString(cursor.getColumnIndex("IsFull")).equals("1");
			entity.SetIsFullText(isFull);
			entity.SetSummary(cursor.getString(cursor.getColumnIndex("Summary")));
			Date updateTime = new java.util.Date();
			if (cursor.getString(cursor.getColumnIndex("Updated")) != null) {
				updateTime = AppUtil.ParseDate(cursor.getString(cursor
						.getColumnIndex("Updated")));
			}
			entity.SetUpdateTime(updateTime);
			entity.SetViewNum(cursor.getInt(cursor.getColumnIndex("View")));
			boolean isRead = cursor.getString(cursor.getColumnIndex("IsReaded")).equals("1");
			entity.SetIsReaded(isRead);
			entity.SetUserName(cursor.getString(cursor.getColumnIndex("UserName")));

			listBlog.add(entity);
		}
		cursor.close();

		return listBlog;
	}
	/**
	 * 是否已读
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean GetIsReaded(int blogId) {
		Blog entity = GetBlogEntity(blogId);
		if (entity != null) {
			return entity.GetIsReaded();
		}
		return false;
	}
	/**
	 * 标志为已读
	 * 
	 * @param blogId
	 */
	public void MarkAsReaded(int blogId) {
		String sql = "update BlogList set IsReaded=1 where BlogId=?";
		String[] args = {String.valueOf(blogId)};
		db.execSQL(sql, args);
	}
	/**
	 * 将博客内容同步到数据库
	 * 
	 * @param blogId
	 * @param blogContent
	 */
	public void SynchronyContent2DB(int blogId, String blogContent) {
		if (blogContent.equals("")) {
			return;
		}
		String sql = "update BlogList set Content=?,IsFull=1 where BlogId=?";
		String[] args = {blogContent, String.valueOf(blogId)};
		db.execSQL(sql, args);
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(List<Blog> blogList) {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0, len = blogList.size(); i < len; i++) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("BlogId", blogList.get(i).GetBlogId());
			contentValues.put("BlogTitle", blogList.get(i).GetBlogTitle());
			contentValues.put("Summary", blogList.get(i).GetSummary());
			String content = "";
			if (blogList.get(i).GetBlogContent() != null) {
				content = blogList.get(i).GetBlogContent();
			}
			contentValues.put("Content", content);
			contentValues.put("Published",
					AppUtil.ParseDateToString(blogList.get(i).GetAddTime()));
			Date datetime = new java.util.Date();
			String updateTime = "";
			if (blogList.get(i).GetUpdateTime() != null) {
				updateTime = AppUtil.ParseDateToString(blogList.get(i)
						.GetUpdateTime());
			} else {
				updateTime = AppUtil.ParseDateToString(datetime);
			}
			contentValues.put("Updated", updateTime);
			contentValues.put("AuthorName", blogList.get(i).GetAuthor());
			contentValues.put("AuthorAvatar", blogList.get(i).GetAvator());
			String authorUrl = "";
			if (blogList.get(i).GetAuthorUrl() != null) {
				authorUrl = blogList.get(i).GetAuthorUrl();
			}
			contentValues.put("AuthorUrl", authorUrl);
			contentValues.put("View", blogList.get(i).GetViewNum());
			contentValues.put("Comments", blogList.get(i).GetCommentNum());
			contentValues.put("Digg", blogList.get(i).GetDiggsNum());
			contentValues.put("IsReaded", false);
			contentValues.put("CateId", blogList.get(i).GetCateId());
			String cateName = "";
			if (blogList.get(i).GetCateName() != null) {
				cateName = blogList.get(i).GetCateName();
			}
			contentValues.put("CateName", cateName);
			contentValues.put("IsFull", blogList.get(i).GetIsFullText());
			contentValues.put("BlogUrl", blogList.get(i).GetBlogUrl());
			contentValues.put("UserName", blogList.get(i).GetUserName());

			list.add(contentValues);
		}
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				// 清除已有
				// String where="IsFull=?";
				// String[] args={"0"};
				// db.delete(DB_BLOG_TABLE, where, args);
				for (int i = 0, len = list.size(); i < len; i++) {
					int blogId=list.get(i).getAsInteger("BlogId");
					boolean isExist = Exist(blogId);
					boolean isFull = IsFull(blogId);
					if (!isExist) {
						db.insert(Config.DB_BLOG_TABLE, null, list.get(i));
					} else if (!isFull) {// 如果没有写内容
						SynchronyContent2DB(list.get(i).getAsInteger("BlogId"),
								list.get(i).getAsString("Content"));
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
}
