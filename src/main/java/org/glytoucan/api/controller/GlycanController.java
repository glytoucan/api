package org.glytoucan.api.controller;

import java.security.Principal;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.DuplicateException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glytoucan.model.GlycanRequest;
import org.glytoucan.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Autowired
	GlycanProcedure glycanProcedure;
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ResponseEntity<Message> register(@RequestBody (required=true) GlycanRequest req, Principal p) {
		String sequence = (String) req.getSequence();
		String dbId = (String) req.getPublicDatabaseStructureId();
		logger.debug("sequence:>" + sequence);
		logger.debug("dbId:>" + dbId);
		logger.debug("name:>" + p.getName());
		Message msg = new Message();
		msg.setMessage("");
		String sequenceResult = null;
		try {
			sequenceResult = glycanProcedure.register(sequence, p.getName());
			msg.setMessage(sequenceResult);
		} catch (DuplicateException e ) {
			msg.setMessage(e.getId());
			try {
				glycanProcedure.addResourceEntry(sequenceResult, p.getName(), e.getId());
			} catch (SparqlException e1) {
				msg.setError(e.getMessage());
				msg.setMessage(sequenceResult + " could not add id:>" + dbId);
				msg.setPath("/glycan/register");
				msg.setStatus(HttpStatus.BAD_REQUEST.toString());
				msg.setTimestamp(new Date());
				return new ResponseEntity<Message> (msg, HttpStatus.BAD_REQUEST);
			}
		} catch (SparqlException e) {
			msg.setError(e.getMessage());
			msg.setMessage(sequence + " not accepted");
			msg.setPath("/glycan/register");
			msg.setStatus(HttpStatus.BAD_REQUEST.toString());
			msg.setTimestamp(new Date());
			return new ResponseEntity<Message> (msg, HttpStatus.BAD_REQUEST);
		}

		msg.setError("");
		msg.setPath("/glycan/register");
		msg.setStatus(HttpStatus.ACCEPTED.toString());
		msg.setTimestamp(new Date());
		return new ResponseEntity<Message> (msg, HttpStatus.ACCEPTED);
	}
}
