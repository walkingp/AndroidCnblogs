package com.cnblogs.android.parser;

import java.util.ArrayList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
/**
 * Blog返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class RssCateXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "Item";// 主标记
	final String ENTRY_ID_TAG = "CateId";// 编号标记
	final String ENTRY_TITLE_TAG = "CateName";// 标题标记
	final String ENTRY_ICON_TAG = "Icon";// 图片标记
	final String ENTRY_SUMMARY_TAG = "Summary";// 简介

	private ArrayList<RssCate> listRss;// 对象集合
	private RssCate entity;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public RssCateXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public RssCateXmlParser(ArrayList<RssCate> list) {
		this.listRss = list;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public ArrayList<RssCate> GetRssCateList() {
		return listRss;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("Blog", "文档解析开始");
		super.startDocument();
		listRss = new ArrayList<RssCate>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new RssCate();
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
				entity.SetCateName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ICON_TAG)) {// 图片
				entity.SetIcon(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_SUMMARY_TAG)) {// 简介
				entity.SetSummary(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// 编号
				int id = Integer.parseInt(chars);
				entity.SetCateId(id);
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
