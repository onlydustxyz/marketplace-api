package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.api.domain.port.output.RewardStoragePort;
import onlydust.com.marketplace.api.od.rust.api.client.adapter.OdRustApiClientAdapter;
import onlydust.com.marketplace.api.rest.api.adapter.authentication.hasura.HasuraJwtPayload;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OdRustApiClientConfiguration {

    @Bean
    public RewardStoragePort<HasuraJwtPayload> rewardStoragePort() {
        return new OdRustApiClientAdapter();
    }
}
