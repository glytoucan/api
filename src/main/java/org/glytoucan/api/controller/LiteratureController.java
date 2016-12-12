package org.glytoucan.api.controller;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.service.exception.LiteratureException;
import org.glycoinfo.rdf.service.impl.LiteratureProcedure;
import org.glytoucan.client.model.RegisterLiteratureRequestResponse;
import org.glytoucan.client.model.ResponseMessage;
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
@RequestMapping("/literature")
public class LiteratureController {

	private static final Log logger = LogFactory
			.getLog(LiteratureController.class);

	@Autowired
	LiteratureProcedure literatureProcedure;
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
  @ApiOperation(value = "Registers literary work by id.", response = RegisterLiteratureRequestResponse.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
  @Transactional
	public ResponseEntity<RegisterLiteratureRequestResponse> register(@RequestBody (required=true) RegisterLiteratureRequestResponse req) {
		logger.debug("accNum:>" + req.getAccessionNumber());
		logger.debug("pubId:>" + req.getPublicationId());
		RegisterLiteratureRequestResponse response = new RegisterLiteratureRequestResponse();		
		ResponseMessage msg = new ResponseMessage();
		
		msg.setMessage("");
		String result = null;
		try {
			if (!req.isRemoveFlag()) {
				result = literatureProcedure.addLiterature(req.getAccessionNumber(), req.getPublicationId(), req.getContributorId());
			} else {
//				result = literatureProcedure.removeLiterature(req.getAccessionNumber(), req.getPublicationId());
			}
			msg.setMessage(result);
				
		} catch (LiteratureException e) {
			logger.error(e.getMessage());
			msg.setMessage(req.getPublicationId() + " not accepted for " + req.getAccessionNumber() + ". details: " + e.getMessage());
			response.setResponseMessage(msg);

			return new ResponseEntity<RegisterLiteratureRequestResponse> (response, HttpStatus.BAD_REQUEST);
		} 

		msg.setMessage(req.getPublicationId() + " registered to " + req.getAccessionNumber() );
    msg.setErrorCode(new BigInteger("0"));
		req.setResponseMessage(msg);
		return new ResponseEntity<RegisterLiteratureRequestResponse> (req, HttpStatus.OK);
	}
}