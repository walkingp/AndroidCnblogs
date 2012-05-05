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
 * Blog返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class RssListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Item";// 主标记
	final String ENTRY_ID_TAG = "RssId";// 编号标记
	final String ENTRY_TITLE_TAG = "Title";// 标题标记
	final String ENTRY_SUMMARY_TAG = "Description";// 简介标记
	final String ENTRY_PUBLISHED_TAG = "AddTime";// 发表时间标记
	final String ENTRY_UPDATED_TAG = "Updated";// 更新时间标记
	final String ENTRY_AUTHOR_NAME_TAG = "Author";// 发表者名称
	final String ENTRY_LINK_TAG = "Link";// 实际链接地址
	final String ENTRY_AVATOR_TAG = "Image";// Logo地址
	final String ENTRY_RSS_NUM_TAG = "RssNum";// 订阅次数
	final String ENTRY_IS_CNBLOGS_TAG = "IsCnblogs";// 是否来自博客园

	private ArrayList<RssList> listRss;// 对象集合
	private RssList entity;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public RssListXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public RssListXmlParser(ArrayList<RssList> list) {
		this.listRss = list;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public ArrayList<RssList> GetRssList() {
		return listRss;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "文档解析开始");
		super.startDocument();
		listRss = new ArrayList<RssList>();
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
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// 编号
				int id = Integer.parseInt(chars);
				entity.SetRssId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_RSS_NUM_TAG)) {// 编号
				int id = Integer.parseInt(chars);
				entity.SetRssNum(id);
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
			} else if (localName.equalsIgnoreCase(ENTRY_IS_CNBLOGS_TAG)) {// 是否来自博客园
				int id = Integer.parseInt(chars);
				boolean isCnblogs = id == 1;
				entity.SetIsCnblogs(isCnblogs);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 截止
				listRss.add(entity);
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
