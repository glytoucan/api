package org.glytoucan.api.controller;


import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.intThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.Matchers.containsString;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.glycoinfo.batch.search.wurcs.SubstructureSearchSparql;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.convert.GlyConvertConfig;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.service.impl.ContributorProcedureConfig;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glycoinfo.rdf.service.impl.LiteratureProcedureConfig;
import org.glycoinfo.vision.generator.ImageGenerator;
import org.glycoinfo.vision.util.Encoding;
import org.glytoucan.admin.client.config.AdminServerConfiguration;
import org.glytoucan.admin.client.config.UserClientConfig;
import org.glytoucan.api.Application;
import org.glytoucan.model.Glycan;
import org.glytoucan.model.GlycanInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes={VirtSesameTransactionConfig.class, GlycanProcedureConfig.class, GlycanControllerConfig.class, GlyConvertConfig.class, UserClientConfig.class, AdminServerConfiguration.class, ContributorProcedureConfig.class, LiteratureProcedureConfig.class})
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class GlycanControllerTest {
  
  private static final Log logger = LogFactory.getLog(GlycanControllerTest.class);
  
  @Autowired
  GlycanController gc;

  @Autowired
  private WebApplicationContext wac;

  private HttpMessageConverter mappingJackson2HttpMessageConverter;

  private MockMvc mockMvc;

  private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
      MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  static final String token = "ya29.CjBVAzYm27tjUA1IXHV7NlH_doYMYUa7go9MCHbJSJLmwoL1RLtapF4kdpLqZ9OjHYw";

  @Autowired
  void setConverters(HttpMessageConverter<?>[] converters) {
    this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
        .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

    Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
  }

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).apply(springSecurity()).build();
  }

  @Bean
//@Scope(value = WebApplicationContext.SCOPE_REQUEST)
//@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
ImageGenerator imageGenerator() {
  return new ImageGenerator();
}
  
  
  @Test
  public void testGlycoCTImage() throws Exception {
  String sequence = "RES\n"
  + "1b:x-dglc-HEX-x:x\n"
  + "2b:b-dgal-HEX-1:5\n"
  + "3b:a-dgal-HEX-1:5\n"
  + "4b:b-dgal-HEX-1:5\n"
  + "5s:n-acetyl\n"
  + "LIN\n"
  + "1:1o(4+1)2d\n"
  + "2:2o(3+1)3d\n"
  + "3:3o(3+1)4d\n"
  + "4:4d(2+1)5n";
    
    String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC";
    // given().body(output2).with().contentType(JSON).then().statusCode(HttpStatus.SC_OK).and().expect().body(equalTo(image)).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended");

    logger.debug("sequence:>" + sequence);
    GlycanInput glycan = new GlycanInput();
    glycan.setFormat(GlyConvert.GLYCOCT);
    glycan.setSequence(sequence);
    ResponseEntity<byte[]> response = gc.getGlycanImageByStructure(glycan, null, "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC", strResult);
  }

  @Test
  public void testWurcsImage() throws Exception {
    String sequence = "WURCS=2.0/2,2,1/[a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5]/1-2/a4-b1";
    String output2 = "{\"encoding\":\"glycoct\",\"structure\":\"" + sequence + "\"}";

    String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC";
    // given().body(output2).with().contentType(JSON).then().statusCode(HttpStatus.SC_OK).and().expect().body(equalTo(image)).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended");

    logger.debug("sequence:>" + sequence);
    GlycanInput glycan = new GlycanInput();
    glycan.setFormat(GlyConvert.WURCS);
    glycan.setSequence(sequence);
    ResponseEntity<byte[]> response = gc.getGlycanImageByStructure(glycan, null, "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAALoAAABSCAIAAABse1lJAAADyUlEQVR42u2bIW/qUBSAryQEMYlAkAyBqJioQE5UIBCISQSiYgIxgZhATCAmJhCIiYkJ5MRERcUSxCRioqISsR+AqCB5iPcOrwlZNthKoS3cfV+uJKH3nq/nnHvbqr8AkVEsAaALoAugC6ALoAugC0sA6ALoAugC6ALoAujCEgC6ALoAugC6ALoAurAEgC6ALoAugC6ALoAuLAGgC6ALoAugCyTIYrH4LbpMJpNer2dZ1umpKpWUYRj1er3f7/u+n+q090EmAXt7ezs7O8vKmPTmPB6PazVVqaheT7mu8n01nSrPU46jrq6W6og3shYp6vJnt6EyySvVavXu7k7n7CKT7Ha75bIajdT/f1wzFgs1HKpiUaWzFkeqi+u6oovOxUjm1mxK5lCz2UZXVuP9XZmm6nQ66LKW6+vrm5sbnVtdiX2jsUweP7oSjiBYGpN0jjlSXdrt9u3trba6OI4jzYoYENGVVY4pFouJ9jFHqott2/f393rqImXIMJad7FauhEP6GOl80eUTklpSqNTZ6CJ9mWyFYrgSdr6lUim53fXxtrqmaeqpi2TOwSCmLjJkd93v99HlI0EQFAqF+XyuoS6y5fO8+LpIFUuuHh2pLsL5+fnz87OGuuTzWze5H4eoZhgGunxiMBjI/khDXf4vaPwh+6OE2VWXrMjn8xo+BMjlcvN5fF18X0k5I7t8pVarvby86KZLpVKRkMfWxXWVZVno8hXZAVxeXuqmS6vVeniIr0uvJ6O3p5qolS6+75fLZd10eXp6sqz4ukhymkwm+8giuukinJycTKdTrXSZz+dyE7y+xnFlNFJSodfGPvq7JuHP0tEl5fdgpEzL3aiVLsLj46NpbvF8MRyzmRLPxuPxprISJSoff5yOLtGvbXe63W4mzxoTn9jFxYVtb3f8X6/XZTm+N+CX69LpdDJ5kyHxiQVBIGVFjImSY4JANRqNZrO59lzhewM2lYa1NSJRXVIIW7vdHg6HGuoSGiM5xjTN7/sYx1m2t3LfbDqDWvUiW4UkzeySTmoJm8Ld9wEHqsuqj5FJSo8mu2vfV+EJnqQTz1ODwbKxNQzDcZyIgY8elZSLUQoJRspQVs+lU90Nym0h/Xyr1ZIsksvlZFkLhUK1WrVt23XdHw+2Y4Rk025l718CpKbLbDYrFoue5+mvyx4P3LI99sj22sSYzKZ5jLpk+JnPMV7b79XlYINxyNdGMeLa0AXQBdAF0AUAXQBdAF0AXQBdAF0A0AXQBdAF0AXQBdAFAF0AXQBdAF0AXQBdANAF0AXQBQ6ef8TncwW7FfIPAAAAAElFTkSuQmCC", strResult);
  }
  
  @Test
  @Transactional
  public void testImagebyAccessionNumber() throws Exception {
    String accessionNumber = "G00030MO";

    logger.debug("accessionNumber:>" + accessionNumber);
    ResponseEntity<byte[]> response = gc.getGlycanImage(accessionNumber, null, "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("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", strResult);
  }
  
  @Test
  @Transactional
  public void testImageG15021LG() throws Exception {
    String accessionNumber = "G15021LG";

    logger.debug("accessionNumber:>" + accessionNumber);
    ResponseEntity<byte[]> response = gc.getGlycanImage(accessionNumber, "png", "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAAIYAAABSCAIAAAAetDv/AAADCElEQVR42u3avYsaQRgG8KcUsbBciBBJLIRYCCfEUogQIRIsLC0kWFjIVVvIYWFhYeGBBxZe5/UWFltYCPsHWFhYWFxhf4XCCSdoYV52kyPNET/Q2Uyeh+nvdn77zsw7K3aMxwJOAUkYkpCEIQlJGJKQhCEJQxKSMCQhCUMSkjAkIQlDEoYkJGFIQhKGJCRhSMKQhCQMSUiiYbbb7f9CMh6Pa7VaOp0GPgDvYrFYJpNpNBqz2cw7HpPJJB6Pq1K5HIlt28Bn4CNwA1jAFHgEJsAAuBYesZG58EJ9RKPRVqulc5XIQ5qmCbwHHoDNG+MFuAMMhXPhZjgcConOC5c8G/Ad+Ao8ve3xOubAVaVSUUhSrVbr9brO27vML/DNKYLNfmMhKgprpVgsNptNbUksy3I2j8XeHr9qxTAMVftKqVTqdrt6kjhL1idn994cPu5kt1cyI1IialfOM5LIPukcsTZHjZdQKKTkZCz/diKR0JNEVgDg9lgSGdfSr1x+RlarVSAQWK/XGpLIUdJpO44mGahau1Kp1GAw0JAE8B++sf85JtLbK5mUdrst5y4tSXCCh9ujKIvf79fwQsXn8wHPJ5BMZelT9aomk8nRaKQbSSQScS6yjiax0um0KhI5WZTLZd1ICoUCcH8CyU2tVlNFIufvcDisG0m/3we+HE0iRTYejxX2B8FgcD6fa0UiR3t50QD7KJIHWc13SiPLprxVut1x9Xo94OqQO0d3PImlbdtqSUzTVHL/ePab4Hw+D/w46CpFOkSZjp3qVCoVJbf0ZydZrVayBDkq+9TKIpvN5nI5hV+QXiPdYqfT0ZDEVZFaSSQSf9tXBrKly7vpBQ93I1Ryvrjct3fZV+QhnR9C3Dv9yvPvD1YT4FYqKRaLWZa180ZkyVJ1H4wLv3pyhpF+RarB6e0RCASkRS+VSsPh0AvF4Wa5XBqGMZ1O9Sf5hyIqqv40STwXkpCEIQlJGJKQhCEJSRiSMCQhCUMSkjAkIQlDEpIwJGFIQhKGJCRhSEIShiQMSUjCHJSfSQcIp0aAOFgAAAAASUVORK5CYII=", strResult);
  }
  
  @Test
  @Transactional
  public void testImageG00029MO() throws Exception {
    String accessionNumber = "G00029MO";

    logger.debug("accessionNumber:>" + accessionNumber);
    ResponseEntity<byte[]> response = gc.getGlycanImage(accessionNumber, "png", "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("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", strResult);
  }  
  
  public static String encodeToString(BufferedImage image, String type) {
    String imageString = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      ImageIO.write(image, type, bos);
      byte[] imageBytes = bos.toByteArray();

      imageString = Base64.encodeBase64String(imageBytes);

      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageString;
  }
  
  @Test
  public void testG00003VQGlycoct() throws Exception {

    // glycoct conversion to wurcs
    SelectSparql compare = new SubstructureSearchSparql();
    String G00003VQ = "WURCS=2.0/4,5,5/[12112h-1b_1-5_2*NCC/3=O][12122a-1b_1-5][22112h-1a_1-5][12122h-1b_1-5]/1-2-1-3-4/a3-b1_b4-c1_c4-d1_d3-e1_a1-e4~n";
    SelectSparql result = gc.substructureSearch(G00003VQ, "wurcs");
    logger.debug("RESULT>" + result.getSparql() +"<");
  }
  
  @Test
  public void testG00012MOGlycoct() throws Exception {
    // glycoct conversion to wurcs
    SelectSparql compare = new SubstructureSearchSparql();
    String G00003VQ = "WURCS=2.0/4,4,3/[u2122h][12112h-1b_1-5][22112h-1a_1-5][12112h-1b_1-5_2*NCC/3=O]/1-2-3-4/a4-b1_b3-c1_c3-d1";
    SelectSparql result = gc.substructureSearch(G00003VQ, "wurcs");
    logger.debug("RESULT>" + result.getSparql() +"<");
  }
  
//@Test
public void testG00032MOGlycoct() throws Exception {

  // glycoct conversion to wurcs
  SelectSparql compare = new SubstructureSearchSparql();
  String G00032MO = "RES\n" + "1b:b-dglc-HEX-1:5\n" + "2s:methyl\n"
      + "3s:sulfate\n" + "4b:b-dgro-HEX-1:5|2:d|3:d|4:d|6:a\n"
      + "5b:a-dglc-HEX-1:5\n" + "6b:o-dman-HEX-0:0|1:aldi\n"
      + "7b:x-dgro-dgal-NON-2:6|2:keto|1:a|3:d\n" + "8s:n-acetyl\n"
      + "9b:b-dara-HEX-2:5|2:keto\n" + "10b:o-dgro-TRI-0:0|1:aldi\n"
      + "11b:b-dxyl-HEX-1:5|1:keto|4:keto|6:d\n"
      + "12b:b-drib-HEX-1:5|2:d|6:d\n" + "13b:b-dglc-HEX-1:5\n"
      + "14s:n-acetyl\n" + "15s:phospho-ethanolamine\n"
      + "16b:b-HEX-x:x\n" + "17b:b-SUG-x:x\n" + "18b:o-SUG-0:0\n"
      + "19b:a-dery-HEX-1:5|2,3:enx\n"
      + "20b:a-dman-OCT-x:x|1:a|2:keto|3:d\n" + "LIN\n"
      + "1:1o(2+1)2n\n" + "2:1o(3+1)3n\n" + "3:1o(4+1)4d\n"
      + "4:4o(6+1)5d\n" + "5:5o(4+1)6d\n" + "6:6o(6+2)7d\n"
      + "7:7d(5+1)8n\n" + "8:7o(8+2)9d\n" + "9:9o(4+1)10d\n"
      + "10:10o(3+1)11d\n" + "11:11o(3+1)12d\n" + "12:12o(4+1)13d\n"
      + "13:13d(2+1)14n\n" + "14:13o(6+1)15n\n" + "15:15n(1+1)16o\n"
      + "16:16o(4+1)17d\n" + "17:17o(-1+1)18d\n"
      + "18:18o(-1+1)19d\n" + "19:19o(4+2)20d\n";
  SelectSparql result = gc.substructureSearch(G00032MO, "glycoct");
  logger.debug(result.getSparql());
}


//@Test
//public void testWurcsSubstructure() throws Exception {
//  String url = "/glycans/sparql/substructure?sequence=WURCS%3D2.0%2F2%2C2%2C1%2F%5Ba1122h-1x_1-5%5D%5Ba2112h-1x_1-5_2*NCC%2F3%3DO%5D%2F1-2%2Fa%3F-b1&format=wurcs";
//      given().redirects().follow(false).when().get(url).then().statusCode(HttpStatus.SC_OK).and().body(containsString("http://rdf.glycoinfo.org/glycan/wurcs/2.0/monosaccharide/a1122h-1x_1-5"));
//}

//@Test
public void postImage() {
  String sequence = "RES\\n"
    + "1b:x-dglc-HEX-x:x\\n"
    + "2b:b-dgal-HEX-1:5\\n"
    + "3b:a-dgal-HEX-1:5\\n"
    + "4b:b-dgal-HEX-1:5\\n"
    + "5s:n-acetyl\\n"
    + "LIN\\n"
    + "1:1o(4+1)2d\\n"
    + "2:2o(3+1)3d\\n"
    + "3:3o(3+1)4d\\n"
    + "4:4d(2+1)5n";
String output2 = "{\"encoding\":\"glycoct\",\"structure\":\"" + sequence + "\"}"; 
//    given().redirects().follow(false).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended", "{\"encoding\":\"glycoct\",\"structure\":\"" + sequence + "\"}").then().statusCode(HttpStatus.SC_OK);
    
String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC";
    given().body(output2).with().contentType(JSON).then().statusCode(HttpStatus.SC_OK).and().expect().body(equalTo(image)).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended");
}

@Test
public void testImage() throws Exception {
  String sequence = "RES\n"
    + "1b:x-dglc-HEX-x:x\n"
    + "2b:b-dgal-HEX-1:5\n"
    + "3b:a-dgal-HEX-1:5\n"
    + "4b:b-dgal-HEX-1:5\n"
    + "5s:n-acetyl\n"
    + "LIN\n"
    + "1:1o(4+1)2d\n"
    + "2:2o(3+1)3d\n"
    + "3:3o(3+1)4d\n"
    + "4:4d(2+1)5n";
String output2 = "{\"encoding\":\"glycoct\",\"structure\":\"" + sequence + "\"}"; 
    
String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC";
//    given().body(output2).with().contentType(JSON).then().statusCode(HttpStatus.SC_OK).and().expect().body(equalTo(image)).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended");

GlycanInput glycan = new GlycanInput();
glycan.setFormat("glycoct");
glycan.setSequence(sequence);
gc.getGlycanImageByStructure(glycan, null, "cfg", "extended");
}


@Test
@Transactional
public void testListGlycans() throws Exception {
  gc.listGlycans("full", "10", "10");
}

@Test
@Transactional
public void testGetImage() throws Exception {
  logger.debug("start");
  mockMvc.perform(get("/glycans/G00055MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk());
}

@Test
@Transactional
public void testGetImageData() throws Exception {
  logger.debug("start");
  MvcResult result = mockMvc.perform(get("/glycans/G00055MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk()).andReturn();
  logger.debug(result.getResponse().getContentType());
  logger.debug(result.getResponse().getBufferSize());

//  ByteArrayOutputStream bos = new ByteArrayOutputStream(result.getResponse().getOutputStream());
//
//  try {
//    ImageIO.write(image, "png", bos);
//    byte[] imageBytes = bos.toByteArray();
//
//    imageString = Base64.encodeBase64String(imageBytes);
//
//    bos.close();
//  } catch (IOException e) {
//    e.printStackTrace();
//  }
//
//  
//  String strResult = Encoding.encodeToString(img, "png");
//  logger.debug(strResult);
}

@Test
@Transactional
public void testLoad() throws Exception {
  logger.debug("start");
  for(int i = 0; i < 5; i++) {
  MvcResult result = mockMvc.perform(get("/glycans/G00055MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk()).andReturn();
  logger.debug(result.getResponse().getContentType());
  Assert.assertEquals(4096, result.getResponse().getBufferSize());
  result = mockMvc.perform(get("/glycans/G00026MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk()).andReturn();
  logger.debug(result.getResponse().getContentType());
  Assert.assertEquals(4096, result.getResponse().getBufferSize());
  result = mockMvc.perform(get("/glycans/G00030MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk()).andReturn();
  logger.debug(result.getResponse().getContentType());
  Assert.assertEquals(4096, result.getResponse().getBufferSize());
  result = mockMvc.perform(get("/glycans/G00065MO/image?style=extended&format=png&notation=cfg"))
  .andExpect(status().isOk()).andReturn();
  logger.debug(result.getResponse().getContentType());
  Assert.assertEquals(4096, result.getResponse().getBufferSize());
  }

//  ByteArrayOutputStream bos = new ByteArrayOutputStream(result.getResponse().getOutputStream());
//
//  try {
//    ImageIO.write(image, "png", bos);
//    byte[] imageBytes = bos.toByteArray();
//
//    imageString = Base64.encodeBase64String(imageBytes);
//
//    bos.close();
//  } catch (IOException e) {
//    e.printStackTrace();
//  }
//
//  
//  String strResult = Encoding.encodeToString(img, "png");
//  logger.debug(strResult);
}

protected String json(Object o) throws IOException {
  MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
  this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
  return mockHttpOutputMessage.getBodyAsString();
}

@Test
@Transactional
public void testListOk() throws Exception {
  logger.debug("start");
  mockMvc.perform(get("/glycans/list?limit=100&offset=100"))
  .andExpect(status().isOk());
}

@Test
@Transactional
public void testListDefault() throws Exception {
  
  int limit = 100;
  


      MvcResult res = mockMvc.perform(
          get("/glycans/list?limit=" + limit + "&offset=0").accept(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(status().isOk())
          .andExpect(content().contentType(contentType))
          .andExpect(jsonPath("$.items", hasSize(limit)))
          .andExpect(jsonPath("$.items[0]", is("G76789ES")))
//          .andExpect(jsonPath("$[0].accessionNumber", is("G76895ES")))
//          .andExpect(jsonPath("$[1].sequence", is("Gal")));
          .andReturn()          
          ;
      logger.debug(res);
  }

@Test
@Transactional
public void testListFull() throws Exception {
  
  int limit = 100;
//Glycan g = new Glycan();
//g.setAccessionNumber("test");
//g.setStructure("test");
//g.setDateEntered(new Date(0));
//g.setMass("0");
  
      MvcResult res = mockMvc.perform(
          get("/glycans/list?payload=full&limit=" + limit + "&offset=0").accept(MediaType.APPLICATION_JSON_UTF8))
          .andExpect(status().isOk())
          .andExpect(content().contentType(contentType))
          .andExpect(jsonPath("$.items", hasSize(limit)))
          .andExpect(jsonPath("$.items[0].accessionNumber", is("G34073UX")))
          .andExpect(jsonPath("$.items[0].structure", is("WURCS=2.0/4,4,3/[a2112h-1a_1-5_2*NCC/3=O][a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5][a2112h-1a_1-5]/1-2-3-4/a6-b1_b4-c1_c3-d1")))
          .andExpect(jsonPath("$.items[0].mass", is(748.27495657)))
          .andExpect(jsonPath("$.items[0].contributor", is("Administrator")))
          .andReturn()          
          ;
      logger.debug(res);
  }



@Test
public void testWurcsImagePost() throws Exception {
  String sequence = "WURCS=2.0/2,2,1/[a2122h-1b_1-5_2*NCC/3=O][a2112h-1b_1-5]/1-2/a4-b1";
  String output2 = "{\"encoding\":\"glycoct\",\"structure\":\"" + sequence + "\"}";

  String image = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAFWElEQVR42u2cMWgjRxhGv1xljIorBTHEEBeCqDCcICoNEUQQE1y4dCGMChNEuEKFCSpcqHDhAx24cIgv2ODShQsdyGBQ4eIKFy5UqEih4rorLIjgBHaRjKQkGEda7a60s2vpPabcO3/Mzpt/ZjSS/gKAgBFdAIBmAGgGAGgGgGYAaAYAaAaAZgCAZgBoBoBmAIBmAGgGgGYAgGYAaAYAaAaAZgBoBgBoBoBmAGgGAGgGgGYAgGYAaAaAZgCAZgBoBmCVh4eHGdRM08BmX9zc3JRKpUwmM/jTyWQym82Wy+Vmsxn6ECHbhNze3q6uroZlWrCa9f9//82aZvV6PZ3WyopKJdVqajbVaqnRULWq16+1tCQzbsx7CuUNRTyb9K30tfSLVJUa0h/SrXQh/Sx9GWK2J3UskUgcHBzMZjWLvmbmBRSLxeVlnZ2NjPHwoMNDxeOy/J6in036SjqV7ke0z9JbKR7i+B5Qq9WMZjO7aIy4ZqbfNzZMNVC7PT7Mx49KpVQoFKyN4yhnk36Uvpc+jXbsv9aSXlnLNpTd3d29vb3ZPAKJvmbm3a+v9wqCyzydTm8025mbI55N+qFfrO7dtTtjWog1LZfL7e/vo1kImlWrVbPhMaPTUyRTN+LxeND7jYhn62/G7lw79k9Ns5BtFPl8/ujoCM1sa2aWPclk7xTBRyqzFzI7+0CXZFHOJn3TP+G4997eBprNAVPKwl21zqlmZk+cTvtMZUba0tJScKfVEc/WP1q899U+B5rNOXYqlUIz25qZVUSl4j/Y9rbK5fJ8ZpPe+NXMtFxw2RzodDqxWKzb7aKZVc0SiUSj4T/Yu3cBrs0inq3/sZhvzX4La924trZ2cXGBZlY1e/HC8wHD43Z52bvrMIfZpC+8H348bu+Dy+ZMpVLJ5XJoNlyz4Jgk2IcP85ttAsdMu1Z4LC4uctnKajVbWFjodv0Hu7qSWT7NZzbpzwk0uwwu21jS6fTV1RWa2dNsZWWl2fQf7PRUmUxmPrP1Ly761uz34LKNpVwu7+zsoJk9zba2to6P/QcrFFQqlWY729DON9mkXyfQ7Kfg+m0szWZzeXkZzaapmfO3ac7PzzMZ/8HMpH5zczOT2R4nHJpN+s63ZlPJNgkvX75stVpo5qSZ+++h/f8fPnmg2+2aie362k+qszOZVf60qoSFbF6/vzd4bOjDg2xS3Zdmp5P021QwS1YzU6DZGM2c51pP65+Tk5NUysPd3EFrt2XGWb1edx7K7uPZyea+3x4/PPQBk0165eXe8KB9GprNMsViMZQ7xPOrmWFzczOf93aVKZvNmlfl/Ccim20qmg2ySduerlmNymaZQqEQyjdinqtmnrZAox7odDpmGWNGs5u60elofX19Y2Nj6GcvXjULJZsbx9z8TsQgW980NzXtziGbZXK53OHhIZqN18xHKXMezWZuTqVSznuharV3tGDmwlFjxV81s5ltWkuAJ9nG7dMunLPZZLCxDOUM5llWs2ktzB7vN8wLMPvj4+Pej20MPh02JaLRUKXSO1RIJpPVatXlWiuy2SbfNI7K1j/lb/z7yfVd/97jGzfZbGKWi2Hd039Ov2zlfriMPc0bOtWdn59vbW2Z2bd/10GxWCyRSOTz+Vqt5mYyfnIEMuqP2s/mQzP3J5OT95sd2u12PB5vNBqzplkw5dHtcAnrV+i8TkBR67cZxpgW2ht/jppFUx76DWZEMwYK/YZm9hY/QL+hGQCgGQCaAaAZAKAZAJoBoBkAoBkAmgEAmgGgGQCaAQCaAaAZAJoBAJoBoBkAoBkAmgGgGQCgGQCaAaAZAKAZAJoBwFP+BvUCvvHguHARAAAAAElFTkSuQmCC";
  // given().body(output2).with().contentType(JSON).then().statusCode(HttpStatus.SC_OK).and().expect().body(equalTo(image)).when().post("/glycans/image/glycan?format=png&notation=cfg&style=extended");

  logger.debug("sequence:>" + sequence);
  GlycanInput glycan = new GlycanInput();
  glycan.setFormat(GlyConvert.WURCS);
  glycan.setSequence(sequence);
  ResponseEntity<byte[]> response = gc.getGlycanImageByStructure(glycan, null, "cfg", "extended");
  byte[] result = response.getBody();
  BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

  String strResult = encodeToString(img, "png");
  logger.debug("strResult:>" + strResult);
  logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
  Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAALoAAABSCAIAAABse1lJAAADyUlEQVR42u2bIW/qUBSAryQEMYlAkAyBqJioQE5UIBCISQSiYgIxgZhATCAmJhCIiYkJ5MRERcUSxCRioqISsR+AqCB5iPcOrwlZNthKoS3cfV+uJKH3nq/nnHvbqr8AkVEsAaALoAugC6ALoAugC0sA6ALoAugC6ALoAujCEgC6ALoAugC6ALoAurAEgC6ALoAugC6ALoAuLAGgC6ALoAugCyTIYrH4LbpMJpNer2dZ1umpKpWUYRj1er3f7/u+n+q090EmAXt7ezs7O8vKmPTmPB6PazVVqaheT7mu8n01nSrPU46jrq6W6og3shYp6vJnt6EyySvVavXu7k7n7CKT7Ha75bIajdT/f1wzFgs1HKpiUaWzFkeqi+u6oovOxUjm1mxK5lCz2UZXVuP9XZmm6nQ66LKW6+vrm5sbnVtdiX2jsUweP7oSjiBYGpN0jjlSXdrt9u3trba6OI4jzYoYENGVVY4pFouJ9jFHqott2/f393rqImXIMJad7FauhEP6GOl80eUTklpSqNTZ6CJ9mWyFYrgSdr6lUim53fXxtrqmaeqpi2TOwSCmLjJkd93v99HlI0EQFAqF+XyuoS6y5fO8+LpIFUuuHh2pLsL5+fnz87OGuuTzWze5H4eoZhgGunxiMBjI/khDXf4vaPwh+6OE2VWXrMjn8xo+BMjlcvN5fF18X0k5I7t8pVarvby86KZLpVKRkMfWxXWVZVno8hXZAVxeXuqmS6vVeniIr0uvJ6O3p5qolS6+75fLZd10eXp6sqz4ukhymkwm+8giuukinJycTKdTrXSZz+dyE7y+xnFlNFJSodfGPvq7JuHP0tEl5fdgpEzL3aiVLsLj46NpbvF8MRyzmRLPxuPxprISJSoff5yOLtGvbXe63W4mzxoTn9jFxYVtb3f8X6/XZTm+N+CX69LpdDJ5kyHxiQVBIGVFjImSY4JANRqNZrO59lzhewM2lYa1NSJRXVIIW7vdHg6HGuoSGiM5xjTN7/sYx1m2t3LfbDqDWvUiW4UkzeySTmoJm8Ld9wEHqsuqj5FJSo8mu2vfV+EJnqQTz1ODwbKxNQzDcZyIgY8elZSLUQoJRspQVs+lU90Nym0h/Xyr1ZIsksvlZFkLhUK1WrVt23XdHw+2Y4Rk025l718CpKbLbDYrFoue5+mvyx4P3LI99sj22sSYzKZ5jLpk+JnPMV7b79XlYINxyNdGMeLa0AXQBdAF0AUAXQBdAF0AXQBdAF0A0AXQBdAF0AXQBdAFAF0AXQBdAF0AXQBdANAF0AXQBQ6ef8TncwW7FfIPAAAAAElFTkSuQmCC", strResult);
}

}
