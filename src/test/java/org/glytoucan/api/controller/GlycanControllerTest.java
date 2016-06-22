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
  
  @Test
  public void testImageG15021LG() throws Exception {
    String accessionNumber = "G15021LG";

    logger.debug("accessionNumber:>" + accessionNumber);
    ResponseEntity<byte[]> response = gc.getGlycanImage(accessionNumber, "png", "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("iVBORw0KGgoAAAANSUhEUgAAAIYAAABSCAIAAAAetDv/AAAC10lEQVR42u3asYvaUBwH8G83B/+AQA8q1EFoBuGEujdQoVIcHB1CyVRCpwxSMjg4OHjDgctRKN7ucEMKdsrQMYODg8MN3W9QOOEEHa4/Ynu0BalGzUtz3y9vD3mf/N7vvae4ZxIWcApIwpCEJAxJSMKQhCQMSRiSkIQhCUkYkpCEIQlJGJIwJCEJQxKSMCQhCUMShiQkYUhCkhRmtVo9FpIgCFzXNQwDYXRdr1Qq7XZ7Mpkkx2M0GhWLRVUq8ZH4vg+8BJ4DHwEPGAPXwAi4Aj4AT8VG5iIJ9VEoFLrdbpqrRF7ScRzgGXAJLDeMO+Ac0BTOxTrD4VBI0rxwybsBb4HXwM1mj4fxHTi1bVshSbPZbLVaaW7vMr/Am7AIltuNqagorBXTNDudTmpJPM8Lm8d0a4+ftaJpWhL6StpIwiXrRdi9l7uPc+n2ambkz6SKRPpkuMVaRhp3Jycn8e+M/2JQonLE51mWBZxFJZFhynlFoYcqlSM+TLaS4bEjMsmnmNeuTVOfHhLgye6N/ffxRc72JDn4OrDcY3xDjHkUJJlMBrjdg+SrLH2skkMmn8+HF1mRST4bhsH2fsg0Gg3gYg+S967rchN8yAwGA+BVZBIpsiAIeFQ8ZBaLRS6XA/xIJJflclntxYYsm/JVpe2Oq9/vA6e73Dmux41Y+r6vlsRxHCX3j0cvzHq9Drzb6SpFTogyHcqv/2zbVnJLf3SS+XwuS1Cosk2tTKvVaq1WU/gL0kNM0+z1eikkWatIrZRKpX/1lStp6fJtJsFj3QjV7C9ie5L0FXnJ8I8QF+F55fbXD1Yj4EwqSdd1z/PukxFZsuQbUvJoxPzpyR5GzitSDeHZHtlsVo7olmUNh8MkFMc6s9lM07TxeJx+kv8ooqLq0SRJXEhCEoYkJGFIQhKGJCRhSMKQhCQMSUjCkIQkDElIwpCEIQlJGJKQhCEJSRiSMCQhCbNTfgBhmNNUvEd3pQAAAABJRU5ErkJggg==", strResult);
  }
  
  @Test
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
}
