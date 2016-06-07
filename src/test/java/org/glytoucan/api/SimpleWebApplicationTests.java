package org.glytoucan.api;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glytoucan.model.Message;
import org.glytoucan.model.spec.GlycanClientRegisterSpec;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

/**
 * Basic integration tests.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
@DirtiesContext
public class SimpleWebApplicationTests {

	private static final Log logger = LogFactory.getLog(SimpleWebApplicationTests.class);
	@Value("${local.server.port}")
	private int port;

	@Test
	public void testStatusCode() throws Exception {
		ResponseEntity<String> entity = new TestRestTemplate()
				.getForEntity("http://localhost:" + port, String.class);
		
		logger.error("log error level test");
		assertEquals(HttpStatus.FOUND, entity.getStatusCode());
	}

	@Test
	public void testUnauthorized() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.TEXT_HTML));
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

		ResponseEntity<String> responseEntity = new TestRestTemplate().exchange(
				"http://localhost:" + port + "/does-not-exist", HttpMethod.GET,
				requestEntity, String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	 @Test
	  public void testGlycanImageGenStatusCode() throws Exception {
//	   /glycans/G00030MO
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
	    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

	    // TODO: figure out a clean way to post test
	     
//	    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	  }
	 
//   curl -X POST --header 'Content-Type: application/xml' --header 'Accept: text/html' -d '{
//   "sequence": "WURCS=2.0/4,7,6/[a2122h-1x_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a1122h-1b_1-5][a1122h-1a_1-5]/1-2-3-4-2-4-4/a4-b1_b4-c1_c3-d1_c6-f1_e1-d2|d4_g1-f3|f6"
//  }' 'http://localhost:7357/glycans/image/glycan?format=png¬ation=cfg&style=compact'
	 @Test
   public void testGlycanStatusCode() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
    HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

    ResponseEntity<String> responseEntity = new TestRestTemplate().exchange(
        "http://localhost:" + port + "/glycans/G00030MO", HttpMethod.GET,
        requestEntity, String.class);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }
	 
	
	
	 @Test
	  public void testStatusCodeImage() throws Exception {
	   //http://api.glytoucan.org/glycans/G00055MO/image?style=extended&format=png&notation=cfg
	    ResponseEntity<String> entity = new TestRestTemplate()
	        .getForEntity("http://localhost:" + port + "/glycans/G00055MO/image?style=extended&format=png&notation=cfg", String.class);
	    
	    logger.error("log error level test");
	    assertEquals(HttpStatus.OK, entity.getStatusCode());
	
	 }
	 
	 @Test
	 public void testSystem() {
	   System.out.println(
	       Iterables.class.getProtectionDomain().getCodeSource().getLocation()
	   );
//     System.out.println(
//         org.slf4j.LoggerFactory.class.getProtectionDomain().getCodeSource().getLocation()
//     );
	 }
	  /**
	   * 
	   * @param requestMappingUrl
	   *            should be exactly the same as defined in your RequestMapping
	   *            value attribute (including the parameters in {})
	   *            RequestMapping(value = yourRestUrl)
	   * @param serviceReturnTypeClass
	   *            should be the the return type of the service
	   * @param objectToPost
	   *            Object that will be posted to the url
	   * @return
	   */
//	  protected <T> T postEntity(final String requestMappingUrl, final Class<T> serviceReturnTypeClass, final Object objectToPost) {
//	    final TestRestTemplate restTemplate = new TestRestTemplate();
//	    final ObjectMapper mapper = new ObjectMapper();
//	    try {
//	      final HttpEntity<String> requestEntity = new HttpEntity<String>(mapper.writeValueAsString(objectToPost));
//	      final ResponseEntity<T> entity = restTemplate.postForEntity(getBaseUrl() + requestMappingUrl, requestEntity, serviceReturnTypeClass);
//	      return entity.getBody();
//	    } catch (final Exception ex) {
//	      // Handle exceptions
//	    }
//	    return null;
//	  }
	  
//	  private ResponseEntity<Message> submit(HttpEntity<?> requestEntity, String cmd) {
//	    logger.debug("request:>" + cmd);
//	    return restTemplate.exchange(cmd, HttpMethod.POST, requestEntity, Message.class);
//	  }
//
//    private String getBaseUrl() {
//      // TODO Auto-generated method stub
//      return null;
//    }
}
