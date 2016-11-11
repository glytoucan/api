package org.glytoucan.api.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glytoucan.client.model.RegisterContributorResponse;
import org.glytoucan.client.model.ResponseMessage;
import org.glytoucan.model.Message;
import org.glytoucan.model.RegisterContributorRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
  @ApiOperation(value = "Registers Contributor by their username.", response = Message.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
  @Transactional
	public ResponseEntity<RegisterContributorResponse> register(@RequestBody (required=true) RegisterContributorRequest req) {
		String name = (String) req.getName();
		String email = req.getEmail();
		logger.debug("name:>" + name);
    RegisterContributorResponse response = new RegisterContributorResponse();		
		ResponseMessage msg = new ResponseMessage();
		
		msg.setMessage("");
		String result = null;
		try {
			result = contributorProcedure.addContributor(name, email);
			msg.setMessage(result);
		} catch (ContributorException e) {
			logger.error(e.getMessage());
			msg.setMessage(name + " not accepted");
			response.setResponseMessage(msg);

			return new ResponseEntity<RegisterContributorResponse> (response, HttpStatus.BAD_REQUEST);
		}

		msg.setMessage(name);
		response.setResponseMessage(msg);
		response.setContributorId(result);
		response.setName(name);
		return new ResponseEntity<RegisterContributorResponse> (response, HttpStatus.OK);
	}
}