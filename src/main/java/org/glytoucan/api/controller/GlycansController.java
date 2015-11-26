package org.glytoucan.api.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.glycoinfo.batch.search.wurcs.SubstructureSearchSparql;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glytoucan.model.Glycan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Controller
@RequestMapping("/glycans")
public class GlycansController {

	private static final Log logger = LogFactory.getLog(GlycansController.class);

	@Autowired
	GlycanProcedure glycanProcedure;
	
	@Autowired
	SubstructureSearchSparql substructureSearchSparql;

	@RequestMapping(value = "/{accessionNumber}", method = RequestMethod.GET, produces = { "application/xml",
			"application/json" })
	@ApiOperation(value = "Retrieves glycan by accession number", response = Glycan.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 404, message = "Glycan does not exist"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public @ResponseBody Glycan getGlycan(
			@ApiParam(required = true, value = "id of the glycan") @PathVariable("accessionNumber") String accessionNumber)
					throws SparqlException, ParseException {
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

	// sample sparql :
	// https://bitbucket.org/issaku/virtuoso/wiki/G00030MO%20(order%20by%20manual)
	@RequestMapping(value = "/sparql/substructure", method = RequestMethod.GET, produces = { "application/json" })
	@ApiOperation(value = "Returns the select SPARQL used to find a substructure in the wurcs RDF ontology.", response = SelectSparql.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Found match(es)"),
			@ApiResponse(code = 400, message = "Illegal argument - Glycan should be valid"),
			@ApiResponse(code = 404, message = "Cannot generate"),
			@ApiResponse(code = 500, message = "Internal Server Error") })
	public @ResponseBody SelectSparql substructureSearch(
			@RequestParam(required = true, value = "sequence", defaultValue = "WURCS") @ApiParam(required = true, value = "Glycan sequence", name = "sequence") String sequence,
			@RequestParam(required = true, value = "format", defaultValue = "wurcs") @ApiParam(required = true, value = "Glycan format - currently only wurcs via GET", name = "format") String format)
					throws SugarImporterException, GlycoVisitorException, SparqlException {
		logger.debug("Substructure search");
		logger.debug("sequence:" + sequence);
		logger.debug("format:" + format);

		// if glycoct:
		if (format != null && format.equals("glycoct")) {
			// if it's not valid, would have thrown an exception.

			// convert to wurcs
			SugarImporter t_objImporterGlycoCT = new SugarImporterGlycoCTCondensed();

			Sugar sugar = t_objImporterGlycoCT.parse(sequence);
			// GlycoCT must be validated by GlycoVisitorValidation
			GlycoVisitorValidation validation = new GlycoVisitorValidation();
			validation.start(sugar);
			if (validation.getErrors().size() > 0) {
				logger.error("Errors:");
				for (String err : validation.getErrors()) {
					logger.error(err);
				}
				logger.warn("Warnings:");
				for (String warn : validation.getWarnings()) {
					logger.warn(warn);
				}
				logger.warn("\n");
			}
			// SugarExporterWURC t_exporter3 = new SugarExporterWURCS();
			// t_exporter3.start(sugar);
			// sequence = t_exporter3.getWURCSCompress();
		}
		// if wurcs:
		// validate the structure
		
		// wurcs to sparql
		SparqlEntity se = new SparqlEntity();
		se.setValue(GlycoSequence.Sequence, sequence);
		SelectSparql selectSparql = substructureSearchSparql;
		selectSparql.setSparqlEntity(se);
		
		String where = selectSparql.getWhere();
		selectSparql.setWhere(where.replace('\n', ' '));
		String sparql = selectSparql.getSparql();
		selectSparql.setSparql(sparql.replace('\n', ' '));
		logger.debug("GlycanController result:>" + selectSparql.getSparql() + "<");

		return selectSparql;
	}
					
}