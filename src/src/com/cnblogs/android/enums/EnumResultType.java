package com.cnblogs.android.enums;

public class EnumResultType {
	public enum EnumActionResultType{
		Succ,//操作成功 
		Fail,//操作失败
		Exist,//已经存在
		NetworkUnavailable,//网络不可用
		UnknownErr//未知错误
	}
}
