package org.glytoucan.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import org.glytoucan.api.Application;
import org.glytoucan.client.model.RegisterContributorResponse;
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
import org.springframework.http.converter.HttpMessageNotWritableException;
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
public class ContributorControllerTest {
	public static Log logger = (Log) LogFactory
			.getLog(ContributorControllerTest.class);

	@Autowired
	ContributorController controller;
	
	@Autowired
	private WebApplicationContext wac;
	
	@Autowired
	ContributorProcedure contributorProcedure;
	
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private MockMvc mockMvc;

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    
    static final String token = "JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";

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
		RegisterContributorRequest rcr = new RegisterContributorRequest();
		rcr.setName("Administrator");
		logger.debug("start");
		mockMvc.perform(post("/contributor/register").with(httpBasic("1", token)).contentType(contentType).content(this.json(rcr)))
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
		logger.debug("start");
		mockMvc.perform(post("/contributor/register").with(httpBasic("1", token)).contentType(contentType).content(this.json(rcr)))
				.andExpect(status().isOk());
	}

	
	 @Test
	 @Transactional
	  public void testRegirationDirect() throws Exception {
	    RegisterContributorRequest rcr = new RegisterContributorRequest();
	    rcr.setName("testuser");
	    logger.debug("start");
	    ResponseEntity<RegisterContributorResponse> result =  controller.register(rcr);
	    logger.debug(result.getStatusCode());
	    RegisterContributorResponse response = result.getBody();
	    logger.debug(response.getContributorId());
	    Assert.assertEquals(rcr.getName(), response.getName());
      Assert.assertNotNull(response.getContributorId());
	  }

   @Test
   @Transactional
    public void testProcedureDirect() throws Exception {
     String name = "testname";
     logger.debug("name:>" + name);
     
     Message msg = new Message();
     msg.setMessage("");
     String result = null;
     try {
       result = contributorProcedure.addContributor(name);
       msg.setMessage(result);
     } catch (ContributorException e) {
       logger.error(e.getMessage());
       msg.setMessage(name + " not accepted");
       msg.setError(e.getMessage());
       msg.setPath("/contributor/register");
       msg.setStatus(HttpStatus.BAD_REQUEST.toString());
       msg.setTimestamp(new Date());
     }

     msg.setError("");
     msg.setPath("/contributor/register");
     msg.setStatus(HttpStatus.OK.toString());
     msg.setTimestamp(new Date());
    }
}
