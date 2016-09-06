package org.glytoucan.api.controller;

import java.security.Principal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.DuplicateException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.Contributor;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glycoinfo.rdf.service.exception.GlycanException;
import org.glytoucan.model.Glycan;
import org.glytoucan.model.GlycanRequest;
import org.glytoucan.model.Message;
import org.glytoucan.model.RegisterContributorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/contributor")
public class ContributorController {

	private static final Log logger = LogFactory
			.getLog(ContributorController.class);

	@Autowired
	ContributorProcedure contributorProcedure;
	
	@Transactional
	@RequestMapping(value = "/register", method = RequestMethod.POST)
  @ApiOperation(value = "Registers Contributor by their username.", response = Message.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<Message> register(@RequestBody (required=true) RegisterContributorRequest req) {
		String name = (String) req.getName();
		logger.debug("name:>" + name);
		
		Message msg = new Message();
		msg.setMessage("");
		String result = null;
		try {
			result = contributorProcedure.addContributor(name);
			msg.setMessage(result);
		} catch (ContributorException e) {
			logger.error(e.getMessage());
			msg.setMessage(name + " not accepted");
			msg.setError(e.getMessage());
			msg.setPath("/contributor/register");
			msg.setStatus(HttpStatus.BAD_REQUEST.toString());
			msg.setTimestamp(new Date());
			return new ResponseEntity<Message> (msg, HttpStatus.BAD_REQUEST);
		}

		msg.setError("");
		msg.setPath("/contributor/register");
		msg.setStatus(HttpStatus.OK.toString());
		msg.setTimestamp(new Date());
		return new ResponseEntity<Message> (msg, HttpStatus.OK);
	}
}