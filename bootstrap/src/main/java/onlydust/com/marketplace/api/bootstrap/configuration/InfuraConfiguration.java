package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.api.infura.adapters.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfuraConfiguration {
    @Bean
    @ConfigurationProperties("infrastructure.ethereum")
    public InfuraClient.Properties ethereumProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public EthInfuraERC20ProviderAdapter ethereumERC20Provider(final InfuraClient.Properties ethereumProperties) {
        return new EthInfuraERC20ProviderAdapter(ethereumProperties);
    }

    @Bean
    public EthInfuraEnsValidatorAdapter ethereumEnsValidatorAdapter(final InfuraClient.Properties ethereumProperties) {
        return new EthInfuraEnsValidatorAdapter(ethereumProperties);
    }

    @Bean
    public InfuraEvmAccountAddressValidatorAdapter infuraEvmAccountAddressValidatorAdapter(final InfuraClient.Properties ethereumProperties) {
        return new InfuraEvmAccountAddressValidatorAdapter(ethereumProperties);
    }

    @Bean
    @ConfigurationProperties("infrastructure.optimism")
    public InfuraClient.Properties optimismProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public EthInfuraERC20ProviderAdapter optimismERC20Provider(final InfuraClient.Properties optimismProperties) {
        return new EthInfuraERC20ProviderAdapter(optimismProperties);
    }

    @Bean
    @ConfigurationProperties("infrastructure.starknet")
    public InfuraClient.Properties starknetProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public StarknetInfuraERC20ProviderAdapter starknetERC20Provider(final InfuraClient.Properties starknetProperties) {
        return new StarknetInfuraERC20ProviderAdapter(starknetProperties);
    }

    @Bean
    public StarknetInfuraAccountValidatorAdapter starknetInfuraAccountValidatorAdapter(final InfuraClient.Properties starknetProperties) {
        return new StarknetInfuraAccountValidatorAdapter(starknetProperties);
    }
}
