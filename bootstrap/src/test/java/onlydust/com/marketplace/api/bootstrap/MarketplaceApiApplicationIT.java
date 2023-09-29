package onlydust.com.marketplace.api.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import static reactor.netty.http.client.HttpClient.newConnection;

@SpringBootApplication
public class MarketplaceApiApplicationIT {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }

//    @Bean
//    public WebClient webClientIT(final WebClient.Builder webClientBuilder) {
//        return webClientBuilder
//                .clientConnector(new ReactorClientHttpConnector(newConnection().compress(true)))
//                .build();
//    }
}
