package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;
/**
 * Users返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class UserListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// 主标记
	final String ENTRY_AUTHOR_NAME_TAG = "blogapp";// 用户名标记，如：iamzhanglei
	final String ENTRY_BLOG_NAME_TAG = "title";// 博客名，如：【当耐特砖家】
	final String ENTRY_AVATOR_TAG = "avatar";// 头像地址
	final String ENTRY_URL_TAG = "id";// 实际网址标签
	final String ENTRY_POST_COUNT_TAG = "postcount";// 博文数
	final String ENTRY_UPDATE_TAG = "updated";// 最后更新时间

	private ArrayList<Users> listUser;// 对象集合
	private Users entity;// 单个对象
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public UserListXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public UserListXmlParser(ArrayList<Users> list) {
		this.listUser = list;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public ArrayList<Users> GetUserList() {
		return listUser;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("Users", "文档解析开始");
		super.startDocument();
		listUser = new ArrayList<Users>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new Users();
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
			Log.i("Users", "正在解析" + localName);
			// 处理
			if (localName.equalsIgnoreCase(ENTRY_AUTHOR_NAME_TAG)) {// 用户名
				entity.SetUserName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_BLOG_NAME_TAG)) {// 博客名
				entity.SetBlogName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AVATOR_TAG)) {// 用户头像
				entity.SetAvator(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_URL_TAG)) {// 博客地址
				entity.SetBlogUrl(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_POST_COUNT_TAG)) {// 博文数量
				int postCount = Integer.parseInt(chars);
				entity.SetBlogCount(postCount);
			} else if (localName.equalsIgnoreCase(ENTRY_UPDATE_TAG)) {// 最后更新时间
				Date updateTime = AppUtil.ParseUTCDate(chars);
				entity.SetLastUpdate(updateTime);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 截止
				listUser.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * 文档结束时触发
	 */
	public void endDocument() throws SAXException {
		Log.i("Users", "文档解析结束");
		super.endDocument();
	}
}
