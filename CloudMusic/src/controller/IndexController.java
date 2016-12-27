package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.*;

@Controller
class IndexController {
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String requestHTML() {
		return "index";
	}
	
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String requestHTML_2() {
		return "index";
	}
	
	@RequestMapping(value = "index/getNews", method = RequestMethod.GET,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String returnStringNews() {
		return NewsModel.getRealTimeNews();
	}
	
	@RequestMapping(value = "index/getMusic", method = RequestMethod.GET,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String returnMusic() {
		return MusicModel.getLatestMusic();
	}
	
	@RequestMapping (value="index/getRank/{data}", method=RequestMethod.GET,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String returnRank(@PathVariable("data") int data){
		return MusicModel.getRankMusic(data);
	}
	
	@RequestMapping (value="index/getMInfo/{id}", method=RequestMethod.GET,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String returnMinfo(@PathVariable("id") String id){
		return MusicModel.getMusicInfo(id);
	}
	
	@RequestMapping (value="index/getMSrc/**", method=RequestMethod.GET,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String returnMinfoSrc(HttpServletRequest request){
		String url = request.getRequestURL().toString();
		int l = url.split("index/getMSrc/").length;
		url = url.split("index/getMSrc/")[l-1];
		url = url.substring(10, url.indexOf("harPattern", 10));
		System.out.println(url);
		return MusicModel.getMusicInfoSrc(url);
//		return src;
	}	
	
}
