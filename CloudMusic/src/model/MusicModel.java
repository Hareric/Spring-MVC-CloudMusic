package model;

import java.sql.SQLException;

import model.db.Connector;
import model.db.DbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.ResultSetMetaData;

/**
 * 
 * @author Eric_Chan
 * 数据库音乐模型
 */
public class MusicModel {
	private String name;  // 音乐名
	private String src;  // 音乐资源链接
	private String lyric;  // 音乐类型
	private int music_id;  // 音乐id
	private int listeners;  // 收听的人数
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getLyric() {
		return lyric;
	}
	public void setLyric(String lyric) {
		this.lyric = lyric;
	}
	public int getMusic_id() {
		return music_id;
	}
	public void setMusic_id(int music_id) {
		this.music_id = music_id;
	}
	public int getListeners() {
		return listeners;
	}
	public void setListeners(int listeners) {
		this.listeners = listeners;
	}
	
	/**
	 * 获取音乐列表
	 * @return
	 */
	public static String getLatestMusic(){
		DbHelper connector = Connector.getInstance();
		ResultSet rs = (ResultSet) connector.executeQuery("SELECT name, music_id, singer_name, src FROM app_singerRmusic"
				+ " NATURAL JOIN app_Singer NATURAL JOIN app_Music LIMIT 20");
		return DbHelper.resultSetToJson(rs);
	}
	
	/**
	 * 获取音乐排名列表
	 * @param l
	 * @return
	 */
	public static String getRankMusic(int l){
		l++;
		DbHelper connector = Connector.getInstance();
		ResultSet rs = (ResultSet) connector.executeQuery("	SELECT name, music_id FROM app_musicRclass "
				+ "NATURAL JOIN app_Music NATURAL JOIN app_Class WHERE class_id=? ORDER BY listeners DESC LIMIT 10", l);
		return DbHelper.resultSetToJson(rs);
	}
	
	/**
	 * 获取单条音乐详细信息
	 * @param id
	 * @return
	 */
	public static String getMusicInfo(String id){
		DbHelper connector = Connector.getInstance();
		ResultSet rs = (ResultSet) connector.executeQuery("SELECT name, singer_name, src, music_id FROM "
				+ "app_singerRmusic NATURAL JOIN app_Music NATURAL JOIN app_Singer WHERE music_id=?",id);
		return DbHelper.resultSetToJson(rs);
	}
	public static String getMusicInfoSrc(String src){
		System.out.println(src);
		DbHelper connector = Connector.getInstance();
		ResultSet rs = (ResultSet) connector.executeQuery("SELECT name, singer_name, src, music_id FROM "
				+ "app_singerRmusic NATURAL JOIN app_Music NATURAL JOIN app_Singer WHERE src=?",src);
		return DbHelper.resultSetToJson(rs);
	}
	public static void main(String args[]){
		System.out.print(getRankMusic(0));
	}
	
}
