package onlydust.com.marketplace.api.configuration;

import com.onlydust.customer.io.adapter.CustomerIOAdapter;
import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerIOConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.customer-io", ignoreUnknownFields = false)
    public CustomerIOProperties customerIOProperties() {
        return new CustomerIOProperties();
    }

    @Bean
    public CustomerIOHttpClient customerIOHttpClient(final CustomerIOProperties customerIOProperties) {
        return new CustomerIOHttpClient(customerIOProperties);
    }

    @Bean
    public CustomerIOAdapter notificationInstantEmailSender(final CustomerIOProperties customerIOProperties,
                                                            final CustomerIOHttpClient customerIOHttpClient) {
        return new CustomerIOAdapter(customerIOHttpClient, customerIOProperties);
    }
}
