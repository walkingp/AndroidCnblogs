package com.cnblogs.android.services;

import java.util.ArrayList;
import java.util.List;

import com.cnblogs.android.MainActivity;
import com.cnblogs.android.R;
import com.cnblogs.android.cache.ImageCacher;
import com.cnblogs.android.core.BlogHelper;
import com.cnblogs.android.core.CommentHelper;
import com.cnblogs.android.core.Config;
import com.cnblogs.android.core.NewsHelper;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.dal.CommentDalHelper;
import com.cnblogs.android.dal.NewsDalHelper;
import com.cnblogs.android.entity.Blog;
import com.cnblogs.android.entity.Comment;
import com.cnblogs.android.entity.News;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class DownloadServices extends Service{
	//通知栏
	private NotificationManager downloadNotifMg;
	private Notification downloadNotify;
	
	public static final int INIT_DOWNLOAD_NOTIFY = 10010;
	/*
	 * 数据类型
	 */
	public enum EnumDataType{
		Blog,//博客
		News,//新闻 
		AuthorBlog,//某一位作者的博客
		BlogAndNews//博客和新闻
	}
	@Override
	public void onCreate() {
		downloadNotifMg = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		downloadNotify = new Notification();
		downloadNotify.contentView =new RemoteViews(getPackageName(),R.layout.offline_download_notification);
		downloadNotify.contentView.setViewVisibility(R.id.notify_download_done, View.GONE);
		downloadNotify.icon = android.R.drawable.stat_sys_download;
		downloadNotify.tickerText="博客园开始离线下载";
	    
	    Intent i = new Intent(this,MainActivity.class);
	    i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
	    PendingIntent pendingIntent = PendingIntent.getActivity(DownloadServices.this, 0, i, 0);
	    downloadNotify.contentIntent = pendingIntent;
	    downloadNotifMg.notify(INIT_DOWNLOAD_NOTIFY,downloadNotify);
		super.onCreate();
	}
	/*
	 * 服务开始
	 * @see android.app.Service#onStart(android.content.Intent, int)
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		int dataType=intent.getIntExtra("type",1);//默认下载类型博客
		String author=intent.getStringExtra("author");//如果是下载某一个人的博客
		
		int top=intent.getIntExtra("size",10);//默认下载条数；如果是下载某一个人博客，则为其全部博客文章数量
		new DownloadTask(author).execute(top,dataType);
	}
	/**
	 * 开始下载博客
	 */
	class DownloadTask extends AsyncTask<Integer, Integer, Integer>{
		int doneBlogNum=0;//总条数
		int doneNewsNum=0;
		String author;//用户
		String currentText="";//提示文字
		ImageCacher imageCacher=new ImageCacher(getApplicationContext());//下载图片
		
		public DownloadTask(String author){
			this.author=author;
		}
		protected Integer doInBackground(Integer... params) {
			
			int top=params[0];
			int dataType=params[1];
			boolean isDownBlog=false;
			boolean isDownNews=false;
			boolean isDownAuthor=false;
			if(dataType==0){//下载博客
				isDownBlog=true;
			}else if(dataType==1){//下载新闻
				isDownNews=true;
			}else if(dataType==2){
				isDownAuthor=true;
			}else{
				isDownBlog=true;
				isDownNews=true;
			}
			
			//下载Blog(一般情况为同时下载博客和新闻，单独下载用户博客)
			if(isDownBlog || isDownAuthor){
				currentText="开始下载博客内容";
				Log.i("downloadservices","开始下载Blog");
				int pageSize=top/Config.BLOG_PAGE_SIZE;
				int lastNum=pageSize%Config.BLOG_PAGE_SIZE;
				
				currentText="正在下载博客页索引";
				List<Blog> listBlogs=new ArrayList<Blog>();
				//下载前几页
				for(int i=0;i<pageSize;i++){
					List<Blog> list=new ArrayList<Blog>();
					if(isDownBlog){
						list=BlogHelper.GetBlogList(i+1);
					}else if(isDownAuthor){
						list=BlogHelper.GetAuthorBlogList(author, i+1);
					}
					
					if(list==null || list.size()==0){
						break;
					}
					listBlogs.addAll(list);
					
					//进度
					int percent=i*100/pageSize;
					publishProgress(percent);
				}
				
				Log.i("downloadservices","下载索引结束");
				//下载剩余内容
				if(top%Config.BLOG_PAGE_SIZE>0){
					List<Blog> list=new ArrayList<Blog>();//下载最后一页
					if(isDownBlog){
						list=BlogHelper.GetBlogList(pageSize+1);
					}else if(isDownAuthor){
						list=BlogHelper.GetAuthorBlogList(author, pageSize+1);
					}
					int size=list.size();
					for(int i=0;i<size;i++){
						//进度
						int percent=i*100/size;
						publishProgress(percent);
						
						listBlogs.add(list.get(i));
						if(list.get(i).GetBlogId()==lastNum){
							break;
						}
					}
				}
				currentText="开始下载博客内容";
				Log.i("downloadservices","开始下载内容");
				//内容
				int size=listBlogs.size();
				int[] blogIdArray = new int[size];
				for(int i=0;i<size;i++){
					blogIdArray[i]=listBlogs.get(i).GetBlogId();
					//进度
					int percent=i*100/size;
					publishProgress(percent);
					
					String content=BlogHelper.GetBlogContentByIdWithNet(listBlogs.get(i).GetBlogId());

					//下载作者头像
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.Avatar, content);
					//下载博客内的图片
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.Blog, content);
					
					//格式化内容，使图片地址为本地路径
					content=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.Blog, content);
					listBlogs.get(i).SetBlogContent(content);
					listBlogs.get(i).SetIsFullText(true);
					
					//下载评论
					List<Comment> listComment=CommentHelper.GetCommentList(listBlogs.get(i).GetBlogId(),
							Comment.EnumCommentType.Blog, listBlogs.get(i).GetCommentNum());
					CommentDalHelper comentDalHelper=new CommentDalHelper(getApplicationContext());
					comentDalHelper.SynchronyData2DB(listComment);
					
					doneBlogNum++;
					currentText="下载(" + (i+1) + "/" + size + ")：" + listBlogs.get(i).GetBlogTitle();
				}
				BlogDalHelper helper=new BlogDalHelper(getApplicationContext());
				helper.SynchronyData2DB(listBlogs);
				Log.i("downloadservices","下载内容结束");
				currentText="博客内容下载完成";
				// 广播
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putIntArray("blogIdArray", blogIdArray);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_bloglist");
				sendBroadcast(intent);
			}
			//下载新闻
			if(isDownNews){
				currentText="开始下载新闻内容";
				Log.i("downloadservices","开始下载News");
				int pageSize=top/Config.NEWS_PAGE_SIZE;
				int lastNum=pageSize%Config.NEWS_PAGE_SIZE;
				
				currentText="正在下载新闻页索引";
				List<News> listNews=new ArrayList<News>();
				//下载前几页
				for(int i=0;i<pageSize;i++){
					List<News> list=NewsHelper.GetNewsList(i+1);
					
					if(list==null || list.size()==0){
						break;
					}
					listNews.addAll(list);
					
					//进度
					int percent=i*100/pageSize;
					publishProgress(percent);
				}
				Log.i("downloadservices","下载索引结束");
				//下载剩余内容
				if(top%Config.NEWS_PAGE_SIZE>0){
					List<News> list=NewsHelper.GetNewsList(pageSize+1);//下载最后一页
					int size=list.size();
					for(int i=0;i<size;i++){
						//进度
						int percent=i*100/size;
						publishProgress(percent);
						
						listNews.add(list.get(i));
						if(list.get(i).GetNewsId()==lastNum){
							break;
						}
					}
				}
				currentText="开始下载新闻内容";
				Log.i("downloadservices","开始下载内容");
				//内容
				int size=listNews.size();
				int[] newsIdArray = new int[size];
				for(int i=0;i<size;i++){
					newsIdArray[i]=listNews.get(i).GetNewsId();
					//进度
					int percent=i*100/size;
					publishProgress(percent);
					
					String content=NewsHelper.GetNewsContentByIdWithNet(listNews.get(i).GetNewsId());

					//下载图片
					imageCacher.DownloadHtmlImage(ImageCacher.EnumImageType.News, content);
					
					//格式化内容，使图片地址为本地路径
					content=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.News, content);					
					listNews.get(i).SetNewsContent(content);
					listNews.get(i).SetIsFullText(true);

					//下载评论
					List<Comment> listComment=CommentHelper.GetCommentList(listNews.get(i).GetNewsId(),
							Comment.EnumCommentType.News, listNews.get(i).GetCommentNum());
					CommentDalHelper comentDalHelper=new CommentDalHelper(getApplicationContext());
					comentDalHelper.SynchronyData2DB(listComment);
					
					currentText="下载(" + (i+1) + "/" + size + ")：" + listNews.get(i).GetNewsTitle();
					doneNewsNum++;
				}
				NewsDalHelper helper=new NewsDalHelper(getApplicationContext());
				helper.SynchronyData2DB(listNews);
				Log.i("downloadservices","下载内容结束");
				currentText="新闻内容下载完成";
				// 广播
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putIntArray("newsIdArray", newsIdArray);
				intent.putExtras(bundle);
				intent.setAction("android.cnblogs.com.update_newslist");
				sendBroadcast(intent);
			}
			
			return 1;
		}
		@Override
		protected void onPostExecute(Integer result) {
			downloadNotify.contentView.setViewVisibility(R.id.progressBlock, View.GONE);
			downloadNotify.contentView.setViewVisibility(R.id.notify_download_done, View.VISIBLE);
			downloadNotifMg.notify(INIT_DOWNLOAD_NOTIFY, downloadNotify);
			downloadNotifMg.cancelAll();
			String tips=getResources().getString(R.string.offline_notification_end_toast);
			tips=tips.replace("{0}", "博客" + doneBlogNum + "条，新闻" + doneNewsNum + "条");
			Toast.makeText(getApplicationContext(), tips, Toast.LENGTH_SHORT).show();
			DownloadServices.this.stopSelf();
		}
		@Override
		protected void onPreExecute() {
			downloadNotify.contentView.setProgressBar(R.id.notify_ProgressBar, 100, 0, false);
			downloadNotify.contentView.setTextViewText(R.id.text_percent, "0%");
			downloadNotifMg.notify(INIT_DOWNLOAD_NOTIFY, downloadNotify);
		}
		/*
		 * 报告进度
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			downloadNotify.contentView.setProgressBar(R.id.notify_ProgressBar, 100, values[0], false);
			downloadNotify.contentView.setTextViewText(R.id.text_percent, values[0]+"%");
			downloadNotifMg.notify(INIT_DOWNLOAD_NOTIFY, downloadNotify);
			if(!currentText.equals("")){
				downloadNotify.contentView.setTextViewText(R.id.notify_text_title, currentText);
			}
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}
