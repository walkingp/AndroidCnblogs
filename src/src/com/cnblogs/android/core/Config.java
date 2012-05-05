package com.cnblogs.android.core;

/**
 * 配置内信息
 * 
 * @author walkingp
 * 
 */
public class Config {
	public static final String TEMP_IMAGES_LOCATION = "/sdcard/cnblogs/images/";// 临时图片文件

	public static final String CNBLOGS_URL = "http://www.cnblogs.com/";// 博客园域名
	
	public static final String DB_FILE_NAME="cnblogs_db";//数据库文件名
	public static final String APP_PACKAGE_NAME="com.cnblogs.android";//程序包名

	public static final String ENCODE_TYPE = "utf-8";// 全局编码方式

	public static final String APP_UPDATE_URL = "http://android.walkingp.com/api/update_app.ashx?alias={alias}&action=update";

	public static final int BLOG_PAGE_SIZE = 10;// 博客分页条数
	public static final String URL_GET_BLOG_LIST = "http://wcf.open.cnblogs.com/blog/sitehome/paged/{pageIndex}/{pageSize}";// 根据页码（从1开始)
	public static final String URL_GET_BLOG_DETAIL = "http://wcf.open.cnblogs.com/blog/post/body/{0}";// 根据编号取内容

	public static final String URL_48HOURS_TOP_VIEW_LIST="http://wcf.open.cnblogs.com/blog/48HoursTopViewPosts/{size}";//48小时阅读排行
	public static final int NUM_48HOURS_TOP_VIEW=20;//48小时阅读排行数据条数
	public static final String URL_TENDAYS_TOP_DIGG_LIST="http://wcf.open.cnblogs.com/blog/TenDaysTopDiggPosts/{size}";//10天内推荐排行
	public static final int NUM_TENDAYS_TOP_DIGG=20;//10天内推荐排行数据条数
	
	
	public static final int NEWS_PAGE_SIZE = 10;// 新闻分页条数
	public static final String URL_GET_NEWS_LIST = "http://wcf.open.cnblogs.com/news/recent/paged/{pageIndex}/{pageSize}";// 根据页码（从1开始)
	public static final String URL_GET_NEWS_DETAIL = "http://wcf.open.cnblogs.com/news/item/{0}";// 根据编号取内容
	
	public static final String URL_RECOMMEND_NEWS_LIST="http://wcf.open.cnblogs.com/news/recommend/paged/{pageIndex}/{pageSize}";//推荐新闻
	
	public static final int COMMENT_PAGE_SIZE = 10;// 评论分页条数
	
	public static final String URL_NEWS_GET_COMMENT_LIST = "http://wcf.open.cnblogs.com/news/item/{contentId}/comments/{pageIndex}/{pageSize}";// 得到新闻评论分页
	public static final String URL_BLOG_GET_COMMENT_LIST = "http://wcf.open.cnblogs.com/blog/post/{contentId}/comments/{pageIndex}/{pageSize}";// 得到博客评论分页
	
	public static final String URL_USER_SEARCH_AUTHOR_LIST = "http://wcf.open.cnblogs.com/blog/bloggers/search?t={username}";// 用户搜索
	
	public static final int NUM_RECOMMEND_USER=10;//推荐博客分页条数
	public static final String URL_RECOMMEND_USER_LIST="http://wcf.open.cnblogs.com/blog/bloggers/recommend/{pageIndex}/{pageSize}";//推荐博客排名
	
	public static final int BLOG_LIST_BY_AUTHOR_PAGE_SIZE = 10;// 博主文章列表分页
	public static final String URL_GET_BLOG_LIST_BY_AUTHOR = "http://wcf.open.cnblogs.com/blog/u/{author}/posts/{pageIndex}/{pageSize}";// 博主文章列表

	public static final String LOCAL_PATH = "file:///android_asset/";// 本地html
	// 新浪微博api
	public static final String consumerKey = "4216444778";
	public static final String consumerSecret = "1f6960b6dfe01c1ab71c417d29b439a8";
	public static final String callBackUrl = "myapp://AboutActivity";

	public static final String AuthorWeiboUserId = "1240794802";// 自己的新浪微博用户编号
	public static final String AuthorWeiboUserName = "walkingp";// 作者的新浪微博用户昵称

	public static final String DB_BLOG_TABLE = "BlogList";// 博客数据表名
	public static final String DB_NEWS_TABLE = "NewsList";// 新闻数据表名
	public static final String DB_COMMENT_TABLE = "CommentList";// 评论数据表名
	public static final String DB_RSSLIST_TABLE = "RssList";// 订阅博客数据表名
	public static final String DB_RSSITEM_TABLE = "RssItem";// 订阅文章数据表名
	public static final String DB_FAV_TABLE="FavList";//收藏表

	public static final boolean IS_SYNCH2DB_AFTER_READ = true;// 阅读时是否同步到数据库

	public static final String URL_RSS_CATE_URL = "http://m.walkingp.com/api/xml/cnblogs_rsscate.xml";// 备选RSS文件地址
	public static final String URL_RSS_LIST_URL = "http://m.walkingp.com/api/xml/cnblogs_rss_item_{0}.xml";// 备选RSS文件地址
}
