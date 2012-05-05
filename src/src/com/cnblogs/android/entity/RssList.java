package com.cnblogs.android.entity;

import java.util.Date;

public class RssList {
	private int _rssId;
	private String _title;
	private String _link;
	private String _description;
	private Date _addTime;
	private int _orderNum;
	private int _rssNum;
	private String _guid;
	private boolean _isCnblogs;
	private String _image;
	private Date _updated;
	private String _author;
	private int _cateId;
	private String _cateName;
	private boolean _isActive;
	public void SetRssId(int rssId) {
		_rssId = rssId;
	}
	public void SetTitle(String title) {
		_title = title;
	}
	public String GetTitle() {
		return _title;
	}
	public int GetRssId() {
		return _rssId;
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
	public void SetAddTime(Date addTime) {
		_addTime = addTime;
	}
	public Date GetAddTime() {
		return _addTime;
	}
	public void SetOrderNum(int orderNum) {
		_orderNum = orderNum;
	}
	public int GetOrderNum() {
		return _orderNum;
	}
	public void SetRssNum(int rssNum) {
		_rssNum = rssNum;
	}
	public int GetRssNum() {
		return _rssNum;
	}
	public void SetGuid(String guid) {
		_guid = guid;
	}
	public String GetGuid() {
		return _guid;
	}
	public void SetIsCnblogs(boolean isCnblogs) {
		_isCnblogs = isCnblogs;
	}
	public boolean GetIsCnblogs() {
		return _isCnblogs;
	}
	public void SetImage(String image) {
		_image = image;
	}
	public String GetImage() {
		return _image;
	}
	public void SetUpdated(Date updated) {
		_updated = updated;
	}
	public Date GetUpdated() {
		return _updated;
	}
	public void SetAuthor(String author) {
		_author = author;
	}
	public String GetAuthor() {
		return _author;
	}
	public void SetCateId(int cateId) {
		_cateId = cateId;
	}
	public int GetCateId() {
		return _cateId;
	}
	public void SetCateName(String cateName) {
		_cateName = cateName;
	}
	public String GetCateName() {
		return _cateName;
	}
	public void SetIsActive(boolean isActive) {
		_isActive = isActive;
	}
	public boolean GetIsActive() {
		return _isActive;
	}
}
