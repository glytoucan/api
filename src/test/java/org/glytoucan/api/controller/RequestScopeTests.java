package org.glytoucan.api.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glytoucan.api.Application;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes=Application.class)
@WebAppConfiguration
@Configuration
@SpringApplicationConfiguration(Application.class)
public class RequestScopeTests {
  
  private static final Log logger = LogFactory.getLog(RequestScopeTests.class);
  
  @Autowired GlycanController glycanController;
//  @Autowired MockHttpServletRequest request;
  
  @Bean
  GlycanController glycanController() {
    return new GlycanController();
  }
  
  @Test
  public void requestScope() throws IOException {
    
    String accessionNumber = "G00029MO";
    logger.debug("accessionNumber:>" + accessionNumber);
    ResponseEntity<byte[]> response = glycanController.getGlycanImage(accessionNumber, null, "cfg", "extended");
    byte[] result = response.getBody();
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(result));

    String strResult = encodeToString(img, "png");
    logger.debug("strResult:>" + strResult);
    logger.debug("<html><img src=\"data:image/png;base64," + strResult + "\"></html>");
    Assert.assertEquals("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", strResult);
    
    // assert results
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
}