package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.project.domain.port.input.RecommendationFacadePort;
import onlydust.com.marketplace.project.domain.port.output.RecommenderSystemPort;
import onlydust.com.marketplace.project.domain.service.RecommendationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecommendationConfiguration {

    @Bean
    public RecommendationFacadePort recommendationFacadePort(RecommenderSystemPort recommenderSystemPort) {
        return new RecommendationService(recommenderSystemPort);
    }
}
