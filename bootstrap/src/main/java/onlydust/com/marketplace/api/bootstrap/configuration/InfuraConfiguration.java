package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.infura.InfuraClient;
import onlydust.com.marketplace.api.infura.adapters.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfuraConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.ethereum", ignoreUnknownFields = false)
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
    public InfuraEvmTransactionStorageAdapter ethereumTransactionStorageAdapter(final InfuraClient.Properties ethereumProperties) {
        return new InfuraEvmTransactionStorageAdapter(ethereumProperties);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.optimism", ignoreUnknownFields = false)
    public InfuraClient.Properties optimismProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public EthInfuraERC20ProviderAdapter optimismERC20Provider(final InfuraClient.Properties optimismProperties) {
        return new EthInfuraERC20ProviderAdapter(optimismProperties);
    }

    @Bean
    public InfuraEvmTransactionStorageAdapter optimismTransactionStorageAdapter(final InfuraClient.Properties optimismProperties) {
        return new InfuraEvmTransactionStorageAdapter(optimismProperties);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.starknet", ignoreUnknownFields = false)
    public InfuraClient.Properties starknetProperties() {
        return new InfuraClient.Properties();
    }

    @Bean
    public StarknetInfuraERC20ProviderAdapter starknetERC20Provider(final InfuraClient.Properties starknetProperties) {
        return new StarknetInfuraERC20ProviderAdapter(starknetProperties);
    }

    @Bean
    public StarknetAccountValidatorAdapter starknetInfuraAccountValidatorAdapter() {
        return new StarknetAccountValidatorAdapter();
    }

    @Bean
    public StarknetInfuraTransactionStorageAdapter starknetInfuraTransactionStorageAdapter(final InfuraClient.Properties starknetProperties) {
        return new StarknetInfuraTransactionStorageAdapter(starknetProperties);
    }
}
