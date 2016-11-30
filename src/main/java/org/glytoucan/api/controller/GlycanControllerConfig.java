package org.glytoucan.api.controller;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycoinfo.vision.generator.ImageGenerator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

@Configuration
@ComponentScan("org.glytoucan.api.controller")
public class GlycanControllerConfig {
  
  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
//  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  BuilderWorkspace glycanWorkspace() {
    return new BuilderWorkspace(glycanRenderer());
  }
  
  @Bean
//  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
//  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  ImageGenerator imageGenerator() {
    return new ImageGenerator();
  }

  @Bean
  @Scope(value = WebApplicationContext.SCOPE_REQUEST)
//  @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
//  @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
  GlycanRendererAWT glycanRenderer() {
    return new GlycanRendererAWT();
  }
  
  @Bean
  MonosaccharideConverter monosaccharideConverter() {
    Config config = new Config();
    MonosaccharideConverter mc = new MonosaccharideConverter();
    mc.setConfig(config);
    return mc;
  }
}