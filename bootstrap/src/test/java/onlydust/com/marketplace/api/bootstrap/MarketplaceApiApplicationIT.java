package onlydust.com.marketplace.api.bootstrap;

import onlydust.com.marketplace.api.bootstrap.helper.ITAuthenticationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootApplication
public class MarketplaceApiApplicationIT {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }


    @Bean
    @Primary
    public ITAuthenticationContext authenticationContext() {
        return new ITAuthenticationContext();
    }

}
