package controller;


import model.UserModel;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
class LoginController {
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView requestHTML() {
		return new ModelAndView("login", "log", new UserModel());
	}
	
	@RequestMapping(value = "/userLogin", method = RequestMethod.POST,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String userRegister(@ModelAttribute("log") UserModel user) {
		String status = user.login();
		return status;
	}
}
