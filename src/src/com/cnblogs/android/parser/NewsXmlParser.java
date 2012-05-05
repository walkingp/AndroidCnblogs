package com.cnblogs.android.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
/**
 * 实体类转换
 * 
 * @author walkingp
 * 
 */
public class NewsXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Content";// 主标记
	final String ENTRY_IMAGE_URL = "ImageUrl";// 图片标记

	private String newsContent;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public NewsXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public NewsXmlParser(String content) {
		this.newsContent = content;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public String GetNewsContent() {
		return newsContent;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("News", "文档解析开始");
		super.startDocument();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			newsContent = "";
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
			Log.i("News", "正在解析" + localName);
			// 处理
			if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 标题
				newsContent = chars;
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
