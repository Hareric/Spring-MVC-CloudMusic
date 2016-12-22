package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import model.NewsModel;

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
	
}
