package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.core.CommentHelper;
import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;

/**
 * Comment返回xml解析器
 * 
 * @author walkingp
 * 
 */
public class CommentListXmlParser extends DefaultHandler {
	final String ENTRY_TAG = "entry";// 主标记
	final String ENTRY_ID_TAG = "id";// 编号标记
	final String ENTRY_PUBLISHED_TAG = "published";// 发表时间标记
	final String ENTRY_AUTHOR_TAG = "name";// 评论者名称
	final String ENTRY_AUTHOR_URL_TAG = "uri";// 评论者主页
	final String ENTRY_CONTENT = "content";// 评论内容

	private ArrayList<Comment> listComment;// 对象集合
	private Comment entity;// 单个对象
	private Comment.EnumCommentType commentType;// 评论类型
	private int contentId;// 内容编号
	private boolean isStartParse;// 开始解析
	private StringBuilder currentDataBuilder;// 当前取到的值
	/**
	 * 默认构造函数
	 */
	public CommentListXmlParser() {
		super();
	}
	/**
	 * 构造函数
	 * 
	 * @return
	 */
	public CommentListXmlParser(int contentId, ArrayList<Comment> list,
			Comment.EnumCommentType _commentType) {
		this.listComment = list;
		this.contentId = contentId;
		commentType = _commentType;
	}
	/**
	 * 将结果返回
	 * 
	 * @return
	 */
	public ArrayList<Comment> GetCommentList() {
		return listComment;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException {
		Log.i("Comment", "文档解析开始");
		super.startDocument();
		listComment = new ArrayList<Comment>();
		currentDataBuilder = new StringBuilder();
	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		if (localName.equalsIgnoreCase(ENTRY_TAG)) {
			entity = new Comment();
			entity.SetCommentType(commentType);// 设置评论类型
			entity.SetContentId(contentId);
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
			Log.i("Comment", "正在解析" + localName);
			// 处理
			if (localName.equalsIgnoreCase(ENTRY_CONTENT)) {// 内容
				chars = StringEscapeUtils.unescapeHtml(chars);// 进行编码处理，避免出现&gt;这种html
				// 处理回复：@
				chars = CommentHelper.FormatCommentString(chars);
				entity.SetContent(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG)) {// 评论者
				entity.SetPostUserName(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_ID_TAG)) {// 编号
				int id = Integer.parseInt(chars);
				entity.SetCommentId(id);
			} else if (localName.equalsIgnoreCase(ENTRY_PUBLISHED_TAG)) {// 发布时间
				Date addTime = AppUtil.ParseUTCDate(chars);
				entity.SetAddTime(addTime);
			} else if (localName.equalsIgnoreCase(ENTRY_AUTHOR_URL_TAG)) {// 评论者主页
				entity.SetPostUserUrl(chars);
			} else if (localName.equalsIgnoreCase(ENTRY_TAG)) {// 截止
				listComment.add(entity);
				isStartParse = false;
			}
		}

		currentDataBuilder.setLength(0);
	}
	/**
	 * 文档结束时触发
	 */
	public void endDocument() throws SAXException {
		Log.i("Comment", "文档解析结束");
		super.endDocument();
	}
}
