package onlydust.com.marketplace.api.bootstrap.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfiguration {

  @Bean
  public Docket api() {
    // http://localhost:9999/swagger-ui/#/
    return new Docket(DocumentationType.OAS_30)
        .apiInfo(
            new ApiInfoBuilder()
                .title("Marketplace backend REST API")
                .version("1.0.0-SNAPSHOT")
                .contact(
                    new Contact(
                        "Pierre Oucif",
                        "http://www.onlydust.com",
                        "pierre@onlydust.xyz"
                    )
                ).build()
        )
        .securityContexts(Collections.singletonList(securityContext()))
        .securitySchemes(List.of(apiKey()))
        .select()
        .paths(PathSelectors.regex("(/api/.*|/bo/.*)"))
        .build()
        .pathMapping("/");
  }


  private ApiKey apiKey() {
    return new ApiKey("Authorization", "Authorization", "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder().securityReferences(defaultAuth()).build();
  }

  private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Arrays.asList(new SecurityReference("Authorization", authorizationScopes));
  }
}
