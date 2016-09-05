package org.glytoucan.api.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glytoucan.api.Application;
import org.glytoucan.model.RegisterContributorRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
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

import com.github.fromi.openidconnect.security.UserInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { Application.class })
@WebAppConfiguration
@EnableAutoConfiguration
public class ContributorControllerTest {
	public static Log logger = (Log) LogFactory
			.getLog(ContributorControllerTest.class);

	@Autowired
	private WebApplicationContext wac;
	
    private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private MockMvc mockMvc;

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));
    
    static final String token = "ya29.CjBVA84kJF7CplkqvzP2YtuOnp22cBaLW8nNl6jIO4Lma01zCwdO52SBK60xzHetohg";

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

    
}
