package org.glytoucan.api;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.schema.AlternateTypeRules.newRule;

import java.lang.reflect.WildcardType;

import org.glycoinfo.convert.GlyConvertConfig;
import org.glycoinfo.rdf.SelectSparql;
import org.glycoinfo.rdf.dao.virt.VirtSesameTransactionConfig;
import org.glycoinfo.rdf.glycan.GlycoSequenceSelectSparql;
import org.glycoinfo.rdf.service.impl.GlycanProcedureConfig;
import org.glytoucan.api.controller.GlycanControllerConfig;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class)
@Import(value = { VirtSesameTransactionConfig.class, GlycanProcedureConfig.class, GlycanControllerConfig.class, GlyConvertConfig.class })
@EnableSwagger2
public class Application {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class, args);
  }

  // @Bean
  // public Docket fullApi() {
  // return new
  // Docket(DocumentationType.SWAGGER_2).groupName("full-api").apiInfo(apiInfo()).select()
  // .paths(allPaths()).build();
  // }

  @Bean
  public Docket fullApi() {
    return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select().apis(RequestHandlerSelectors.any())
        // .paths(PathSelectors.any()).build().pathMapping("/")
        .paths(allPaths()).build().pathMapping("/").directModelSubstitute(LocalDate.class, String.class)
        .genericModelSubstitutes(ResponseEntity.class)
        .alternateTypeRules(newRule(
            typeResolver.resolve(DeferredResult.class, typeResolver.resolve(ResponseEntity.class, WildcardType.class)),
            typeResolver.resolve(WildcardType.class)))
        .useDefaultResponseMessages(false)
        // .globalResponseMessage(RequestMethod.GET,
        // newArrayList(new
        // ResponseMessageBuilder().code(500).message("500 message")
        // .responseModel(new ModelRef("Error")).build()))
        // .securitySchemes(newArrayList(apiKey())).securityContexts(newArrayList(securityContext()))
        .enableUrlTemplating(true);
  }

  private Predicate<String> allPaths() {
    return or(regex("/glycans.*"), or(regex("/glycan/register"), regex("/status")));
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("Glytoucan REST API")
        .description("This API can be used as a REST api server or in combination with " + "the glytoucan client.  "
            + "Please refer to <a href=\"http://glytoucan.org\">glytoucan.org</a> "
            + "for links to the latest status and functionality of the Glycan Repository.")
        .termsOfServiceUrl("http://code.glytoucan.org").contact("glytoucan").license("Creative Commons")
        .licenseUrl("http://creativecommons.org/licenses/by/4.0/").version("4.0").build();
  }

  @Autowired
  private TypeResolver typeResolver;

  @Bean(name = "glycoSequenceSelectSparql")
  SelectSparql getSelectSparql() {
    SelectSparql select = new GlycoSequenceSelectSparql();
    select.setFrom("FROM <http://rdf.glytoucan.org/sequence/wurcs>\nFROM <http://rdf.glytoucan.org/core>");
    return select;
  }

}
