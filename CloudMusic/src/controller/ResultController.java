package controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
class ResultController {
	@RequestMapping(value = "/result", method = RequestMethod.GET)
	public String requestHTML() {
		return "result";
		
	}
}
