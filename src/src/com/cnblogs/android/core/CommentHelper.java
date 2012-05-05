package com.cnblogs.android.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.parser.CommentListXmlParser;
import com.cnblogs.android.utility.AppUtil;
import com.cnblogs.android.utility.NetHelper;

/**
 * Comment操作类
 * 
 * @author walkingp
 * @date 2011-11-29
 */
public class CommentHelper extends DefaultHandler {
	/**
	 * 根据页码返回Comment对象集合
	 * 
	 * @return pageIndex:页码，从1开始
	 */
	public static ArrayList<Comment> GetCommentList(int contentId,
			int pageIndex, Comment.EnumCommentType commentType) {
		int pageSize = Config.COMMENT_PAGE_SIZE;
		String url = Config.URL_NEWS_GET_COMMENT_LIST;
		if (commentType.equals(Comment.EnumCommentType.Blog)) {
			url = Config.URL_BLOG_GET_COMMENT_LIST;
		}
		url = url.replace("{contentId}", String.valueOf(contentId))
				.replace("{pageIndex}", String.valueOf(pageIndex))
				.replace("{pageSize}", String.valueOf(pageSize));// 数据地址
		String dataString = NetHelper.GetContentFromUrl(url);

		ArrayList<Comment> list = ParseString(contentId, dataString,
				commentType);

		return list;
	}
	/*
	 * 返回内容的所有评论
	 */
	public static List<Comment> GetCommentList(int contentId,
			Comment.EnumCommentType commentType, int commentCount) {
		List<Comment> listComment = new ArrayList<Comment>();
		int pageSize = 0;// 分页条数
		int pageCount = 0;// 总页码
		switch (commentType) {
			case Blog :
				pageSize = Config.BLOG_PAGE_SIZE;
				break;
			case News :
				pageSize = Config.NEWS_PAGE_SIZE;
				break;
		}
		pageCount = commentCount % pageSize == 0
				? commentCount / pageSize
				: commentCount / pageSize + 1;
		for (int i = 1; i < pageCount + 1; i++) {
			List<Comment> list = GetCommentList(contentId, i, commentType);
			listComment.addAll(list);
		}

		return listComment;
	}
	/**
	 * 将字符串转换为Comment集合
	 * 
	 * @return
	 */
	private static ArrayList<Comment> ParseString(int contentId,
			String dataString, Comment.EnumCommentType commentType) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		ArrayList<Comment> listComment = new ArrayList<Comment>();
		try {
			XMLReader xmlReader = saxParserFactory.newSAXParser()
					.getXMLReader();
			CommentListXmlParser handler = new CommentListXmlParser(contentId,
					listComment, commentType);
			xmlReader.setContentHandler(handler);

			xmlReader.parse(new InputSource(new StringReader(dataString)));
			listComment = handler.GetCommentList();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listComment;
	}
	/**
	 * 格式化评论内容 1、将'@name'后加冒号
	 * 
	 * @hchxxzx<br/> <a href="#2256199" title="查看所回复的评论">@</a>hchxxzx<br/>
	 *               内容 2、格式化引用部分 <fieldset
	 *               class="comment_quote"><legend>引用</legend>鎏：<br/>
	 *               大哥，看电视不是为了拆电视</fieldset><br/>
	 * @param html
	 * @return
	 */
	static final Pattern patternAtNoLink = Pattern.compile("@([^<]+)");
	static final Pattern patternAt = Pattern.compile("<a(.+?)>@</a>([^<]+)");
	static final Pattern patternQuote = Pattern
			.compile("<fieldset(.+?)><legend>(.+?)</legend>(.+?)：?<br\\s?/>(.+?)</fieldset>");// 引用
	public static String FormatCommentString(String html) {
		// 替换回复
		Matcher m = patternAtNoLink.matcher(html);
		String rs = new String(html);
		while (m.find()) {
			rs = m.replaceAll("@$1：");
		}
		m = patternAt.matcher(rs);
		while (m.find()) {
			rs = m.replaceAll("@$2：");
		}
		// 替换引用
		m = patternQuote.matcher(rs);
		while (m.find()) {
			rs = m.replaceAll("$1『");
			rs = m.replaceAll("@$2");
			rs = m.replaceAll("$3』");
		}
		// 替换图片
		rs = AppUtil.RemoveImgTag(rs);
		// 替换视频
		rs = AppUtil.RemoveVideoTag(rs);

		return rs;
	}
}
