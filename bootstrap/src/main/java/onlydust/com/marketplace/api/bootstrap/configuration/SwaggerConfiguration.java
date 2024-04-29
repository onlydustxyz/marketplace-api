package onlydust.com.marketplace.api.bootstrap.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Marketplace backend REST API")
                        .version("1.0.0-SNAPSHOT")
                        .contact(new Contact().name("OnlyDust tech team")
                                .email("tech@onlydust.xyz")
                                .url("http://www.onlydust.com"))
                )
                .externalDocs(new ExternalDocumentation()
                        .description("Github repo")
                        .url("https://github.com/onlydustxyz/marketplace-api"))
                .components(new Components()
                        .addSecuritySchemes("apiKey",
                                new SecurityScheme().type(SecurityScheme.Type.APIKEY).in(SecurityScheme.In.HEADER).name("Api-Key"))
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).in(SecurityScheme.In.HEADER).scheme("bearer")));
    }
}
