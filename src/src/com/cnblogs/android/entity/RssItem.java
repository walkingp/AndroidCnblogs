package com.cnblogs.android.entity;

import java.util.Date;

public class RssItem {
	private int _id;
	private String _title;
	private String _link;
	private String _description;
	private String _category;
	private String _author;
	private Date _addDate;
	private boolean _isReaded;
	private boolean _isDigg;
	public void SetId(int id) {
		_id = id;
	}
	public int GetId() {
		return _id;
	}
	public void SetTitle(String title) {
		_title = title;
	}
	public String GetTitle() {
		return _title;
	}
	public void SetLink(String link) {
		_link = link;
	}
	public String GetLink() {
		return _link;
	}
	public void SetDescription(String description) {
		_description = description;
	}
	public String GetDescription() {
		return _description;
	}
	public void SetCategory(String category) {
		_category = category;
	}
	public String GetCategory() {
		return _category;
	}
	public void SetAuthor(String author) {
		_author = author;
	}
	public String GetAuthor() {
		return _author;
	}
	public void SetAddDate(Date addDate) {
		_addDate = addDate;
	}
	public Date GetAddDate() {
		return _addDate;
	}
	public void SetIsReaded(boolean isReaded) {
		_isReaded = isReaded;
	}
	public boolean GetIsReaded() {
		return _isReaded;
	}
	public void SetIsDigg(boolean isDigg) {
		_isDigg = isDigg;
	}
	public boolean GetIsDigg() {
		return _isDigg;
	}
}
