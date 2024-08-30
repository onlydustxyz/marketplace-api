package onlydust.com.marketplace.api.configuration;

import onlydust.com.marketplace.api.infura.Web3Client;
import onlydust.com.marketplace.api.infura.adapters.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonRpcConfiguration {
    @Bean
    @ConfigurationProperties(value = "infrastructure.ethereum", ignoreUnknownFields = false)
    public Web3Client.Properties ethereumProperties() {
        return new Web3Client.Properties();
    }

    @Bean
    public EthWeb3ERC20ProviderAdapter ethereumERC20Provider(final Web3Client.Properties ethereumProperties) {
        return new EthWeb3ERC20ProviderAdapter(ethereumProperties);
    }

    @Bean
    public EthWeb3EnsValidatorAdapter ethereumEnsValidatorAdapter(final Web3Client.Properties ethereumProperties) {
        return new EthWeb3EnsValidatorAdapter(ethereumProperties);
    }

    @Bean
    public Web3EvmAccountAddressValidatorAdapter infuraEvmAccountAddressValidatorAdapter(final Web3Client.Properties ethereumProperties) {
        return new Web3EvmAccountAddressValidatorAdapter(ethereumProperties);
    }

    @Bean
    public Web3EvmTransactionStorageAdapter ethereumTransactionStorageAdapter(final Web3Client.Properties ethereumProperties,
                                                                              final EthWeb3ERC20ProviderAdapter ethereumERC20Provider) {
        return new Web3EvmTransactionStorageAdapter(ethereumProperties, ethereumERC20Provider);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.optimism", ignoreUnknownFields = false)
    public Web3Client.Properties optimismProperties() {
        return new Web3Client.Properties();
    }

    @Bean
    public EthWeb3ERC20ProviderAdapter optimismERC20Provider(final Web3Client.Properties optimismProperties) {
        return new EthWeb3ERC20ProviderAdapter(optimismProperties);
    }

    @Bean
    public Web3EvmTransactionStorageAdapter optimismTransactionStorageAdapter(final Web3Client.Properties optimismProperties,
                                                                              final EthWeb3ERC20ProviderAdapter optimismERC20Provider) {
        return new Web3EvmTransactionStorageAdapter(optimismProperties, optimismERC20Provider);
    }

    @Bean
    @ConfigurationProperties(value = "infrastructure.starknet", ignoreUnknownFields = false)
    public Web3Client.Properties starknetProperties() {
        return new Web3Client.Properties();
    }

    @Bean
    public StarknetERC20ProviderAdapter starknetERC20Provider(final Web3Client.Properties starknetProperties) {
        return new StarknetERC20ProviderAdapter(starknetProperties);
    }

    @Bean
    public StarknetAccountValidatorAdapter starknetAccountValidatorAdapter() {
        return new StarknetAccountValidatorAdapter();
    }

    @Bean
    public StarknetTransactionStorageAdapter starknetTransactionStorageAdapter(final Web3Client.Properties starknetProperties) {
        return new StarknetTransactionStorageAdapter(starknetProperties);
    }
}
