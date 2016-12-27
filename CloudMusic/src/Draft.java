
public class Draft {
	public static void main(String[] args){
		String url = "http://localhost:8080/CloudMusic/index/getMSrc/harPatternhttp://7xstax.com1.z0.glb.clouddn.com/lrc-love.mp3harPattern";
	
		int l = url.split("index/getMSrc/").length;
		url = url.split("index/getMSrc/")[l-1];
		
		url = url.substring(10, url.indexOf("harPattern", 10));
		System.out.println(url);
	}
}
