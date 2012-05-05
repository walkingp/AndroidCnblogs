package com.cnblogs.android.entity;

import java.util.Date;

/**
 * 博客实体类
 * @author walkingp
 * @since 2011-11-18 
 *
 */
public class Blog {
	private int _blogId;
	private String _blogTitle;
	private String _author;//作者博客名
	private String _userName;//用户名
	private String _authorUrl;
	private String _blogContent;
	private Date _addTime;
	private Date _updateTime;
	private int _viewNum;
	private int _commentNum;
	private int _diggsNum;
	private String _summary;
	private String _avator;
	private String _blogUrl;
	private int _cateId;
	private String _cateName;
	private boolean _isFullText;
	private boolean _isReaded;
		
	public void SetBlogId(int blogId){
		_blogId=blogId;
	}
	public int GetBlogId(){
		return _blogId;
	}
	public void SetBlogTitle(String blogTitle){
		_blogTitle=blogTitle;	
	}
	public String GetBlogTitle(){
		return _blogTitle;
	}
	public void SetAuthor(String author){
		_author=author;
	}
	public String GetAuthor(){
		return _author;
	}
	public void SetBlogContent(String content){
		_blogContent=content;
	}
	public String GetBlogContent(){
		return _blogContent;
	}
	public void SetAuthorUrl(String authorUrl){
		_authorUrl=authorUrl;
	}
	public String GetAuthorUrl(){
		return _authorUrl;
	}
	public void SetAddTime(Date addTime){
		_addTime=addTime;
	}
	public Date GetAddTime(){
		return _addTime;
	}
	public void SetUpdateTime(Date updateTime){
		_updateTime=updateTime;
	}
	public Date GetUpdateTime(){
		return _updateTime;
	}
	public void SetViewNum(int viewNum){
		_viewNum=viewNum;
	}
	public int GetViewNum(){
		return _viewNum;
	}
	public void SetCommentNum(int commentNum){
		_commentNum=commentNum;
	}
	public int GetCommentNum(){
		return _commentNum;
	}
	public void SetDiggsNum(int diggsNum){
		_diggsNum=diggsNum;
	}
	public int GetDiggsNum(){
		return _diggsNum;
	}
	public void SetSummary(String summary){
		_summary=summary;
	}
	public String GetSummary(){
		return _summary;
	}
	public void SetAvator(String avator){
		_avator=avator;
	}
	public String GetAvator(){
		return _avator;
	}
	public void SetBlogUrl(String blogUrl){
		_blogUrl=blogUrl;
	}
	public String GetBlogUrl(){
		return _blogUrl;
	}
	/**
	 * 重写
	 */
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Blog){
			Blog o = (Blog)obj;
		    return String.valueOf(o.GetBlogId()).equals(String.valueOf(this.GetBlogId()));
		}else{
		    return super.equals(obj);
		}
	}
	public void SetCateId(int _cateId) {
		this._cateId = _cateId;
	}
	public int GetCateId() {
		return _cateId;
	}
	public void SetIsFullText(boolean _isFullText) {
		this._isFullText = _isFullText;
	}
	public boolean GetIsFullText() {
		return _isFullText;
	}
	public void SetCateName(String _cateName) {
		this._cateName = _cateName;
	}
	public String GetCateName() {
		return _cateName;
	}
	public void SetIsReaded(boolean _isReaded) {
		this._isReaded = _isReaded;
	}
	public boolean GetIsReaded() {
		return _isReaded;
	}
	public void SetUserName(String userName) {
		_userName = userName;
	}
	public String GetUserName() {
		return _userName;
	}
}
