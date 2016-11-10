package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

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
}
