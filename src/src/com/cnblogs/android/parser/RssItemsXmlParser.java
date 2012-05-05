package com.cnblogs.android.parser;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.util.Log;
import org.xml.sax.Attributes;

import com.cnblogs.android.entity.*;
import com.cnblogs.android.utility.AppUtil;
/**
 * Blog返回xml解析器
 * @author walkingp
 *
 */
public class RssItemsXmlParser extends DefaultHandler {
	final String ENTRY_TAG="item";//主标记
	final String ENTRY_TAG2="entry";//主标记
	final String ENTRY_TITLE_TAG="title";//标题
	final String ENTRY_GUID_TAG="guid";//编号标记
	final String ENTRY_GUID_TAG2="id";//编号标记
	final String ENTRY_CATENAME_TAG="category";//分类标记
	final String ENTRY_ICON_TAG="image";//图片标记
	final String ENTRY_DESCRIPTION_TAG="description";//内容
	final String ENTRY_DESCRIPTION_TAG2="content";//内容
	final String ENTRY_LINK_TAG="link";//链接地址
	final String ENTRY_AUTHOR_TAG="author";//作者
	final String ENTRY_AUTHOR_TAG2="name";//作者
	final String ENTRY_ADDDATE_TAG="pubDate";//添加时间
	final String ENTRY_ADDDATE_TAG2="published";//添加时间
	
	private ArrayList<RssItem> listRss;//对象集合
	private RssItem entity;//单个对象
	private boolean isStartParse;//开始解析
	private StringBuilder currentDataBuilder;//当前取到的值
	/**
	 * 默认构造函数
	 */
	public RssItemsXmlParser(){
		super();
	}
	/**
	 * 构造函数
	 * @return
	 */
	public RssItemsXmlParser(ArrayList<RssItem> list){
		this.listRss=list;
	}
	/**
	 * 将结果返回
	 * @return
	 */
	public ArrayList<RssItem> GetRssItemList(){
		return listRss;
	}
	/**
	 * 文档开始时触发
	 */
	public void startDocument() throws SAXException{
		Log.i("Rss","文档解析开始");
		super.startDocument();
		listRss=new ArrayList<RssItem>();
		currentDataBuilder = new StringBuilder();  	}
	/**
	 * 读取并解析XML数据
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		super.startElement(uri, localName, qName,  attributes);
		if(localName.equalsIgnoreCase(ENTRY_TAG))  
        {  
            entity = new RssItem();  
            isStartParse = true;   
        }else if(localName.equalsIgnoreCase(ENTRY_TAG2)){
        	entity = new RssItem();  
            isStartParse = true;   
        }
	}
	/**
	 * 读取元素内容
	 * @param ch
	 * @param start
	 * @param length
	 * @throws SAXException
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
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
		if(isStartParse){//发现目标
			String chars=currentDataBuilder.toString().trim().replaceAll("\n|\t|\r", "");
			Log.i("Rss","正在解析" + localName);
    		//处理
    		if(localName.equalsIgnoreCase(ENTRY_TITLE_TAG)){//标题
    			entity.SetTitle(chars.trim());
    		}else if(localName.equalsIgnoreCase(ENTRY_ICON_TAG)){//图片
    			entity.SetLink(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_DESCRIPTION_TAG)){//简介
    			chars=StringEscapeUtils.unescapeHtml(chars);//进行编码处理，避免出现&gt;这种html
    			entity.SetDescription(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_DESCRIPTION_TAG2)){//简介 && (entity.GetDescription()==null || entity.GetDescription().equals(""))
    			entity.SetDescription(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_GUID_TAG)){//编号
				try{
					int id=Integer.parseInt(chars);
					entity.SetId(id);
				}catch(Exception ex){
					entity.SetId(0);
				}
			}else if(localName.equalsIgnoreCase(ENTRY_GUID_TAG2)){//编号 && entity.GetId()==0
				try{
					int id=Integer.parseInt(chars);
    				entity.SetId(id);
				}catch(Exception ex){
					entity.SetId(0);
				}
			}else if(localName.equalsIgnoreCase(ENTRY_LINK_TAG)){//链接
				entity.SetLink(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG)){//作者
				entity.SetAuthor(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_AUTHOR_TAG2)){//作者 && (entity.GetAuthor()==null || entity.GetAuthor().equals(""))
				entity.SetAuthor(chars);
			}else if(localName.equalsIgnoreCase(ENTRY_ADDDATE_TAG)){//添加时间
				Date addTime=AppUtil.ParseUTCDate(chars);
				entity.SetAddDate(addTime);	
			}else if(localName.equalsIgnoreCase(ENTRY_ADDDATE_TAG2)){//添加时间
				Date addTime=AppUtil.ParseUTCDate(chars);
				entity.SetAddDate(addTime);	
			}else if(localName.equalsIgnoreCase(ENTRY_CATENAME_TAG)){//分类
				entity.SetCategory(chars);
    		}else if(localName.equalsIgnoreCase(ENTRY_TAG)){//截止
    			listRss.add(entity);
    			isStartParse=false;
    		}else if(localName.equalsIgnoreCase(ENTRY_TAG2)){//截止
    			listRss.add(entity);
    			isStartParse=false;
    		}
		}
		
		currentDataBuilder.setLength(0);
	}
	/**
	 * 文档结束时触发
	 */
	public void endDocument() throws SAXException{
		Log.i("Rss","文档解析结束");
		super.endDocument();
	}
}





















