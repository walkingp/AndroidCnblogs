package com.cnblogs.android.utility;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cnblogs.android.R;
import com.cnblogs.android.SettingActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class AppUtil {
	/**
	 * String转换为时间
	 * @param str
	 * @return
	 */
	public static Date ParseDate(String str){
		SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
		Date addTime = null;
		try {
			addTime = dateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return addTime;
	}
	/**
	 * 将日期转换为字符串
	 * @param date
	 * @return
	 */
	public static String ParseDateToString(Date date){
		return ParseDateToString(date,"yyyy-MM-dd HH:mm:ss");
	}
	/**
	 * 将日期转换为字符串（重载）
	 * @param date
	 * @param format:时间格式，必须符合yyyy-MM-dd hh:mm:ss
	 * @return
	 */
	public static String ParseDateToString(Date date,String format){
		SimpleDateFormat dateFormat =new SimpleDateFormat(format);
			
		return dateFormat.format(date);
	}
	/**
	 * 将UMT时间转换为本地时间
	 * @param str
	 * @return
	 * @throws ParseException 
	 */
	public static Date ParseUTCDate(String str){
		//格式化2012-03-04T23:42:00+08:00
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.CHINA);
		try {
			Date date = formatter.parse(str);
			
			return date;
		} catch (ParseException e) {
			//格式化Sat, 17 Mar 2012 11:37:13 +0000
			//Sat, 17 Mar 2012 22:13:41 +0800
			try{
				SimpleDateFormat formatter2=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z",Locale.CHINA);
				Date date2 = formatter2.parse(str);
				
				return date2;
			}catch(ParseException ex){
				return null;
			}
		}		
	}
	/**
	 * 获取网络图片取Drawable
	 * @param url
	 * @return
	 */
	public static Drawable GetUrlDrawable(String url){
		try{			
			URL aryURI=new URL(url);
			URLConnection conn=aryURI.openConnection();
			InputStream is=conn.getInputStream();
			Bitmap bmp=BitmapFactory.decodeStream(is);
			return new BitmapDrawable(bmp);
		}catch(Exception e){
			Log.e("ERROR", "urlImage2Drawable方法发生异常，imageUrl：" + url, e);
			return null;
		}
	}
	/**
	 * 从网络地址返回Bitmap
	 * @param imageUrl
	 * @return
	 */
	public static Bitmap GetBitmap(String imageUrl){   
	    Bitmap mBitmap = null;   
	    try {   
	      URL url = new URL(imageUrl);   
	      URLConnection conn=url.openConnection();
	      InputStream is = conn.getInputStream();   
	      mBitmap = BitmapFactory.decodeStream(is);   
	    } catch (MalformedURLException e) {   
	      e.printStackTrace();   
	    } catch (IOException e) {   
	      e.printStackTrace();   
	    }   
	    return mBitmap;   
	}
	/**
	 * Drawable转换为Bitmap
	 * @param drawable
	 * @return
	 */
	public static Bitmap DrawableToBitmap(Drawable drawable) {  
	    try {  
	        Bitmap bitmap = Bitmap  
	                .createBitmap(  
	                        drawable.getIntrinsicWidth(),  
	                        drawable.getIntrinsicHeight(),  
	                        drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
	                                : Bitmap.Config.RGB_565);  
	        Canvas canvas = new Canvas(bitmap);  
	        // canvas.setBitmap(bitmap);  
	        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable  
	                .getIntrinsicHeight());  
	        drawable.draw(canvas);  
	  
	        return bitmap;  
	    } catch (OutOfMemoryError e) {  
	        e.printStackTrace();  
	        return null;  
	    }  
	} 
	/**
	 * 退出程序
	 * @param context
	 */
    public static void QuitHintDialog(final Context context){
    	new AlertDialog.Builder(context)
    	.setMessage(R.string.sys_ask_quit_app)
    	.setTitle(R.string.com_dialog_title_quit)
    	.setIcon(R.drawable.icon)
    	.setPositiveButton(R.string.com_btn_ok,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try{
					((Activity)context).finish();
				}catch(Exception e){
					Log.e("close","+++++++++++++出错+++++++++");
				}
			}
		})
		.setNegativeButton(R.string.com_btn_cancel, new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
    }
	/** 
	* 获得软件版本号
	*/
	public static int GetVersionCode(final Context con) {
		int version = 1;
		PackageManager packageManager = con.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(con.getPackageName(), 0);
			version = packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return version;
	}
	/**
	 * 获得软件名称
	 * @param context
	 * @return
	 */
	public static String GetVersionName(final Context context){
		String versionName = "1.0.0";
		PackageManager packageManager = context.getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionName = packageInfo.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}
	/**
	 * 过滤html特殊字符
	 * @param str
	 * @return
	 */
	public static String HtmlToText(String str){
		str=str.replace("<br />", "\n");
		str=str.replace("<br/>", "\n");
		str=str.replace("&nbsp;&nbsp;", "\t");
		str=str.replace("&nbsp;", " ");
		str=str.replace("&#39;","\\");
		str=str.replace("&quot;", "\\");
		str=str.replace("&gt;",">");
		str=str.replace("&lt;","<");
		str=str.replace("&amp;", "&");
		
		return str;
	}
	/**
	 * 将时间转换为中文
	 * @param datetime
	 * @return
	 */
	public static String DateToChineseString(Date datetime){
		Date today=new Date();
		long   seconds   =   (today.getTime()-   datetime.getTime())/1000; 

		long year=	seconds/(24*60*60*30*12);// 相差年数
		long   month  =   seconds/(24*60*60*30);//相差月数
		long   date   =   seconds/(24*60*60);     //相差的天数 
		long   hour   =   (seconds-date*24*60*60)/(60*60);//相差的小时数 
		long   minute   =   (seconds-date*24*60*60-hour*60*60)/(60);//相差的分钟数 
		long   second   =   (seconds-date*24*60*60-hour*60*60-minute*60);//相差的秒数 
		
		if(year>0){
			return year + "年前";
		}
		if(month>0){
			return month + "月前";
		}
		if(date>0){
			return date + "天前";
		}
		if(hour>0){
			return hour + "小时前";
		}
		if(minute>0){
			return minute + "分钟前";
		}
		if(second>0){
			return second + "秒前";
		}
		return "未知时间";
	}
	/**
	 * 判断是否cnblogs内部链接，返回0则代表不是
	 * 	格式：http://www.cnblogs.com/walkingp/archive/2011/05/27/2059420.html
	 * @param url
	 * @return
	 */
	public static int GetCnblogsBlogLinkId(String url){
		Pattern pattern=Pattern.compile("http://www.cnblogs.com/(.+?)/archive/(\\d+?)/(\\d+?)/(\\d+?)/(\\d+?).html");
		Matcher m=pattern.matcher(url);
		int id=0;
		while(m.find()){
			id=Integer.parseInt(m.group(5));
		}
		return id;
	}
	/**
	 * 格式化内容（用于博客内容及新闻内容）
	 */	
	public static String FormatContent(Context context, String html){
		//是否图片模式
		boolean isImgMode=SettingActivity.IsPicReadMode(context);
		
		if(!isImgMode){
			html=AppUtil.ReplaceImgTag(html);
			html=AppUtil.ReplaceVideoTag(html);
		}
		
		return html;
	}
	static final Pattern patternHtml=Pattern.compile("<.+?>");
	/**
	 * 移除html标记
	 * @param html
	 * @return
	 */
	public static String RemoveHtmlTag(String html){
		Matcher m=patternHtml.matcher(html);
		while(m.find()){
			html= m.replaceAll("");
		}
		return html;
	}
	/**
	 * 判断是否含有图片内容
	 * @param html
	 * @return
	 */
	static final Pattern patternImg=Pattern.compile("<img(.+?)src=\"(.+?)\"(.+?)(onload=\"(.+?)\")?([^\"]+?)>");
	public static boolean IsContainImg(String html){
		Matcher m=patternImg.matcher(html);
		while(m.find()){
			return true;
		}
		return false;
	}
	/**
	 * 移除图片标记
	 * @param html
	 * @return
	 */
	public static String RemoveImgTag(String html){
		Matcher m=patternImg.matcher(html);
		while(m.find()){
			html= m.replaceAll("");
		}
		return html;
	}
	/**
	 * 替换图片标记
	 * @param html
	 * @return
	 */
	static final Pattern patternImgSrc=Pattern.compile("<img(.+?)src=\"(.+?)\"(.+?)>");
	public static String ReplaceImgTag(String html){
		Matcher m=patternImgSrc.matcher(html);
		while(m.find()){
			html= m.replaceAll("【<a href=\"$2\">点击查看图片</a>】");
		}
		return html;
	}
	/**
	 * 移除视频标记
	 */
	static final Pattern patternVideo=Pattern.compile("<object(.+?)>(.*?)<param name=\"src\" value=\"(.+?)\"(.+?)>(.+?)</object>");
	public static String RemoveVideoTag(String html){
		Matcher m=patternVideo.matcher(html);
		while(m.find()){
			html= m.replaceAll("");
		}
		return html;
	}
	/**
	 * 替换视频标记
	 */
	static final Pattern patternVideoSrc=Pattern.compile("<object(.+?)>(.*?)<param name=\"src\" value=\"(.+?)\"(.+?)>(.+?)</object>");
	public static String ReplaceVideoTag(String html){
		Matcher m=patternVideoSrc.matcher(html);
		while(m.find()){
			html= m.replaceAll("【<a href=\"$3\">点击查看视频</a>】");
		}
		return html;
	}
}
