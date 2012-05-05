package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;

import org.apache.commons.lang.*;
/**
 * News返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class NewsListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// 主标记
	final String ENTRY_ID_TAG = "id";// 编号标记
	final String ENTRY_TITLE_TAG = "title";// 标题标记
	final String ENTRY_SUMMARY_TAG = "summary";// 简介标记
	final String ENTRY_PUBLISHED_TAG = "published";// 发表时间标记
	final String ENTRY_LINK_TAG = "link";// 实际链接地址
	final String ENTRY_DIGG_TAG = "diggs";// 推荐次数
	final String ENTRY_VIEW_TAG = "views";// 查看次数
	final String ENTRY_COMMENTS_TAG = "comments";// 评论次数
	final String ENTRY_URL_TAG = "link";// 实际网址标签
	final String ENTRY_URL_ATTRIBUTE_TAG = "href";// 网址属性标签

	private ArrayList<News> listNews;// 对象集合
	private News entity;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public NewsListXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public NewsListXmlParser(ArrayList<News> list) {
		this.listNews = list;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public ArrayList<News> GetNewsList() {
		return listNews;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("News", "文档解析开始");
		super.startDocument();
		listNews = new ArrayList<News>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new News();
			isStartParse = true;
		}
		if (isStartParse && localName.equalsIgnoreCase(ENTRY_URL_TAG)) {
			entity.SetNewsUrl(attributes.getValue(ENTRY_URL_ATTRIBUTE_TAG));
		}// 实际网址
	}
	/**
	 * 读取元素内容
	 * 
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		currentDataBuilder.append(ch, start, length);
	}
	/**
	 * 元素结束时触发
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		if (isStartParse) {// 发现目标

			String chars = currentDataBuilder.toString();
			Log.i("News", "正在解析" + localName);
			// 处理
			if (localName.equalsIgnoreCase(ENTRY_TITLE_TAG)) {// 标题
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// 进行编码处理，避免出现&gt;这种html
				} catch (Exception ex) {
					Log.e("newsXml", "__________解析出错_____________");
				}
				entity.SetNewsTitle(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// 摘要
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// 进行编码处理，避免出现&gt;这种html
				} catch (Exception ex) {
					Log.e("newsXml", "__________解析出错_____________");
				}
				entity.SetSummary(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// 编号
				int id = Integer.parseInt(chars);
				entity.SetNewsId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// 发布时间
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_DIGG_TAG)) {// 推荐次数
				entity.SetDiggsNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_VIEW_TAG)) {// 查看次数
				entity.SetViewNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_COMMENTS_TAG)) {// 评论次数
				entity.SetCommentNum(Integer.parseInt(chars));
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 截止
				listNews.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * 文档结束时触发
	 */
	public void endDocument() throws SAXException {
		Log.i("News", "文档解析结束");
		super.endDocument();
	}
}
