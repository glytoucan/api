package org.glytoucan.api.controller;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlDAO;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.utils.TripleStoreProperties;
import org.glytoucan.model.GlycoSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;


/**
 * @author aoki
 *
 * This work is licensed under the Creative Commons Attribution 4.0 International License. 
 * To view a copy of this license, visit http://creativecommons.org/licenses/by/4.0/.
 *
 */
@RestController
@Api(value="/glycosequence")
@RequestMapping ("/glycosequence")
public class GlycoSequenceController {
	protected Log logger = LogFactory.getLog(getClass());

	@Autowired
	SparqlDAO sparqlDAO;

	@Autowired
	@Qualifier(value = "glycoSequenceSelectSparql")
	private SelectSparql selectSparql;
	
	public SparqlDAO getSparqlDAO() {
		return sparqlDAO;
	}

	public void setSparqlDAO(SparqlDAO sparqlDAO) {
		this.sparqlDAO = sparqlDAO;
	}
		
	public void setSelectSparql(SelectSparql read) {
		this.selectSparql = read;
	}

	public SelectSparql getSelectSparql() {
		return selectSparql;
	}

	private final AtomicLong counter = new AtomicLong();

	@RequestMapping(value="/retrieve", method=RequestMethod.GET)
  @ApiOperation(value="Retrieve the sequence of an id", response=GlycoSequence.class)
	@ApiResponses (value ={@ApiResponse(code=200, message="Sequence retrieved successfully")})
	public @ResponseBody GlycoSequence retrieve(
			@ApiParam(required=true, value="id of the sequence")
			@RequestParam(value = "primaryId", defaultValue = "G00030MO") String primaryId) {
		logger.debug("primaryId=" + primaryId + "<");
		List<SparqlEntity> list = null;
		try {
			SelectSparql ss = getSelectSparql();
			SparqlEntity se = ss.getSparqlEntity();
			if (null==ss.getSparqlEntity())
				se = new SparqlEntity();
			se.setValue(Saccharide.PrimaryId, primaryId);
			ss.setSparqlEntity(se);
			logger.debug(ss.getSparql());
			list = sparqlDAO.query(ss);
		} catch (SparqlException e) {
			return new GlycoSequence(0, "sorry something bad happened." + e.getMessage());
		}
		SparqlEntity se = null;
		try {
		se = list.get(0);
		} catch (java.lang.IndexOutOfBoundsException ie) {
			return new GlycoSequence(0, "sorry no results for " + primaryId );
		}
		GlycoSequence gs = new GlycoSequence(counter.incrementAndGet(), primaryId);
		gs.setSequence(se.getValue(org.glycoinfo.rdf.glycan.GlycoSequence.Sequence));

		return gs;
	}
	
//	@RequestMapping("/execute")
//    @ApiOperation(value="Execute a sparql", response=GlycoSequence.class)
//	@ApiResponses (value ={@ApiResponse(code=200, message="Sequence retrieved successfully")})
//	public GlycoSequence execute(
//			@RequestParam(value = "primaryId", defaultValue = "idRequired") String sparql) {
//		logger.debug("primaryId=" + sparql + "<");
//		List<SparqlEntity> list = null;
//		try {
//			SelectSparql ss = getSelectSparql();
//			SparqlEntity se = ss.getSparqlEntity();
//			if (null==ss.getSparqlEntity())
//				se = new SparqlEntity();
//			se.setValue(Saccharide.PrimaryId, sparql);
//			ss.setSparqlEntity(se);
//			logger.debug(ss.getSparql());
//			list = sparqlDAO.query(ss);
//		} catch (SparqlException e) {
//			return new GlycoSequence(0, "sorry something bad happened." + e.getMessage());
//		}
//		SparqlEntity se = null;
//		try {
//		se = list.get(0);
//		} catch (java.lang.IndexOutOfBoundsException ie) {
//			return new GlycoSequence(0, "sorry no results for " + primaryId );
//		}
//		GlycoSequence gs = new GlycoSequence(counter.incrementAndGet(), primaryId);
//		gs.setSequence(se.getValue(org.glycoinfo.rdf.glycan.GlycoSequence.Sequence));
//
//		return gs;
//	}
}