package org.glytoucan.api.controller;

import java.math.BigInteger;
import java.security.Principal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.convert.error.ConvertException;
import org.glycoinfo.rdf.DuplicateException;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.Contributor;
import org.glycoinfo.rdf.glycan.ResourceEntry;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glycoinfo.rdf.service.exception.GlycanException;
import org.glytoucan.client.GlycoSequenceClient;
import org.glytoucan.client.config.ClientConfiguration;
import org.glytoucan.client.config.GlycanQueryConfig;
import org.glytoucan.client.model.GlycoSequenceSearchResponse;
import org.glytoucan.client.model.ResponseMessage;
import org.glytoucan.model.Glycan;
import org.glytoucan.model.GlycanRequest;
import org.glytoucan.model.Message;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;

@Controller
@RequestMapping("/glycan")
@Import({GlycanQueryConfig.class, ClientConfiguration.class})
public class GlycanRegisterController {

	private static final Log logger = LogFactory
			.getLog(GlycanRegisterController.class);

	@Autowired
	ContributorProcedure contributorProcedure;
	
	@Autowired
	GlycanProcedure glycanProcedure;
	
	@Autowired
	@Qualifier("glycoSequenceClient")
	GlycoSequenceClient glycoSequenceClient;
	
	@Transactional
	@RequestMapping(value = "/register", method = RequestMethod.POST)
    @ApiOperation(value = "Registers glycan by sequence string.  If dbId is passed, the link is created by the membership of the user.", response = Message.class)
	public ResponseEntity<Message> register(@RequestBody (required=true) GlycanRequest req, Principal p) {
		String sequence = (String) req.getSequence();
		String dbId = (String) req.getPublicDatabaseStructureId();
		Message msg = new Message();
		msg.setMessage("");
		String sequenceResult = null;

		try {
  		if (StringUtils.isBlank(dbId)) {
        logger.debug("registering with:>");
  	    logger.debug("sequence:>" + sequence);
  	    logger.debug("name:>" + p.getName());
        sequenceResult = glycanProcedure.register(sequence, p.getName());
        msg.setMessage(sequenceResult);
  		} else {
        logger.debug("registering with:>");
  	    logger.debug("sequence:>" + sequence);
  	    logger.debug("dbId:>" + dbId);
  	    logger.debug("name:>" + p.getName());
        sequenceResult = glycanProcedure.register(sequence, p.getName(), dbId);
        msg.setMessage(sequenceResult);
  		}
    } catch (DuplicateException dupe) {
      logger.error("returning dupe:>" + dupe.getMessage());
      msg.setMessage(dupe.getId());
      msg.setPath("/glycan/register");
      msg.setStatus(HttpStatus.ACCEPTED.toString());
      msg.setTimestamp(new Date());
      return new ResponseEntity<Message> (msg, HttpStatus.ACCEPTED);
		} catch (GlycanException | ContributorException | SparqlException e) {
			logger.error("returning error:>" + e.getMessage());
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
	
	 @Transactional
	  @RequestMapping(value = "/removePartnerAccession", method = RequestMethod.POST)
	    @ApiOperation(value = "Removes Partner ID linked to glycan structure.", response = Message.class)
	  public ResponseEntity<Message> removePartnerId(@RequestBody (required=true) GlycanRequest req, Principal p) {
	    String sequence = (String) req.getSequence();
	    String dbId = (String) req.getPublicDatabaseStructureId();
	    Message msg = new Message();
	    msg.setMessage("");
	    String sequenceResult = null;

	    try {
	      if (StringUtils.isBlank(dbId)) {
	        logger.error("db id is blank");
	        msg.setError("db id is blank");
	        msg.setMessage(sequence + " not accepted");
	        msg.setPath("/glycan/removePartnerId");
	        msg.setStatus(HttpStatus.BAD_REQUEST.toString());
	        msg.setTimestamp(new Date());
	        return new ResponseEntity<Message> (msg, HttpStatus.BAD_REQUEST);

	      } else {
	        logger.debug("registering with:>");
	        logger.debug("sequence:>" + sequence);
	        logger.debug("dbId:>" + dbId);
	        logger.debug("name:>" + p.getName());
	        
//    		glycanProcedure.setSequence(sequence.getSequenceInput());
    	    GlycoSequenceSearchResponse response = glycoSequenceClient.textSearchRequest(sequence);
    	    Assert.assertNotNull(response);
    	    
    	    logger.debug(response);
    	    logger.debug(response.getAccessionNumber());
    	    ResponseMessage rm = response.getResponseMessage();
    	    logger.debug(rm);
    	    logger.debug(rm.getErrorCode());
    	    if (BigInteger.ZERO.compareTo(rm.getErrorCode()) != 0) {
	            logger.debug("ResponseMessage error:>" + rm.getMessage());
	            msg.setMessage(dbId + " for " + sequence + " removed (if it existed).");
    	    } else { 
    	    	// follow the same method as StructuresController
    	    	//	        SparqlEntity sequenceSE = glycanProcedure.searchBySequence(sequence);
        		String id = response.getAccessionNumber();
    	    	glycanProcedure.removeResourceEntry(id, p.getName(), dbId);
    	    }
	      }
	    } catch (GlycanException | ContributorException e) {
	      logger.error("returning error:>" + e.getMessage());
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
