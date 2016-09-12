package org.glytoucan.api.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.service.GlycanProcedure;
import org.glytoucan.api.Application;
import org.glytoucan.model.GlycanRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
public class RegistriesControllerTest {
	public static Log logger = (Log) LogFactory
			.getLog(RegistriesControllerTest.class);


  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(),
      Charset.forName("utf8"));
  
  static final String token = "JDUkMjAxNjA5MDUwOTM5MjMkVWZzaHNyRVFkMVl4Umx0MjJiczVyZFZVNDQ5bUJBVTBoQTdaeGpiUkRpMw==";

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setup() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
	}

  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  
	@Test
	@Transactional
	public void testRegistriesStart() throws Exception {
//	    UserInfo userinfo = new UserInfo("testid", "testname", "Johnny", "", "", "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", null, "glytoucan@gmail.com", "true");

	  GlycanRequest request = new GlycanRequest();
	  request.setPublicDatabaseStructureId("9999");
	  request.setSequence("test");
//		mockMvc.perform(post("/glycan/register").with(csrf()).with(httpBasic("254", "JDUkMjAxNjA5MDUwOTQyMDQkTzhsamx3bG1URzZnTUlPZGcwOWhFc0NiNmxpTWRlWWFrTUFTQTAzNmhaMQ==")))
//				.andExpect(status().isOk());
		
    mockMvc.perform(post("/glycan/register").with(httpBasic("1", token)).contentType(contentType).content(this.json(request)))
    .andExpect(status().isOk());
	}

	@Test
	@Transactional
	public void testRegistries() throws Exception {
//	    UserInfo userinfo = new UserInfo("testid", "testname", "Johnny", "", "", "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg", null, "glytoucan@gmail.com", "true");

		mockMvc.perform(post("/glycan/register").with(csrf()).with(httpBasic("254", "JDUkMjAxNjA5MDUwOTQyMDQkTzhsamx3bG1URzZnTUlPZGcwOWhFc0NiNmxpTWRlWWFrTUFTQTAzNmhaMQ==")))
				.andExpect(status().isOk());
	}

  protected String json(Object o) throws IOException {
    MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
    this.mappingJackson2HttpMessageConverter.write(
            o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
    return mockHttpOutputMessage.getBodyAsString();
}

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {
      this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
              hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

      Assert.assertNotNull("the JSON message converter must not be null",
              this.mappingJackson2HttpMessageConverter);
  }

}