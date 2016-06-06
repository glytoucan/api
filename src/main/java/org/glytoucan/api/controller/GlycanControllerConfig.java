package org.glytoucan.api.controller;

import org.eurocarbdb.application.glycanbuilder.BuilderWorkspace;
import org.eurocarbdb.application.glycanbuilder.renderutil.GlycanRendererAWT;
import org.eurocarbdb.resourcesdb.Config;
import org.eurocarbdb.resourcesdb.io.MonosaccharideConverter;
import org.glycoinfo.vision.generator.ImageGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlycanControllerConfig {
  
  @Bean
  BuilderWorkspace glycanWorkspace() {
    return new BuilderWorkspace(glycanRenderer());
  }
  
  @Bean
  ImageGenerator imageGenerator() {
    return new ImageGenerator();
  }

  @Bean
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
