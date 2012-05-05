package com.cnblogs.android.dal;
import java.util.List;

import com.cnblogs.android.core.Config;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper {

	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	public final static byte[] _writeLock = new byte[0];
	// 打开数据库
	public void OpenDB(Context context) {
		dbHelper = new DatabaseHelper(context);
		db = dbHelper.getWritableDatabase();
	}
	// 关闭数据库
	public void Close() {
		dbHelper.close();
		if(db!=null){
			db.close();
		}
	}
	/**
	 * 插入
	 * 
	 * @param list
	 * @param table
	 *            表名
	 */
	public void Insert(List<ContentValues> list, String tableName) {
		synchronized (_writeLock) {
			db.beginTransaction();
			try {
				db.delete(tableName, null, null);
				for (int i = 0, len = list.size(); i < len; i++)
					db.insert(tableName, null, list.get(i));
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}
	public DBHelper(Context context) {
		this.dbHelper = new DatabaseHelper(context);
	}
	/**
	 * 用于初始化数据库
	 * 
	 * @author Administrator
	 * 
	 */
	public static class DatabaseHelper extends SQLiteOpenHelper {
		// 定义数据库文件
		private static final String DB_NAME = Config.DB_FILE_NAME;
		// 定义数据库版本
		private static final int DB_VERSION = 1;
		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			CreateBlogDb(db);
			Log.i("DBHelper", "创建BlogList表成功");
			CreateNewsDb(db);
			Log.i("DBHelper", "创建NewsList表成功");
			CreateCommentDb(db);
			Log.i("DBHelper", "创建CommentList表成功");
			CreateRssListDb(db);
			Log.i("DBHelper", "创建RssList表成功");
			CreateRssItemDb(db);
			Log.i("DBHelper", "创建RssItem表成功");
			CreateFavListDb(db);
			Log.i("DBHelper", "创建FavList表成功");			
		}
		/**
		 * 创建BlogList表
		 * 
		 * @param db
		 */
		private void CreateBlogDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [BlogList] (");
			sb.append("[BlogId] INTEGER(13) NOT NULL DEFAULT (0), ");
			sb.append("[BlogTitle] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Summary] NVARCHAR(500) NOT NULL DEFAULT (''), ");
			sb.append("[Content] NTEXT NOT NULL DEFAULT (''), ");
			sb.append("[Published] DATETIME, ");
			sb.append("[Updated] DATETIME, ");
			sb.append("[AuthorUrl] NVARCHAR(200), ");
			sb.append("[AuthorName] NVARCHAR(50), ");
			sb.append("[AuthorAvatar] NVARCHAR(200), ");
			sb.append("[View] INTEGER(16) DEFAULT (0), ");
			sb.append("[Comments] INTEGER(16) DEFAULT (0), ");
			sb.append("[Digg] INTEGER(16) DEFAULT (0), ");
			sb.append("[IsReaded] BOOLEAN DEFAULT (0), ");
			sb.append("[IsFull] BOOLEAN DEFAULT (0), ");// 是否全文
			sb.append("[BlogUrl] NVARCHAR(200), ");// 网页地址
			sb.append("[UserName] NVARCHAR(50), ");// 用户名
			sb.append("[CateId] INTEGER(16), ");
			sb.append("[CateName] NVARCHAR(16))");

			db.execSQL(sb.toString());
		}
		/**
		 * 创建NewsList表
		 * 
		 * @param db
		 */
		private void CreateNewsDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [NewsList] (");
			sb.append("[NewsId] INTEGER(13) NOT NULL DEFAULT (0), ");
			sb.append("[NewsTitle] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Summary] NVARCHAR(500) NOT NULL DEFAULT (''), ");
			sb.append("[Content] NTEXT NOT NULL DEFAULT (''), ");
			sb.append("[Published] DATETIME, ");
			sb.append("[Updated] DATETIME, ");
			sb.append("[View] INTEGER(16) DEFAULT (0), ");
			sb.append("[Comments] INTEGER(16) DEFAULT (0), ");
			sb.append("[Digg] INTEGER(16) DEFAULT (0), ");
			sb.append("[IsReaded] BOOLEAN DEFAULT (0), ");
			sb.append("[IsFull] BOOLEAN DEFAULT (0), ");
			sb.append("[CateId] INTEGER(16), ");
			sb.append("[NewsUrl] NVARCHAR(200), ");// 网页地址
			sb.append("[CateName] NVARCHAR(16))");

			db.execSQL(sb.toString());
		}
		/**
		 * 创建评论CommentList表
		 * 
		 * @param db
		 */
		private void CreateCommentDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [CommentList] (");
			sb.append("[CommentId] INTEGER NOT NULL DEFAULT (0), ");
			sb.append("[PostUserUrl] NVARCHAR(200) NOT NULL DEFAULT (''), ");
			sb.append("[PostUserName] NVARCHAR(50) NOT NULL DEFAULT (''), ");
			sb.append("[Content] NTEXT NOT NULL DEFAULT (''), ");
			sb.append("[ContentId] INTEGER NOT NULL DEFAULT (0), ");
			sb.append("[CommentType] INTEGER DEFAULT (0), ");
			sb.append("[AddTime] DATETIME);");
			db.execSQL(sb.toString());
		}
		/**
		 * 创建订阅博客RssList表
		 * 
		 * @param db
		 */
		private void CreateRssListDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [RssList] (");
			sb.append("[RssId] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,");
			sb.append("[Title] NVARCHAR(50) NOT NULL DEFAULT (''),");
			sb.append("[Link] NVARCHAR(500) NOT NULL DEFAULT (''), ");
			sb.append("[Description] NVARCHAR(500) DEFAULT (''),");
			sb.append("[AddTime] DATETIME DEFAULT (date('now')), ");
			sb.append("[OrderNum] INTEGER DEFAULT (0),");
			sb.append("[RssNum] INTEGER DEFAULT (0),");
			sb.append("[Guid] NVARCHAR(500),");
			sb.append("[IsCnblogs] BOOLEAN DEFAULT (0),");
			sb.append("[Image] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Updated] DATETIME DEFAULT (date('now')),");
			sb.append("[Author] NVARCHAR(50) DEFAULT (''),");
			sb.append("[CateId] INTEGER,");
			sb.append("[CateName] NVARCHAR DEFAULT (''),");
			sb.append("[IsActive] BOOLEAN DEFAULT (1));");
			sb.append(");");
			db.execSQL(sb.toString());
		}
		/**
		 * 创建订阅文章RssItem表
		 * 
		 * @param db
		 */
		private void CreateRssItemDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [RssItem] (");
			sb.append("[Id] INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append("[Title] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Link] NVARCHAR(200) DEFAULT (''),");
			sb.append("[Description] NTEXT DEFAULT (''),");
			sb.append("[Category] NVARCHAR(50),");
			sb.append("[Author] NVARCHAR(50) DEFAULT (''),");
			sb.append("[AddDate] DATETIME,");
			sb.append("[IsReaded] BOOLEAN DEFAULT (0),");
			sb.append("[IsDigg] BOOLEAN DEFAULT (0));");
			db.execSQL(sb.toString());
		}
		/**
		 * 创建收藏表FavList
		 * @param db
		 */
		private void CreateFavListDb(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE TABLE [FavList] (");
			sb.append("[FavId] INTEGER PRIMARY KEY AUTOINCREMENT,");
			sb.append("[AddTime] DATETIME NOT NULL DEFAULT (date('now')), ");
			sb.append("[ContentType] INTEGER NOT NULL DEFAULT (0),");
			sb.append("[ContentId] INTEGER NOT NULL DEFAULT (0));");
			db.execSQL(sb.toString());
		}
		/**
		 * 更新版本时更新表
		 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			DropTable(db);
			onCreate(db);
			Log.e("User", "onUpgrade");
		}
		/**
		 * 删除表
		 * 
		 * @param db
		 */
		private void DropTable(SQLiteDatabase db) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP TABLE IF EXISTS " + Config.DB_BLOG_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_NEWS_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_COMMENT_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_RSSLIST_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_RSSITEM_TABLE + ";");
			sb.append("DROP TABLE IF EXISTS " + Config.DB_FAV_TABLE + ";");
			db.execSQL(sb.toString());
		}
		/**
		 * 清空数据表（仅清空无用数据）
		 * @param db
		 */
		public static void ClearData(Context context){
			DatabaseHelper dbHelper = new DBHelper.DatabaseHelper(context);
			SQLiteDatabase db=dbHelper.getWritableDatabase();
			StringBuilder sb=new StringBuilder();
			sb.append("DELETE FROM BlogList WHERE IsFull=0 AND BlogId NOT IN(SELECT ContentId FROM FavList WHERE ContentType=0);");//清空博客表
			sb.append("DELETE FROM NewsList WHERE IsFull=0;");//清空新闻表
			sb.append("DELETE FROM CommentList;");//清空评论表
			sb.append("DELETE FROM RssItem;");//清空订阅文章表
			db.execSQL(sb.toString());
		}
	}
}
