package com.cnblogs.android.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cnblogs.android.core.Config;
import com.cnblogs.android.entity.RssList;
import com.cnblogs.android.utility.AppUtil;

public class RssListDalHelper {
	private DBHelper.DatabaseHelper dbHelper;
	private SQLiteDatabase db;
	public final static byte[] _writeLock = new byte[0];

	public RssListDalHelper(Context context) {
		dbHelper = new DBHelper.DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	/**
	 * 判断是否已经存在
	 * 
	 * @param blogId
	 * @return
	 */
	public boolean Exist(String link) {
		String where = "Link=?";
		String[] args = {link};
		Cursor cursor = db.query(Config.DB_RSSLIST_TABLE, null, where, args,
				null, null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();

		return isExist;
	}
	/**
	 * 根据用户名（博客园判断）
	 * @param author
	 * @return
	 */
	public boolean ExistByAuthorName(String author){
		String where="Author=?";
		String[] args={author};
		Cursor cursor = db.query(Config.DB_RSSLIST_TABLE, null, where, args,
				null, null, null);
		boolean isExist = cursor != null && cursor.moveToNext();
		cursor.close();

		return isExist;
	}
	/**
	 * 得到头条
	 * 
	 * @return
	 */
	public List<RssList> GetRssList() {
		String where = "IsActive=?";
		String[] args = {"1"};

		return GetRssListByWhere(null, where, args);
	}
	/*
	 * 分页
	 */
	public List<RssList> GetRssListByPage(int pageIndex, int pageSize) {
		String limit = String.valueOf((pageIndex - 1) * pageSize) + ","
				+ String.valueOf(pageSize);
		List<RssList> list = GetRssListByWhere(limit, null, null);

		return list;
	}
	/*
	 * 得到对象
	 */
	public RssList GetRssListEntity(String link) {
		String limit = "1";
		String where = "Link=?";
		String[] args = {link};
		List<RssList> list = GetRssListByWhere(limit, where, args);
		if (list.size() > 0) {
			return list.get(0);
		}

		return null;
	}
	/**
	 * 根据条件得到
	 * 
	 * @param top
	 * @param where
	 */
	public List<RssList> GetRssListByWhere(String limit, String where,
			String[] args) {
		List<RssList> listRss = new ArrayList<RssList>();
		String orderBy = "AddTime asc";
		Cursor cursor = db.query(Config.DB_RSSLIST_TABLE, null, where, args,
				null, null, orderBy, limit);
		while (cursor != null && cursor.moveToNext()) {
			RssList entity = new RssList();
			String addTimeStr = cursor.getString(cursor
					.getColumnIndex("AddTime"));
			Date addTime = AppUtil.ParseDate(addTimeStr);
			entity.SetAddTime(addTime);
			String updateTimeStr = cursor.getString(cursor
					.getColumnIndex("Updated"));
			Date updated = AppUtil.ParseDate(updateTimeStr);
			entity.SetUpdated(updated);
			entity.SetTitle(cursor.getString(cursor.getColumnIndex("Title")));
			entity.SetLink(cursor.getString(cursor.getColumnIndex("Link")));
			entity.SetRssId(cursor.getInt(cursor.getColumnIndex("RssId")));
			String description = "";
			if (cursor.getString(cursor.getColumnIndex("Description")) != null) {
				description = cursor.getString(cursor
						.getColumnIndex("Description"));
			}
			entity.SetDescription(description);
			entity.SetOrderNum(cursor.getInt(cursor.getColumnIndex("OrderNum")));
			entity.SetRssNum(cursor.getInt(cursor.getColumnIndex("RssNum")));
			entity.SetIsCnblogs(cursor.getString(cursor.getColumnIndex("IsCnblogs")).equals("1"));
			entity.SetImage(cursor.getString(cursor.getColumnIndex("Image")));
			entity.SetCateId(cursor.getInt(cursor.getColumnIndex("CateId")));
			boolean isActive = cursor.getString(
					cursor.getColumnIndex("IsActive")).equals("1");
			entity.SetCateName(cursor.getString(cursor
					.getColumnIndex("CateName")));
			entity.SetGuid(cursor.getString(cursor.getColumnIndex("Guid")));
			entity.SetAuthor(cursor.getString(cursor.getColumnIndex("Author")));
			entity.SetIsActive(isActive);

			listRss.add(entity);
		}
		cursor.close();

		return listRss;
	}
	/**
	 * 删除
	 * 
	 * @param link
	 */
	public void Delete(String link) {
		String where = "Link=?";
		String[] args = {link};
		db.delete(Config.DB_RSSLIST_TABLE, where, args);
	}
	/**
	 * 插入数据库
	 * 
	 * @param entity
	 */
	public void Insert(RssList entity) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("Title", entity.GetTitle());
		contentValues.put("Link", entity.GetLink());
		contentValues.put("Guid", entity.GetGuid());
		String description = "";
		if (entity.GetDescription() != null) {
			description = entity.GetDescription();
		}
		contentValues.put("Description", description);
		// contentValues.put("AddTime",AppUtil.ParseDateToString(entity.GetAddTime()));
		// contentValues.put("Updated",AppUtil.ParseDateToString(entity.GetUpdated()));
		contentValues.put("Image", entity.GetImage());
		contentValues.put("IsCnblogs", entity.GetIsCnblogs());
		contentValues.put("Author", entity.GetAuthor());
		contentValues.put("CateId", entity.GetCateId());
		contentValues.put("CateName", entity.GetCateName());
		contentValues.put("IsActive", entity.GetIsActive());

		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				boolean isExist = Exist(contentValues.getAsString("Link"));
				if (!isExist) {
					db.insert(Config.DB_RSSLIST_TABLE, null, contentValues);
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	/**
	 * 插入
	 * 
	 * @param list
	 */
	public void SynchronyData2DB(List<RssList> listRss) {
		List<ContentValues> list = new ArrayList<ContentValues>();
		for (int i = 0, len = listRss.size(); i < len; i++) {
			ContentValues contentValues = new ContentValues();
			// contentValues.put("RssId",listRss.get(i).GetRssId());
			contentValues.put("Title", listRss.get(i).GetTitle());
			contentValues.put("Link", listRss.get(i).GetLink());
			contentValues.put("Guid", listRss.get(i).GetGuid());
			String description = "";
			if (listRss.get(i).GetDescription() != null) {
				description = listRss.get(i).GetDescription();
			}
			contentValues.put("Description", description);
			contentValues.put("AddTime",
					AppUtil.ParseDateToString(listRss.get(i).GetAddTime()));
			contentValues.put("Updated",
					AppUtil.ParseDateToString(listRss.get(i).GetUpdated()));
			contentValues.put("Image", listRss.get(i).GetImage());
			contentValues.put("IsCnblogs", listRss.get(i).GetIsCnblogs());
			contentValues.put("Author", listRss.get(i).GetAuthor());
			contentValues.put("CateId", listRss.get(i).GetCateId());
			contentValues.put("CateName", listRss.get(i).GetCateName());
			contentValues.put("IsActive", listRss.get(i).GetIsActive());

			list.add(contentValues);
		}
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				// 清除已有
				// String where="IsFull=?";
				// String[] args={"0"};
				// db.delete(DB_NEWS_TABLE, where, args);
				for (int i = 0, len = list.size(); i < len; i++) {
					boolean isExist = Exist(list.get(i).getAsString("Link"));
					if (!isExist) {
						db.insert(Config.DB_RSSLIST_TABLE, null, list.get(i));
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
}
