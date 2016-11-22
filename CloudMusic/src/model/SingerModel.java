package model;

import java.sql.Date;

/**
 * 
 * @author Eric_Chan
 * 数据库歌手信息模型
 *
 */
public class SingerModel {
	private int singer_id;
	private String singer_name;
	private String country;
	private Date birthday;
	public int getSinger_id() {
		return singer_id;
	}
	public void setSinger_id(int singer_id) {
		this.singer_id = singer_id;
	}
	public String getSinger_name() {
		return singer_name;
	}
	public void setSinger_name(String singer_name) {
		this.singer_name = singer_name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public Date getBirthday() {
		return birthday;
	}
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
}
