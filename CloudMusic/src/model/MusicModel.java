package model;

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
	
	
}
