package org.glytoucan.api.controller;

import java.util.Date;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glytoucan.model.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WelcomeController {

	private static final Log logger = LogFactory
			.getLog(WelcomeController.class);
	
    @RequestMapping("/")
    public String home() {
        return "redirect:swagger-ui.html";
    }
    
	@RequestMapping("/welcome")
	public @ResponseBody Message welcome(Map<String, Object> model) {
		Message msg = new Message();
		msg.setError("");
		msg.setMessage("OK");
		msg.setPath("/");
		msg.setStatus("200");
		msg.setTimestamp(new Date());
		return msg;
	}

	@RequestMapping("/status")
	public @ResponseBody Message status(Map<String, Object> model) {
		Message msg = new Message();
		msg.setError("");
		msg.setMessage("status");
		msg.setPath("/status");
		msg.setStatus("200");
		msg.setTimestamp(new Date());
		return msg;
	}
	
	@RequestMapping("/documentation/apidoc.html")
	public String apidoc(Map<String, Object> model) {
		 return "redirect:/swagger-ui.html";
	}
}
