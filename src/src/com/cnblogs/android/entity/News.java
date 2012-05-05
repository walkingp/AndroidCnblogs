package com.cnblogs.android.entity;

import java.util.Date;

/**
 * 新闻类
 * 
 * @author walkingp
 * 
 */
public class News {
	private int _newsId;
	private String _newsTitle;
	private String _author;
	private String _summary;
	private String _newsUrl;
	private String _newsContent;
	private Date _addTime;
	private int _diggsNum;
	private int _viewNum;
	private int _commentNum;
	private boolean _isFullText;
	private boolean _isReaded;

	public void SetNewsId(int blogId) {
		_newsId = blogId;
	}
	public int GetNewsId() {
		return _newsId;
	}
	public void SetNewsTitle(String blogTitle) {
		_newsTitle = blogTitle;
	}
	public String GetNewsTitle() {
		return _newsTitle;
	}
	public void SetAuthor(String author) {
		_author = author;
	}
	public String GetAuthor() {
		return _author;
	}
	public void SetSummary(String summary) {
		_summary = summary;
	}
	public String GetSummary() {
		return _summary;
	}
	public void SetNewsUrl(String newsUrl) {
		_newsUrl = newsUrl;
	}
	public String GetNewsUrl() {
		return _newsUrl;
	}
	public void SetNewsContent(String content) {
		_newsContent = content;
	}
	public String GetNewsContent() {
		return _newsContent;
	}
	public void SetAddTime(Date addTime) {
		_addTime = addTime;
	}
	public Date GetAddTime() {
		return _addTime;
	}
	public void SetDiggsNum(int diggsNum) {
		_diggsNum = diggsNum;
	}
	public int GetDiggsNum() {
		return _diggsNum;
	}
	public void SetViewNum(int viewNum) {
		_viewNum = viewNum;
	}
	public int GetViewNum() {
		return _viewNum;
	}
	public void SetCommentNum(int commentNum) {
		_commentNum = commentNum;
	}
	public int GetCommentNum() {
		return _commentNum;
	}
	/**
	 * 重写
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof News) {
			News o = (News) obj;
			return String.valueOf(o.GetNewsId()).equals(
					String.valueOf(this.GetNewsId()));
		} else {
			return super.equals(obj);
		}
	}
	public void SetIsFullText(boolean b) {
		this._isFullText = b;
	}
	public boolean GetIsFullText() {
		return _isFullText;
	}
	public void SetIsReaded(boolean _isReaded) {
		this._isReaded = _isReaded;
	}
	public boolean GetIsReaded() {
		return _isReaded;
	}
}
