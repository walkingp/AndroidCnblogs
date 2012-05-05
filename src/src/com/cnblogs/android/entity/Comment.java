package com.cnblogs.android.entity;

import java.util.Date;

/**
 * 评论实体类
 * @author walkingp
 *
 */
public class Comment {
	private int _commentId;
	private String _postUserUrl;
	private String _postUserName;
	private String _content;
	private EnumCommentType _commentType;
	private Date _addTime;
	private int _contentId;
	
	public enum EnumCommentType{
		Blog,//0:博客
		News//1:文章
	}
	
	public void SetCommentId(int commentId){
		_commentId=commentId;
	}
	public int GetCommentId(){
		return _commentId;
	}
	public void SetPostUserUrl(String postUserUrl){
		_postUserUrl=postUserUrl;
	}
	public String GetPostUserUrl(){
		return _postUserUrl;
	}
	public void SetPostUserName(String postUserName){
		_postUserName=postUserName;
	}
	public String GetPostUserName(){
		return _postUserName;
	}
	public void SetContent(String content){
		_content=content;
	}
	public String GetContent(){
		return _content;
	}
	public void SetCommentType(EnumCommentType commentType){
		_commentType=commentType;
	}
	public EnumCommentType GetCommentType(){
		return _commentType;
	}
	public void SetAddTime(Date addTime){
		_addTime=addTime;
	}
	public Date GetAddTime(){
		return _addTime;
	}
	/**
	 * 重写
	 */
	@Override
	public boolean equals(Object obj){
		if (obj instanceof Comment){
			Comment o = (Comment)obj;
		    return String.valueOf(o.GetCommentId()).equals(String.valueOf(this.GetCommentId()));
		}else{
		    return super.equals(obj);
		}
	}
	public void SetContentId(int _contentId) {
		this._contentId = _contentId;
	}
	public int GetContentId() {
		return _contentId;
	}
}
