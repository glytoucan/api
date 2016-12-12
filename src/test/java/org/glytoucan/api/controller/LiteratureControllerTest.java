package org.glytoucan.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.service.ContributorProcedure;
import org.glycoinfo.rdf.service.exception.ContributorException;
import org.glycoinfo.rdf.service.impl.LiteratureProcedure;
import org.glytoucan.api.Application;
import org.glytoucan.client.model.RegisterContributorResponse;
import org.glytoucan.client.model.RegisterLiteratureRequestResponse;
import org.glytoucan.model.Message;
import org.glytoucan.model.RegisterContributorRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
@EnableAutoConfiguration
public class LiteratureControllerTest {
	public static Log logger = (Log) LogFactory
			.getLog(LiteratureControllerTest.class);

	@Autowired
	LiteratureController controller;
	
	@Autowired
	private WebApplicationContext wac;
	
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private MockMvc mockMvc;

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    
    static final String token = "b83f8b8040a584579ab9bf784ef6275fe47b5694b1adeb82e076289bf17c2632";

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {
        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }
    
	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

	@Test
	@Transactional
	public void testRegistriesOk() throws Exception {
		RegisterLiteratureRequestResponse req = new RegisterLiteratureRequestResponse();
		req.setAccessionNumber("G00029MO");
		req.setPublicationId("9565568");
    req.setContributorId("12345");
		logger.debug("start");
		mockMvc.perform(post("/literature/register").with(httpBasic("815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d", token)).contentType(contentType).content(this.json(req)))
				.andExpect(status().isOk());
	}

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

	@Test
	@Transactional
	public void testRegistriesTestUserOk() throws Exception {
		RegisterContributorRequest rcr = new RegisterContributorRequest();
		rcr.setName("testuser");
    rcr.setEmail("testglytoucan@gmail.com");
		logger.debug("start");
		mockMvc.perform(post("/contributor/register").with(httpBasic("815e7cbca52763e5c3fbb5a4dccc176479a50e2367f920843c4c35dca112e33d", token)).contentType(contentType).content(this.json(rcr)))
				.andExpect(status().isOk());
	}

	
	 @Test
	 @Transactional
	  public void testRegirationDirect() throws Exception {
			RegisterLiteratureRequestResponse req = new RegisterLiteratureRequestResponse();
			req.setAccessionNumber("G00029MO");
			req.setPublicationId("9565568");
      req.setContributorId("12345");
	    logger.debug("start");
	    ResponseEntity<RegisterLiteratureRequestResponse> result =  controller.register(req);
	    logger.debug(result.getStatusCode());
	    RegisterLiteratureRequestResponse response = result.getBody();
	    logger.debug(response.getAccessionNumber());
	    Assert.assertEquals("G00029MO", response.getAccessionNumber());
	    Assert.assertEquals("9565568", response.getPublicationId());
	  }
}
