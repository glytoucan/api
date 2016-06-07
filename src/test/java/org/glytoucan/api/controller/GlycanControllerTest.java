package org.glytoucan.api.controller;


import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import static org.hamcrest.Matchers.containsString;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

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
import org.hsqldb.lib.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(Application.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class GlycanControllerTest {
  
  private static final Log logger = LogFactory.getLog(GlycanControllerTest.class);
  
  @Autowired
  GlycanController gc;



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
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAALoAAABSCAIAAABse1lJAAAD0ElEQVR42u2bIW/qUBSAb6bIgpisQCAQiIqJCuQEAoFATCLIgphATCAmKiYQExMIxMSWbAlyYqKiS0gQExOIiYpKxPsBiAqSh3jvMBKybLBBoe24+75cSULvPV/POfe2Vf8AVkaxBIAugC6ALoAugC6ALiwBoAugC6ALoAugC6ALSwDoAugC6ALoAugC6MISALoAugC6ALoAugC6sASALoAugC6ALhAhk8nkt+gyGAxs2y4Wi+oN0zRLpVKr1fJ9P9Zpb4NEAvb6+np4eJiUMfHNud/vFwoql1O2rVxX+b4aDpXnKcdRZ2cqk1HijaxFjLr83WyoRPJKPp+/urrSObvIJJvNZjarul319o8LxmSiOh1lGCqetdhRXVzXFV10LkYyt0pFMocajZa6Mh9//ijLUo1GA10Wcn5+fnFxoXOrK7Evl6fJ41tXZiMIpsZEnWN2VJdarXZ5eamtLo7jSLMiBqzoyjzHGIYRaR+zo7rU6/Xr62s9dZEyZJrTTnYtV2ZD+hjpfNHlA5JaYqjUyegifZlshUK4Mut8M5lMdLvr3W11LcvSUxfJnO12SF1knJyoVquFLu8JgiCdTo/HYw11kS2f54XX5fY2wnq0o7oIR0dHj4+PGuqyt7d2k/t+PD1Nz3zR5QPtdlv2Rxrq8rag4cfLi4qYTXVJiv39fQ0fAqRSqfE4vC69npJyRnb5TKFQ6PV6uumSy+V8P7wu9/eqWCyiy2dkB3B6eqqbLtVq9eYmvC6NhrJte0s1UStdfN/PZrO66fLw8FAshtdFktNgMNhGFtFNF+Hg4GA4HGqly3g8lpvg+TmMK92ukgq9MParv2sy+1k8usT8HoyUabkbtdJFuLu7s6w1ni/OxmikxLN+v7+srKwSlfc/jkeX1a9tc5rNZiLPGiOf2PHxcb2+3vF/qVSS5fjagF+uS6PRSORNhsgnFgSBlBUxZpUcEwSqXC5XKpWF5wpfG7CsNCysEZHqEkPYarVap9PRUJeZMZJjLMv6uo9xnGl7K/fNsjOoeS+yVkjizC7xpJZZU7j5PuCH6jLvY2SS0qPJ7tr31ewET9KJ56l2e9rYmqbpOM6KgV89KjEXoxgSjJShpJ5Lx7oblNtC+vlqtSpZJJVKybKm0+l8Pl+v113X/fZgO0RIlu1Wtv4lQGy6jEYjwzA8z9Nfly0euCV77JHstYkxiU1zF3VJ8DOfXby236vLjw3GT742ihHXhi6ALoAugC4A6ALoAugC6ALoAugCgC6ALoAugC6ALoAuAOgC6ALoAugC6ALoAoAugC6ALvDj+Q9rU3E93BTPUwAAAABJRU5ErkJggg==", strResult);
  }
  
  @Test
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


@Test
public void testWurcsSubstructure() throws Exception {
  String url = "/glycans/sparql/substructure?sequence=WURCS%3D2.0%2F2%2C2%2C1%2F%5Ba1122h-1x_1-5%5D%5Ba2112h-1x_1-5_2*NCC%2F3%3DO%5D%2F1-2%2Fa%3F-b1&format=wurcs";
      given().redirects().follow(false).when().get(url).then().statusCode(HttpStatus.SC_OK).and().body(containsString("http://rdf.glycoinfo.org/glycan/wurcs/2.0/monosaccharide/a1122h-1x_1-5"));
}
}
