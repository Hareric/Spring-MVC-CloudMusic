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
class RegController {
	@RequestMapping(value = "/reg", method = RequestMethod.GET)
	public ModelAndView requestHTML() {
		return new ModelAndView("reg", "register", new UserModel());	
	}
	
	@RequestMapping(value = "/userRegister", method = RequestMethod.POST,
			produces="text/html;charset=utf-8")
	@ResponseBody
	public String userRegister(@ModelAttribute("register") UserModel user) {
		String status = user.register();
		return status;
	}
}
