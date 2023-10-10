package onlydust.com.marketplace.api.od.old.api.client.adapter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

public class OdOldApiAdapterITConfiguration {

    @Bean
    @ConfigurationProperties("od-old-api-client-adapter.base-url")
    public OdOldApiProperties OdOldApiProperties() {
        return new OdOldApiProperties();
    }
}
