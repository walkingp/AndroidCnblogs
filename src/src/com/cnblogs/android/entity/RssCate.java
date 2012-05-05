package com.cnblogs.android.entity;

/**
 * ∂©‘ƒ∑÷¿‡
 * @author walkingp
 *
 */
public class RssCate {
	private int _cateId;
	private String _cateName;
	private String _icon;
	private String _summary;
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
	public void SetIcon(String icon) {
		_icon = icon;
	}
	public String GetIcon() {
		return _icon;
	}
	public void SetSummary(String summary) {
		_summary = summary;
	}
	public String GetSummary() {
		return _summary;
	}
}
