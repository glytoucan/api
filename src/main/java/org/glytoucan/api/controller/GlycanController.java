package org.glytoucan.api.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eurocarbdb.MolecularFramework.io.CarbohydrateSequenceEncoding;
import org.eurocarbdb.MolecularFramework.io.SugarImporter;
import org.eurocarbdb.MolecularFramework.io.SugarImporterException;
import org.eurocarbdb.MolecularFramework.io.SugarImporterFactory;
import org.eurocarbdb.MolecularFramework.io.GlycoCT.SugarImporterGlycoCTCondensed;
import org.eurocarbdb.MolecularFramework.sugar.Sugar;
import org.eurocarbdb.MolecularFramework.util.validation.GlycoVisitorValidation;
import org.eurocarbdb.MolecularFramework.util.visitor.GlycoVisitorException;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycoinfo.batch.search.wurcs.SubstructureSearchSparql;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.util.DetectFormat;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.SparqlException;
import org.glycoinfo.rdf.dao.SparqlEntity;
import org.glycoinfo.rdf.dao.SparqlEntityFactory;
import org.glycoinfo.rdf.glycan.GlycoSequence;
import org.glycoinfo.rdf.glycan.Saccharide;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glycoinfo.vision.generator.ImageGenerator;
import org.glycoinfo.vision.importers.GWSImporter;
import org.glytoucan.model.Glycan;
import org.glytoucan.model.GlycanInput;
import org.glytoucan.model.GlycanList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.eurocarbdb.MolecularFramework.util.validation.StructureParserValidator;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api
@Controller
@RequestMapping("/glycans")
public class GlycanController {

  private static final Log logger = LogFactory.getLog(GlycanController.class);

  @Autowired
  GlycanProcedure glycanProcedure;

  @Autowired
  SubstructureSearchSparql substructureSearchSparql;

  @Autowired
  ImageGenerator imageGenerator;

  @Autowired
  MonosaccharideConverter monosaccharideConverter;

  @RequestMapping(value = "/{accessionNumber}", method = RequestMethod.GET, produces = { "application/xml",
      "application/json" })
  @ApiOperation(value = "Retrieves glycan by accession number", response = Glycan.class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 404, message = "Glycan does not exist"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
  @Transactional
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
    glycan.setMass(sparqlEntity.getValue("Mass"));
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

  @RequestMapping(value = "/list", method = RequestMethod.GET, produces = { "application/xml", "application/json" })
  @ApiOperation(value = "Lists all the glycans", response = GlycanList.class, notes = "payload option can be omitted to get only the glycan ids or set to 'full' to get glycan objects. 'exhibit' option allows to get glycan objects conforming to SIMILE Exhibit Json"
      + " format.")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
  public @ResponseBody ResponseEntity<GlycanList> listGlycans(
      @ApiParam(required = false, value = "payload: id (default) or full") @RequestParam(required = false, value = "payload", defaultValue = "id") String payload,
      @ApiParam(required = true, value = "limit: the limit to rows returned") @RequestParam(required = true, value = "limit", defaultValue = "100") String limit,
      @ApiParam(required = false, value = "offset: offset off of first row to retrieve") @RequestParam(required = false, value = "offset", defaultValue = "100") String offset)
      throws ParseException, SparqlException {
    GlycanList list = new GlycanList();
//    String imageURL;
    List<Glycan> glycanList = new ArrayList<Glycan>();
    if (payload != null && (payload.equalsIgnoreCase("full"))) {
      List<SparqlEntity> glycans = glycanProcedure.getGlycansAll(offset, limit);
      for (SparqlEntity sparqlEntity : glycans) {
        Glycan glycan = copyGlycan(sparqlEntity);
        logger.debug("adding:>" + glycan + "<");
        glycanList.add(glycan);
      }
      list.setGlycans(glycanList.toArray());
    } else {
      List<SparqlEntity> glycans = glycanProcedure.getGlycans(offset, limit);
      for (SparqlEntity sparqlEntity : glycans) {
        Glycan glycan = new Glycan();
        glycan.setAccessionNumber(sparqlEntity.getValue(Saccharide.PrimaryId));
        logger.debug("adding:>" + glycan + "<");
        glycanList.add(glycan);
      }
      List<String> ids = new ArrayList<String>();
      for (SparqlEntity sparqlEntity : glycans) {
        ids.add(sparqlEntity.getValue(GlycoSequence.AccessionNumber));
      }
      list.setGlycans(ids.toArray());
    }

    return new ResponseEntity<GlycanList>(list, HttpStatus.OK);
  }

  public Glycan copyGlycan(SparqlEntity se) throws ParseException {
    Glycan glycan = new Glycan();
    glycan.setAccessionNumber(se.getValue(GlycoSequence.AccessionNumber));
//    glycan.setContributor(se.getValue("Contributor"));

    logger.debug(se.getValue("DateRegistered"));
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.ENGLISH);
    Date date = sdf.parse(se.getValue("DateRegistered"));
    glycan.setDateEntered(date);
    if (StringUtils.isNotBlank(se.getValue("MassLabel")))
      glycan.setMass(se.getValue("MassLabel"));
    if (StringUtils.isNotBlank(se.getValue("GlycoCTSequence")))
      glycan.setStructure(se.getValue("GlycoCTSequence") + "\n");
    if (StringUtils.isNotBlank(se.getValue("Sequence")))
      glycan.setStructure(se.getValue("Sequence"));
    // glycan.setStructureLength(glycan.getStructure().length());
    return glycan;
  }

  @RequestMapping(value = "/image/glycan", method = RequestMethod.POST, consumes = { "application/xml",
      "application/json" }, produces = { MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_XML_VALUE,
          MediaType.IMAGE_JPEG_VALUE })
  @ApiOperation(value = "Retrieves glycan image by accession number", response = Byte[].class)
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Illegal argument"),
      @ApiResponse(code = 404, message = "Glycan does not exist"),
      @ApiResponse(code = 415, message = "Media type is not supported"),
      @ApiResponse(code = 500, message = "Internal Server Error") })
  @Transactional
  public @ResponseBody ResponseEntity<byte[]> getGlycanImageByStructure(
      @RequestBody(required = true) @ApiParam(required = true, value = "Glycan") GlycanInput glycan,
      @ApiParam(required = false, value = "format of the the glycan image", defaultValue = "png") @RequestParam("format") String format,
      @ApiParam(required = false, value = "notation to use to generate the image", defaultValue = "cfg") @RequestParam("notation") String notation,
      @ApiParam(required = false, value = "style of the image", defaultValue = "compact") @RequestParam("style") String style)
      throws Exception {

    if (StringUtils.isBlank(glycan.getFormat())
        || (!glycan.getFormat().equals(GlyConvert.WURCS) && !glycan.getFormat().equals(GlyConvert.GLYCOCT))) {
      glycan.setFormat(DetectFormat.detect(glycan.getSequence()));
    }

    byte[] bytes = null;

    if (glycan.getFormat().equals(GlyConvert.GLYCOCT)) {
      Sugar sugarStructure = importParseValidate(glycan);
      if (sugarStructure == null) {
        throw new IllegalArgumentException("Structure cannot be imported");
      }
      String exportedStructure;

      // export into GlycoCT
      try {
        exportedStructure = StructureParserValidator.exportStructure(sugarStructure);
      } catch (Exception e) {
        throw new IllegalArgumentException("Cannot export into common encoding: " + e.getMessage());
      }
      
      glycan.setSequence(exportedStructure);
      bytes = imageGenerator.getGlycoCTImage(glycan.getSequence(), format, notation, style);
      
    } else {      // else its wurcs
      bytes = imageGenerator.getImage(glycan.getSequence(), format, notation, style);
      
    }
    
    if (null == bytes || bytes.length < 1) {
      // our image generator returned null, return a default image.
      BufferedImage defaultImage = ImageIO.read(GlycanController.class.getResourceAsStream("/notfound.png"));
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ImageIO.write( defaultImage  , "png", byteArrayOutputStream);
      bytes = byteArrayOutputStream.toByteArray();
    }
    logger.debug(glycan.getSequence());

    HttpHeaders headers = new HttpHeaders();
    if (format == null || format.equalsIgnoreCase("png")) {
      headers.setContentType(MediaType.IMAGE_PNG);
    } else if (format.equalsIgnoreCase("svg")) {
      headers.setContentType(MediaType.APPLICATION_XML);
    } else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
      headers.setContentType(MediaType.IMAGE_JPEG);
    }
    return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
  }

  public Sugar importParseValidate(GlycanInput glycan) throws GlycoVisitorException, SugarImporterException {
    String encoding = glycan.getFormat();
    logger.debug("Input structure: {" + glycan.getSequence() + "}");
    Sugar sugarStructure = null;
    if (encoding != null && !encoding.isEmpty()
        && !(encoding.equalsIgnoreCase("glycoct") || encoding.equalsIgnoreCase("glycoct_condensed"))
        && !encoding.equalsIgnoreCase("gws")) {
      logger.debug("Converting from {" + encoding + "}");
      ArrayList<CarbohydrateSequenceEncoding> supported = SugarImporterFactory.getSupportedEncodings();
      for (Iterator<CarbohydrateSequenceEncoding> iterator = supported.iterator(); iterator.hasNext();) {
        CarbohydrateSequenceEncoding carbohydrateSequenceEncoding = (CarbohydrateSequenceEncoding) iterator.next();
        if (encoding.equalsIgnoreCase(carbohydrateSequenceEncoding.getId())) {
          try {
            if (encoding.equalsIgnoreCase("kcf")) {
              // sugarStructure =
              // SugarImporterFactory.importSugar(glycan.getSequence(),
              // carbohydrateSequenceEncoding, residueTranslator);
            } else {
              sugarStructure = SugarImporterFactory.importSugar(glycan.getSequence(), carbohydrateSequenceEncoding,
                  monosaccharideConverter);
            }
          } catch (Exception e) {
            // import failed
            String message = e.getMessage();
            // e.printStackTrace();
            if (e instanceof SugarImporterException) {
              message = ((SugarImporterException) e).getErrorText() + ": " + ((SugarImporterException) e).getPosition();
            }
            throw new IllegalArgumentException("Structure cannot be imported: " + message);
          }
          break;
        }
      }
      if (sugarStructure == null && !encoding.equalsIgnoreCase("gws")) {
        // encoding is not supported
        throw new IllegalArgumentException("Encoding " + encoding + " is not supported");
      }
    } else {
      String structure;
      if (encoding != null && encoding.equalsIgnoreCase("gws")) { // glycoworkbench
                                                                  // encoding
        structure = new GWSImporter().parse(glycan.getSequence());
        // logger.debug("converted from gws: {}", structure);
      } else {
        // assume GlycoCT encoding
        structure = glycan.getSequence();
      }
      sugarStructure = StructureParserValidator.parse(structure);
    }

    if (StructureParserValidator.isValid(sugarStructure)) {
      return sugarStructure;
    } else {
      throw new IllegalArgumentException("Validation error, please submit a valid structure");
    }
  }
  
  @RequestMapping(value="/{accessionNumber}/image", method=RequestMethod.GET, produces={MediaType.IMAGE_PNG_VALUE, MediaType.APPLICATION_XML_VALUE, MediaType.IMAGE_JPEG_VALUE})
  @ApiOperation(value="Retrieves glycan image by accession number", response=Byte[].class)
@ApiResponses (value ={@ApiResponse(code=200, message="Success"),
    @ApiResponse(code=400, message="Illegal argument"),
    @ApiResponse(code=404, message="Glycan does not exist"),
    @ApiResponse(code=500, message="Internal Server Error")})
  @Transactional
  public @ResponseBody ResponseEntity<byte[]> getGlycanImage (
      @ApiParam(required=true, value="id of the glycan") 
      @PathVariable("accessionNumber") 
      String accessionNumber,
      @ApiParam(required=false, value="format of the the glycan image", defaultValue="png") 
      @RequestParam("format") 
      String format,
      @ApiParam(required=false, value="notation to use to generate the image", defaultValue="cfg") 
      @RequestParam("notation") 
      String notation,
      @ApiParam(required=false, value="style of the image", defaultValue="compact") 
      @RequestParam("style") 
      String style
      ) {
    logger.debug("Start Image generation of Accession:>" + accessionNumber + "<");

//  SparqlEntity se = sparqlEntityFactory.create();
    SparqlEntity se = new SparqlEntity();
  se.setValue(Saccharide.PrimaryId, accessionNumber);
  
  SparqlEntityFactory.set(se);
//  sparqlEntityFactory.set(se);
//  logger.debug("sparqlEntityFactory:>" + sparqlEntityFactory + "<");
  logger.debug("SparqlEntity:>" + se + "<");
    SparqlEntity glycanEntity;
    try {
      glycanEntity = glycanProcedure.searchSequenceByFormatAccessionNumber(accessionNumber, GlyConvert.WURCS);
    } catch (SparqlException e1) {
       logger.debug(e1.getMessage());
       HttpHeaders headers = new HttpHeaders();
       byte[] bytes = new byte[0];
       return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    String sequence = glycanEntity.getValue(GlycoSequence.Sequence);
    byte[] bytes = null;
    logger.debug("image for " + accessionNumber + " sequence:>" + sequence + "<");
    if (StringUtils.isNotBlank(sequence)) {
      try {
      bytes = imageGenerator.getImage(sequence, format, notation, style);
      } catch (Exception e) {
        // have to do this because it just throws Exceptions
      }
    } else {
      try {
        glycanEntity = glycanProcedure.searchSequenceByFormatAccessionNumber(accessionNumber, GlyConvert.GLYCOCT);
      } catch (SparqlException e) {
        logger.debug(e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      }      
      sequence = glycanEntity.getValue(GlycoSequence.Sequence);
      logger.debug("image for " + accessionNumber + " sequence:>" + sequence + "<");
      
      sequence = glycanEntity.getValue("GlycoCTSequence");
      try {
        bytes = imageGenerator.getGlycoCTImage(sequence, format, notation, style);
      } catch (Exception e) {
        logger.error(e.getMessage());
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
    
    if (null == bytes || bytes.length < 1) {
      // our image generator returned null, return a default image.
      BufferedImage defaultImage;
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      try {
      defaultImage = ImageIO.read(GlycanController.class.getResourceAsStream("/notfound.png"));
      ImageIO.write( defaultImage  , "png", byteArrayOutputStream);
      } catch (IOException e) {
        HttpHeaders headers = new HttpHeaders();
        return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.INTERNAL_SERVER_ERROR);
      }
      bytes = byteArrayOutputStream.toByteArray();
    }
      
  HttpHeaders headers = new HttpHeaders();
    if (format == null || format.equalsIgnoreCase("png")) {    
      headers.setContentType(MediaType.IMAGE_PNG);
    } else if (format.equalsIgnoreCase("svg")) {
      headers.setContentType(MediaType.APPLICATION_XML);
    } else if (format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("jpeg")) {
      headers.setContentType(MediaType.IMAGE_JPEG);
    }
    
    logger.debug("End Image generation of Accession:>" + accessionNumber + "<");

  return new ResponseEntity<byte[]>(bytes, headers, HttpStatus.OK);
  }
}