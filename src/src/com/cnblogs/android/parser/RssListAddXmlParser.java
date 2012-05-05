package com.cnblogs.android.parser;
import java.util.Date;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;

import org.apache.commons.lang.*;
/**
 * Blog返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class RssListAddXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "channel";// 主标记
	final String ENTRY_TITLE_TAG = "title";// 标题标记
	final String ENTRY_PUBLISHED_TAG="pubDate";//发布时间
	final String ENTRY_SUMMARY_TAG = "description";// 简介标记
	final String ENTRY_UPDATED_TAG = "lastBuildDate";// 更新时间标记
	final String ENTRY_AUTHOR_NAME_TAG = "webMaster";// 发表者名称
	final String ENTRY_LINK_TAG = "link";// 实际链接地址
	final String ENTRY_AVATOR_TAG = "image";// Logo地址

	private RssList entity;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public RssListAddXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public RssListAddXmlParser(RssList entity) {
		this.entity = entity;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public RssList GetRssList() {
		return entity;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "文档解析开始");
		super.startDocument();
		entity = new RssList();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new RssList();
			isStartParse = true;
		}
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
			Log.i("Blog", "正在解析" + localName);
			// 处理
			if (localName.equalsIgnoreCase(ENTRY_TITLE_TAG)) {// 标题
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// 进行编码处理，避免出现&gt;这种html
				} catch (Exception ex) {
					Log.e("rssXml", "__________解析出错_____________");
				}
				entity.SetTitle(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// 摘要
				try {
					chars = StringEscapeUtils.unescapeHtml(chars);// 进行编码处理，避免出现&gt;这种html
				} catch (Exception ex) {
					Log.e("rssXml", "__________解析出错_____________");
				}
				entity.SetDescription(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// 发布时间
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_UPDATED_TAG)) {// 修改时间
				Date updateTime = AppUtil.ParseUTCDate(chars);
				entity.SetUpdated(updateTime);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_NAME_TAG)) {// 作者名称
				entity.SetAuthor(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AVATOR_TAG)) {// Logo地址
				entity.SetImage(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_LINK_TAG)) {// 实际
				entity.SetLink(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 截止
				entity.SetIsActive(true);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * 文档结束时触发
	 */
	public void endDocument() throws SAXException {
		Log.i("Rss", "文档解析结束");
		super.endDocument();
	}
}
