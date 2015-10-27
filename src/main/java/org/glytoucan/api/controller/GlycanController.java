package org.glytoucan.api.controller;

import java.security.Principal;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glytoucan.model.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/glycan")
public class GlycanController {

	private static final Log logger = LogFactory
			.getLog(GlycanController.class);
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<Message> register(@RequestBody (required=true)
    String sequence, Principal p) {
		logger.debug("sequence:>" + sequence);
		logger.debug("name:>" + p.getName());
		
		Message msg = new Message();
		msg.setError("");
		msg.setMessage(sequence + " accepted");
//		msg.setMessage(model.toString());
		msg.setPath("/");
		msg.setStatus(HttpStatus.ACCEPTED.toString());
		msg.setTimestamp(new Date());
		return new ResponseEntity<Message> (msg, HttpStatus.ACCEPTED);
	}
}
