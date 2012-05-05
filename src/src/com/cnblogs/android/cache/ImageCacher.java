package com.cnblogs.android.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import com.cnblogs.android.core.Config;

public class ImageCacher {
	private Context context;
	public ImageCacher(Context context) {
		this.context = context;
	}
	public ImageCacher() {

	}
	/**
	 * 图片类型
	 * 
	 * @author Administrator
	 * 
	 */
	public enum EnumImageType {
		Avatar, // 头像
		Blog, // 博客
		News, // 新闻
		RssIcon, // RSS订阅分类
		Temp
		// 临时文件夹
	}
	/**
	 * 得到图片地址文件夹
	 * 
	 * @param imageType
	 * @return
	 */
	public static String GetImageFolder(EnumImageType imageType) {
		String folder = Config.TEMP_IMAGES_LOCATION;
		switch (imageType) {
			default :
			case Temp :
				folder += "temp/";
				break;
			case Avatar :
				folder += "avatar/";
				break;
			case Blog :
				folder += "blog/";
				break;
			case News :
				folder += "news/";
				break;
			case RssIcon :
				folder += "rss/icon/";
				break;
		}
		return folder;
	}
	static final Pattern patternImgSrc = Pattern
			.compile("<img(.+?)src=\"(.+?)\"(.+?)>");
	/**
	 * 得到html中的图片地址
	 * 
	 * @param html
	 * @return
	 */
	private static List<String> GetImagesList(String html) {
		List<String> listSrc = new ArrayList<String>();
		Matcher m = patternImgSrc.matcher(html);
		while (m.find()) {
			listSrc.add(m.group(2));
		}

		return listSrc;
	}
	/**
	 * 得到新图片地址（本地路径）
	 * 
	 * @param imgType
	 * @param imageUrl
	 * @return
	 */
	private static String GetNewImgSrc(EnumImageType imgType, String imageUrl) {
		if (imageUrl.contains("?")) {// 截断?后的字符串，避免无效图片
			imageUrl = imageUrl.substring(0, imageUrl.indexOf("?"));
		}
		imageUrl = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

		String folder = GetImageFolder(imgType);

		return "file:///mnt" + folder + imageUrl;
	}
	/**
	 * 下载html中的图片
	 * 
	 * @param imgType
	 * @param html
	 */
	public void DownloadHtmlImage(EnumImageType imgType, String html) {
		AsyncImageLoader imageLoader = new AsyncImageLoader(context);
		switch (imgType) {
			case Blog :
			case News :
			case Temp :
			default :
				List<String> listSrc = GetImagesList(html);
				for (String src : listSrc) {
					imageLoader.loadDrawable(imgType, src);
				}
				break;
			case Avatar :// 下载头像
				imageLoader.loadDrawable(imgType, html);
				break;
		}
	}
	/**
	 * 得到格式化后的html
	 * 
	 * @param imgType
	 * @param html
	 * @return
	 */
	public static String FormatLocalHtmlWithImg(EnumImageType imgType,
			String html) {
		List<String> listSrc = GetImagesList(html);
		for (String src : listSrc) {
			String newSrc = GetNewImgSrc(imgType, src);
			html = html.replace(src, newSrc);
		}

		return html;
	}
}
