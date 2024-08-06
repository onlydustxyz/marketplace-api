package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.stellar.StellarAccountIdValidator;
import onlydust.com.marketplace.api.stellar.StellarERC20ProviderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StellarConfiguration {
    @Bean
    public StellarERC20ProviderAdapter stellarERC20Provider() {
        return new StellarERC20ProviderAdapter();
    }

    @Bean
    public StellarAccountIdValidator stellarAccountIdValidator() {
        return new StellarAccountIdValidator();
    }
}
