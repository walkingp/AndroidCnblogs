package com.cnblogs.android.entity;

public class App {
	private int _appId;
	private String _appTitle;
	private String _alias;
	private String _version;
	private int _innerVersion;
	private String _updateRemark;
	private String _summary;
	private String _fileLocalUrl;
	private String _link;
	private int _downNum;
	private String _feedbackUrl;
	public void SetAppId(int _appId) {
		this._appId = _appId;
	}
	public int GetAppId() {
		return _appId;
	}
	public void SetAppTitle(String _appTitle) {
		this._appTitle = _appTitle;
	}
	public String GetAppTitle() {
		return _appTitle;
	}
	public void SetAlias(String _alias) {
		this._alias = _alias;
	}
	public String GetAlias() {
		return _alias;
	}
	public void SetVersion(String _version) {
		this._version = _version;
	}
	public String GetVersion() {
		return _version;
	}
	public void SetInnerVersion(int _innerVersion) {
		this._innerVersion = _innerVersion;
	}
	public int GetInnerVersion() {
		return _innerVersion;
	}
	public void SetUpdateRemark(String _updateRemark) {
		this._updateRemark = _updateRemark;
	}
	public String GetUpdateRemark() {
		return _updateRemark;
	}
	public void SetSummary(String _summary) {
		this._summary = _summary;
	}
	public String GetSummary() {
		return _summary;
	}
	public void SetFileLocalUrl(String _fileLocalUrl) {
		this._fileLocalUrl = _fileLocalUrl;
	}
	public String GetFileLocalUrl() {
		return _fileLocalUrl;
	}
	public void SetLink(String _link) {
		this._link = _link;
	}
	public String GetLink() {
		return _link;
	}
	public void SetDownNum(int _downNum) {
		this._downNum = _downNum;
	}
	public int GetDownNum() {
		return _downNum;
	}
	public void SetFeedbackUrl(String _feedbackUrl) {
		this._feedbackUrl = _feedbackUrl;
	}
	public String GetFeedbackUrl() {
		return _feedbackUrl;
	}
}
