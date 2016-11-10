package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
class MyController {
	@RequestMapping(value = "/my", method = RequestMethod.GET)
	public String requestHTML() {
		return "my";
		
	}
}
