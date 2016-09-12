package org.glytoucan.api.controller;


import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.glycoinfo.batch.search.wurcs.SubstructureSearchSparql;
import org.glycoinfo.convert.GlyConvert;
import org.glycoinfo.rdf.SelectSparql;
import org.glytoucan.api.Application;
import org.glytoucan.model.GlycanInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
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
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAASIAAABSCAIAAAB/iU6UAAAEnElEQVR42u2cL28iQRiHX1HZD4BsciRHciSHIDn8VTQpoqICUVGxElFRWXEJpgmyogJRUVFRUYFYgTiBQCAqKvoNKo6kJNfkSEBww9ISQpdlWNiZZXmerCpb9pdhnp0/O7MyBICIEYoAAM0A0AwA0AwAzQDQDADQDADNAADNANAMAM0AAM0A0AwAzQAAzQDQDADQDADNANAMANAMAM0A0AwA0AwAzQAAzQDQDADNAADNANAMwCjdbjeBmsk6MFYQg8Gg0WiUy+V8Pj+5eiaTcRxH/V19arF+kG11Hh8fU6mULdOi1cz7/vCHMc3u7u7SacnlpFKRZlN6vdHVBwNpt6ValUJB0um0OsfKLxTzbCJfRL6L/BL5LfJXpC/yT6Qlcinyw2K2GdRd4OrqKpmtWfw16/V6JycnmYy4blCSRkOyWSmVSup8Y79N/LOJfBWpe2rNO1yRb4azfaZer2ezWYtN61Zrpn77YlH296XTWRzm7U28k/fN1JiYZxM5FPkp8hLo2Ph4VScby+ZLpVI5Pz9P5hRI/DVT92NVj1Ud1cyjumqqNqt7s4EfJubZPMdeNRzrf3QjD81k8+Xs7Oz6+hrNLGimxgyqP6bTVsy0G6r7EfV4I+bZvL7ii7Zj722agWwBrdnFxQWamdZMddPT6QVjnoCxkBrZR9fRj3k2b86jvqRj7+O0SLMFcH9/r1pgNDOtWaPRyOXCBysUCq7rbmc2b16xH+6INFsAz8/Pqi1FM9OalcvlSiV8sGpVHMfZzmze3H0/7HEZXbZg9vb2Op0OmhnVLJ/PN5vhg7Xboyew25nNez4WWrNWdNmCKZVKtVoNzYxqpr55/Jw33DEaoWxrto9n0OGOfyZX9szM3BwcHKCZv2bRQbaw9Fc7bGKr37ilrdnOzk5sW4yYZ9vQ1kxxdHR0e3uLZozNGJtFiHLs+PgYzZhpjFE238Lf3JnGobcRZnd318qar8RqFrybxu6zqThnm07om20Tn5tNt8ZPT09oFqSZ/j60z/84c4K30iJtZaWF+WzL7t8bn+Z78jjbxq0CmaDaUisLvjZMs+B77VL9n/WuG5ypyvrxzGTTL7fpk+fNjG/cmsYJthY3bq9mw5Cr4Iu+K82nLxHbbGvRbBhqhf68bIa5ubk5PT1FM13NlhoCzTvB29NVVLW529Xc01Wct29qWc2sZNNxTOc9EeNsnml/dNqxgGyGUU2ZlY1nyRyb6YzmJzXG26Gc0dihnA3YBRyuNTOWLVy5BXw6ybZw93RwNsOoMA8PD2im1Zqtq2M2Pd5Qo/NcLjfnfRuFhe+0WHuHNopsqw8a52XzfReITjaT1Go1pVkCJ/TX+2Yr/eqycDbPdw5t8oYmb63DCFVLHMdxXVdnfmxmCmTeRc1nC6GZfuu3ermZodvtplKpVqtl5eqb9J7GpaqLlbfQhbgBxa3cEkwy39MYXXWJpzyUGyREMyoK5YZm5jo/QLmhGdWFcoPN1AwAzQAAzQDQDADNAADNANAMAM0AAM0A0AwA0AwAzQDQDADQDADNANAMANAMAM0AAM0A0AwAzQAAzQDQDADNACBK/gOrdaK6sc2McwAAAABJRU5ErkJggg==", strResult);
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
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAALoAAABSCAIAAABse1lJAAADQklEQVR42u2bLZPiQBBARyD5AZGICAQCgcgPQCAQCAQCgUAiEEgcciUCgUAgEAgEAoFEIBAIBD8CEbGCqkPs9ZE6iuL4yAUyIcN7NWo3VYSZR0/3pKN+AHyjmAJAF0AXQBdAF0AXQBemANAF0AXQBdAF0AXQhSkAdAF0AXQBdAF0AXRhCgBdAF0AXQBdAF0AXZgCQBdAF0AXQBcIEdd1P0KXw+Ewn88bjUYul1N/SafT9Xpd/i7/1fe1X0EkC7Zery3LisoYfd95NBrZtspmVaejFgu138tHq8NBrVbq60s5jrJtW67RqMuv50Y0usgvrdvtmhxd9vt9tVpNp9Vspo6feH3M5yqTUZVKRa5Hl6tMp9NMJqMzDOvWRda+WFT5vNrt7rnije9vdbw4H7YxMdWl0+m0Wi2TU12JK+KKePDQFW/I9iTGSIxBl39pNpu9Xs9YXSQXkT3IT1y5iDESckPNY+IbXdrttpm6yBYrue39fOVOHiOZb3ibdEx1GY/HEq3N1EVqY6mDArjiDcdxZrMZupyz3W4l7pqpS6PRkJo5sC5SXdfrdXS5IJVK7XY7A3XJ5XKLRXBdVqs/J3jocoEUAf1+30BdZEK9s7hgQ0qk8JYkvrpIBVAoFMzUJbAr3giZZ3WJkKj2oxB1SSQSRJcwKJVKw+GQ3IXcxRfiSrlcpjIKpTK6uq6x1sV13WQyqeHJ2sedu9zqNIi1Ll7k3mw25p3q2i881f3fXhPvMj26aO6DkbirrdlDky4/r35mdFoJP6tyfrEeXfzf2/NE9fDoPZ9IF68+kUaXE4PBoFarGajLsd+lKMa4rs9+l+Ktfpf7BtzaGq7uEaHqomHZJLRE0viis5su7aObLnOnmy5YfmBk7iITNZlMzNTllMdI9prNZm/06joPe3XPV8L/qmjejDQEmH6/L7qYVkhfrZVObwIkEgnvtyiWSJ4vNfPD7pYAS3LrF//yNwG06eK6rmVZy+XStGO6MI5itf2C3/nePuU9o1ctSYSv+cTx3j5Xl7ddjHe+NzYj7g1d0AVdAF0AXQDQBdAF0AXQBdAF0AUAXQBdAF0AXQBdAF0A0AXQBdAF0AXQBdAFAF0AXQBd4O35DWGSYVx8hOnGAAAAAElFTkSuQmCC", strResult);
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
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAAIYAAABSCAIAAAAetDv/AAACZklEQVR42u3arY4aURjG8UdwEeNLsiQlWQRJ8YtbLgCBQCARCCSiyZj1CAQCiUSMQCBWjuQuKlZ0khU7ySC2b6ctySaly0I6Zzj8nxwDTAI5P87HO2f0SkoW0QWQEEggIZBAQiCBhEBCIIGEQAIJgQQSAgkkBBICCSQEEkgIJJAQSAgkkBBIIPEwSZJcBclut9tsNsPhsNls6k9qtdpgMLD37dOSeGy32yAIXKkUR7JcLqVP0q30VXqUnqVMepFi6UH6Uq1W7ZoykNg/Zjqd+jxK0jTt9XrSjRTlDIfaWvrc7XbteoceURTV63WHQ1YFeEj30p307Z8ev9p3u7jdbjtUCcNwPB77vLzn4+Mu7+vsuGZT2b2NFVc9MhqNZrOZtyT5+nFz3Ph4M1Zs6ijJuuIViU3H+XoefdDj97piq72TCV1v4xWJ7Wvz/VV2Wmu1Wuv12onHoZcXT2L1R77fzU5tD1avFE/y7jsXTJLXg49nkMRWRbr1cKKi/zwJPJ9B8lJ0X1wHSXZeKy5XQVKpVBglrCUs7z7uuHzeBF9iXeJ5qWi1t1XgF1e972Nj1MlNHe5xHUwYhpPJxDeS15PuBHc6HYd3gvdZLBb9ft9DkjRNrYtzladjxodd7Pa8ZB8bIk4OToo7VbQd7bunijZfOT9V3Md+zGq18pNkv67Yit1oNP569m77q/KcvVvm87mROPlzOHtCJa/tf8YkbG9j+93yPKGSJEkQBHEcO/l2nuM6qOLqqyEpXSCBhEACCYEEEgIJJAQSAgkkBBJICCSQEEggIZAQSCAhkEBCIIGEQEIggYR8KD8AnmvYpH6789MAAAAASUVORK5CYII=", strResult);
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
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAAVYAAACGCAIAAAAJhDVPAAAHI0lEQVR42u3dv28TZxjA8RuQWlRVrFap1KEu8mC1pqJSBkZG/gAGBgZLXYLEwAgLWTJWyoAQQ0ZGBoZUyhAJqaIbI2P+hIyR6iE94xaMf5wvd+/dvb77fOUhcSx4bN/zfd977r33SS4AdJjERwBQAAAKAEABACgAAAUAoAAAFACAAgBQAAAKAEABACgAAAUAoAAAFACAAgBQAAAKAEABACgAAAUAoAAAFACAAgBQAAAKAEABACgAQKsVMJlMjo+Pd3d3b926lfzPYDAYj8fp8+lfG/wgYo4NaIMCXr16lXz/VXLjavL7d8mLG8nbUfL3r8lfN5PDQfLwejL8pt/vp69p5FOIOTZg6xVwfn5+//795Ievkz/609Ra9zj4Kfnx6r1799LX1/b+Y44NaIMC0pxJbl9Lfvs2+fPnrBybPU5G6Yvv3LlTT6bFHBvQEgVMx9g0x05Gm3Ns9kin37evpeNtDW8+5tiANihgeo6dzrHzjLFfjrfD4bDqc++YYwPaoIDJZDKtsWWfY68/9+73+9XV4WOODWiJAo6Pj6c19gI59vGxs7NzdHTUwdiAlihgd3d3eo2taJolD6+Px+MOxga0RAHTNTYvbhRPs8NBSgdjA1qigOnyurej4mn21830H+hgbECLFFA4x2aPSikdm6MHFJDFlStXoh1pY44NUAuo43xbLQC46OwVgek7d0UAaM26gPlz/OU/Lb9s+nOJ2NIpgHUBoIANTCaTfr9fwwq8hbRf9+uCC8rENvMIC4ACNhB2Hf7CUL+uILeQ6iu9UDK2/f39T5G8e/fOYQQKWEuBu/Hu3r278m68lbl9KQUsnCyUjO39+/dEAArYwPn5eZo200w7/iXPGJu+eN09+dkKWFkLmFfA8s9BYiMCUMBmC6Tj7WAw2LgzTzrHztiZJ88sYCHVNxYIQsU2L4LT01MHFihgRV2g3++PRqOV+/Pt7Oxs3J9v5ZCe/bKNCggVGxGAAjYzv0vvdH3eR9LsGo/HR0dHeer/C+XAlXXBdSXAbIOUj22lCD58+OAgAwVU/zaW1gVkFAvqWdurRgAKaFgKMYSRng4QASignYrJs0hhWQRqBFjm7OyMArZ4lpFzxlFYBPF0Ogpyl3YHY9t4ztjr9ZqyAAXUp4BlEeQpFkbV6ehj1P+UeyQdjC2b1OwHBwdmAV1RQH4RRNjpiAKC8+bNm+Fw2OCG1BRQVgFlJpAZIoiz0xEFBGdvb+/x48dNHsmSufzJZ0kRzF8+TH+ePRlnpyMKCM6jR4+eP39OAV20xgLpLODTX589exZnpyMKaOExKS2jKh/8J4JYOx1RQHWjQlOXJCiggfzPJuZORxRQRf6v+5UCWqiAPLKPec9FCqh6YKCAllcB8sg+5p2XKaCGiWHdYUjORr7v7Judo+7CRAEUgMoVEHMXptJp1tXYkgKbX1JAOy2QfSIQc6cjswCzAJQtB2wsB6oFKAdSQKeJ5IrAymMxkjSLObbCReILFwUxI4Z1AeuOxRjSLObYSs4NLQ3C3DLhcKsDL3uEZRSrgqdZzLHVTDp3q+2mbwqIO/mT5OnTp2G7MOWfZ2ZsvliRAqKNrWb29vaePHlCAV3k9PS01+st7C9YRRcmCohZAYeHhw8ePKCAziX/8m3CM6rowpQnx7L30qpUAbHFVjPpFKCRjQMoILrkn7dAqC5MBapNcdYCWjwLSL/E169fU0C3kj/n3oGhujBdXOayc80nAhHGVicvX75MFVDD1m8UEEvyF95BuECnowJptm5kDr5Lb8yx1cbZ2Vmv12uqzQQFNJz89eyuXWCkren4izi2mi3Q2FcgP2tL/mXN5793KEiaNTjQbWNsHcGHXhUFegpWp4BoEyzm2Cgg2Le7FedmATv2zCf/yclJsVlxRZPtCBUgCdutgO2o0Ibq2DOf/PkLfnXWAigAFPAFoTr2FEv+ZRE4IkEB9SkgSMeeAuf8hkRQQBQKKNmxp3zy13NFAKCANef/Re/G29/fDzjyq4qDAupWwGQyKdOxZ5ax1XXsACigWgWU3JlnMBiszP+cW8QCFNCwAiran89yV1DAdiigol16KQAUsB0KqKhjDwWAAgIroEJKd+zJUIAKPygg6llAdR17FsqBRAAKiHdpUJwdewAKiF4BgTr2ABQQRgEZa+wynmm2Yw9AAWF2m73sr58VEK5jD0ABdShgOY1X/r/ZOvj874Tr2ANQQOwKWO5aEbBjD0ABtSogT/5vVEDAjj0ABTRWC8iZ/ys3JgzVsQeggPpmAReZdf6MZ9Y9X75jD0ABKxVQU+eZ7Al/HjWU7NgDUED1sa6v8y2LI88CXgBbqYBL9aIH0B4FGMABswAAFACgawoAQAEAKAAABQCgAAAUAIACAFAAAAoAQAEAKAAABQCgAAAUAIACAFAAAAoAQAEABQCgAAAUAIACAFAAAAoA0Hb+BTfWFxsqIFDnAAAAAElFTkSuQmCC", strResult);
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
  mockMvc.perform(get("/glycans/G00055MO/image?style=extended&format=png&notation=cfg")).andExpect(status().isOk());
}
protected String json(Object o) throws IOException {
  MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
  this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
  return mockHttpOutputMessage.getBodyAsString();
}

}
