package onlydust.com.marketplace.api.bootstrap.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MarketplaceApiWebMvcConfigurer implements WebMvcConfigurer {

    private final WebSecurityPropertiesConfiguration.WebCorsProperties webCorsProperties;

    @Autowired
    public MarketplaceApiWebMvcConfigurer(WebSecurityPropertiesConfiguration.WebCorsProperties webCorsProperties) {
        this.webCorsProperties = webCorsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "PUT", "POST", "DELETE", "PATCH")
                .allowedOrigins(this.webCorsProperties.getHosts());
    }


}

