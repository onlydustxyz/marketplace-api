package onlydust.com.marketplace.api.bootstrap;

import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.api.bootstrap.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.domain.port.output.GithubAuthenticationPort;
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
    public JWTVerifier jwtVerifier() {
        return new JwtVerifierStub();
    }

    @Bean
    @Primary
    public GithubAuthenticationPort githubAuthenticationPort() {
        return new Auth0ApiClientStub();
    }

}
