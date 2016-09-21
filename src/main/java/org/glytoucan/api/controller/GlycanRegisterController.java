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
import org.glycoinfo.rdf.glycan.ResourceEntry;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glycoinfo.rdf.service.exception.GlycanException;
import org.glytoucan.model.Glycan;
import org.glytoucan.model.GlycanRequest;
import org.glytoucan.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
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
public class GlycanRegisterController {

	private static final Log logger = LogFactory
			.getLog(GlycanRegisterController.class);

	@Autowired
	ContributorProcedure contributorProcedure;
	
	@Autowired
	GlycanProcedure glycanProcedure;
	
	@Transactional
	@RequestMapping(value = "/register", method = RequestMethod.POST)
    @ApiOperation(value = "Registers glycan by sequence string.  If dbId is passed, the link is created by the membership of the user.", response = Message.class)
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
			logger.error(e.getMessage());
			msg.setMessage(e.getId());
      logger.debug("dbId:>" + dbId);
			if (StringUtils.isNotBlank(dbId)) {
				try {
					SparqlEntity contribResults = contributorProcedure.selectDatabaseByContributor(p.getName());
					String contribId = contribResults.getValue(ResourceEntry.ContributorId);
					glycanProcedure.addResourceEntry(e.getId(), contribId, dbId);
				} catch (GlycanException | ContributorException e1) {
					logger.error("returning error:>" + e.getMessage());
					msg.setError(e.getMessage());
					msg.setMessage(e.getId() + " could not add id:>" + dbId);
					msg.setPath("/glycan/register");
					msg.setStatus(HttpStatus.BAD_REQUEST.toString());
					msg.setTimestamp(new Date());
					return new ResponseEntity<Message> (msg, HttpStatus.BAD_REQUEST);
				}
			}
		} catch (SparqlException e) {
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
