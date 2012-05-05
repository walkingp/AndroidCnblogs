package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import com.cnblogs.android.dal.BlogDalHelper;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.BlogListXmlParser;
import com.cnblogs.android.parser.BlogXmlParser;
import com.cnblogs.android.utility.NetHelper;

/**
 * Blog操作类
 * 
 * @author walkingp
 * 
 */
public class BlogHelper extends DefaultHandler {
	/**
	 * 离线下载
	 * 
	 * @param top
	 * @return
	 */
	@SuppressWarnings("null")
	public static List<Blog> DownloadOfflineBlogList(int top) {
		int pageSize = top / Config.BLOG_PAGE_SIZE;
		int lastNum = pageSize % Config.BLOG_PAGE_SIZE;

		List<Blog> listBlogs = null;
		// 下载前几页
		for (int i = 0; i < pageSize; i++) {
			List<Blog> list = GetBlogList(i);

			listBlogs.addAll(list);
		}
		// 下载剩余内容
		List<Blog> list = GetBlogList(pageSize);// 下载最后一页
		for (int i = 0, len = list.size(); i < len; i++) {
			listBlogs.addAll(list);
			if (list.get(i).GetBlogId() == lastNum) {
				break;
			}
		}
		// 内容
		for (int i = 0, len = listBlogs.size(); i < len; i++) {
			String content = GetBlogContentByIdWithNet(listBlogs.get(i)
					.GetBlogId());
			listBlogs.get(i).SetBlogContent(content);

			listBlogs.get(i).SetIsFullText(true);// 更新全文标志
		}

		return listBlogs;
	}
	/**
	 * 根据页码返回Blog对象集合
	 * 
	 * @return pageIndex:页码，从1开始
	 */
	public static ArrayList<Blog> GetBlogList(int pageIndex) {
		int pageSize = Config.BLOG_PAGE_SIZE;
		String url = Config.URL_GET_BLOG_LIST.replace("{pageIndex}",
				String.valueOf(pageIndex)).replace("{pageSize}",
				String.valueOf(pageSize));// 数据地址
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * 返回48小时内阅读排行Blog对象集合
	 */
	public static ArrayList<Blog> Get48HoursTopViewBlogList() {
		int size = Config.NUM_48HOURS_TOP_VIEW;
		String url = Config.URL_48HOURS_TOP_VIEW_LIST.replace("{size}",
				String.valueOf(size));// 数据地址
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * 返回10天内推荐排行Blog对象集合
	 */
	public static ArrayList<Blog> Get10DaysTopDiggBlogList() {
		int size = Config.NUM_TENDAYS_TOP_DIGG;
		String url = Config.URL_TENDAYS_TOP_DIGG_LIST.replace("{size}",
				String.valueOf(size));// 数据地址
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * 根据博客用户编号和页码返回Blog对象集合
	 * 
	 * @param userId
	 *            :用户编号
	 * @param pageIndex
	 *            :页码，从1开始
	 * @return
	 */
	public static ArrayList<Blog> GetAuthorBlogList(String author, int pageIndex) {
		int pageSize = Config.BLOG_LIST_BY_AUTHOR_PAGE_SIZE;
		String url = Config.URL_GET_BLOG_LIST_BY_AUTHOR
				.replace("{author}", author)
				.replace("{pageIndex}", String.valueOf(pageIndex))
				.replace("{pageSize}", String.valueOf(pageSize));// 数据地址
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Blog> list = ParseString(dataString);

		return list;
	}
	/**
	 * 将字符串转换为Blog集合
	 * 
	 * @return
	 */
	private static ArrayList<Blog> ParseString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<Blog> listBlog = new ArrayList<Blog>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			BlogListXmlParser handler = new BlogListXmlParser(listBlog);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listBlog = handler.GetBlogList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listBlog;
	}
	/**
	 * 根据编号获取博客内容
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetBlogContentByIdWithNet(int blogId) {
		String blogContent = "";
		String url = Config.URL_GET_BLOG_DETAIL.replace("{0}",
				String.valueOf(blogId));// 网址
		String xml = NetHelper.GetContentFromUrl(url);
		if (xml == "") {
			return "";
		}
		blogContent = ParseBlogString(xml);

		return blogContent;
	}
	/**
	 * 根据编号获取博客内容(先取本地，再取网络)
	 * 
	 * @param blogId
	 * @return
	 */
	public static String GetBlogById(int blogId, Context context) {
		String blogContent = "";
		// 优先考虑本地数据
		BlogDalHelper helper = new BlogDalHelper(context);
		Blog entity = helper.GetBlogEntity(blogId);
		if (null == entity || entity.GetBlogContent().equals("")) {
			blogContent = GetBlogContentByIdWithNet(blogId);
			/*String _blogContent=ImageCacher.FormatLocalHtmlWithImg(ImageCacher.EnumImageType.Blog, blogContent);
			if (Config.IS_SYNCH2DB_AFTER_READ) {
				helper.SynchronyContent2DB(blogId, _blogContent);// 同步至数据库
			}*/
		} else {
			blogContent = entity.GetBlogContent();
		}

		return blogContent;
	}
	/**
	 * 将字符串转换为Blog集合
	 * 
	 * @return
	 */
	private static String ParseBlogString(String dataString) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		String blogContent = "";
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			BlogXmlParser handler = new BlogXmlParser(blogContent);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			blogContent = handler.GetBlogContent();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return blogContent;
	}
}
