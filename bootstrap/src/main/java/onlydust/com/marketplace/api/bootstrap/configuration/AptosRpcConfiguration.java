package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.infrastructure.aptosrpc.RpcClient;
import onlydust.com.marketplace.api.infrastructure.aptosrpc.adapters.AptosAccountValidatorAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AptosRpcConfiguration {
    @Bean
    @ConfigurationProperties("infrastructure.aptos")
    public RpcClient.Properties aptosProperties() {
        return new RpcClient.Properties();
    }

    @Bean
    public RpcClient aptosClient(final RpcClient.Properties aptosProperties) {
        return new RpcClient(aptosProperties);
    }

    @Bean
    public AptosAccountValidatorAdapter aptosAccountValidatorAdapter(final RpcClient aptosClient) {
        return new AptosAccountValidatorAdapter(aptosClient);
    }
}
