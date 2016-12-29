package model;

import java.sql.Date;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.ResultSetMetaData;

import model.db.Connector;
import model.db.DbHelper;

/**
 * 
 * @author Eric_Chan
 * 数据库新闻模型
 */
public class NewsModel {
	private String title;
	private String content;
	private int id;
	private String type;
	private Date pubDate;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getPubDate() {
		return pubDate;
	}
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}
	
	public static String getRealTimeNews(){
		DbHelper connector = Connector.getInstance();
		ResultSet rs = (ResultSet) connector.executeQuery("SELECT title, id, pubDate FROM app_News LIMIT 7");
		return DbHelper.resultSetToJson(rs);
	}
	public static void main(String args[]){
		System.out.print(getRealTimeNews());
	}
	
}
