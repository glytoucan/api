package org.glytoucan.api.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glytoucan.model.Glycan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping("/glycans")
public class GlycansController {

	private static final Log logger = LogFactory
			.getLog(GlycansController.class);
	
	@Autowired
	GlycanProcedure glycanProcedure;
	
	@RequestMapping(value = "/{accessionNumber}", method = RequestMethod.GET, produces={"application/xml", "application/json"})
	@ApiOperation(value="Retrieves glycan by accession number", response=Glycan.class)
	@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
			@ApiResponse(code=404, message="Glycan does not exist"),
			@ApiResponse(code=500, message="Internal Server Error")})
	public @ResponseBody Glycan getGlycan (
			@ApiParam(required=true, value="id of the glycan") @PathVariable("accessionNumber") String accessionNumber) throws SparqlException, ParseException {
		logger.debug("Get glycan");
    	SparqlEntity se = new SparqlEntity();

		se.setValue(Saccharide.PrimaryId, accessionNumber);

		SparqlEntity sparqlEntity = glycanProcedure.searchByAccessionNumber(accessionNumber);
		Glycan glycan = new Glycan();
		glycan.setAccessionNumber(accessionNumber);
		glycan.setContributor(sparqlEntity.getValue("Contributor"));
		logger.debug(sparqlEntity.getValue("DateRegistered"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
		Date date = sdf.parse(sparqlEntity.getValue("DateRegistered"));
		glycan.setDateEntered(date);
		glycan.setMass(Double.valueOf(sparqlEntity.getValue("Mass")));
		glycan.setStructure(sparqlEntity.getValue("GlycoCTSequence") + "\n");

		return glycan;
	}
}