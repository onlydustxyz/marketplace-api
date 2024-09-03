package onlydust.com.marketplace.api.configuration;

import com.onlydust.customer.io.adapter.CustomerIOAdapter;
import com.onlydust.customer.io.adapter.client.CustomerIOHttpClient;
import com.onlydust.customer.io.adapter.client.CustomerIOTrackingApiHttpClient;
import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.accounting.domain.port.out.EmailStoragePort;
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
    public CustomerIOTrackingApiHttpClient customerIOTrackingApiHttpClient(final CustomerIOProperties customerIOProperties) {
        return new CustomerIOTrackingApiHttpClient(customerIOProperties);
    }

    @Bean
    public CustomerIOAdapter notificationInstantEmailSender(final CustomerIOProperties customerIOProperties,
                                                            final CustomerIOHttpClient customerIOHttpClient,
                                                            final CustomerIOTrackingApiHttpClient customerIOTrackingApiHttpClient) {
        return new CustomerIOAdapter(customerIOHttpClient, customerIOTrackingApiHttpClient, customerIOProperties);
    }

    @Bean
    public EmailStoragePort emailStoragePort(final CustomerIOProperties customerIOProperties,
                                             final CustomerIOHttpClient customerIOHttpClient,
                                             final CustomerIOTrackingApiHttpClient customerIOTrackingApiHttpClient) {
        return new CustomerIOAdapter(customerIOHttpClient, customerIOTrackingApiHttpClient, customerIOProperties);
    }
}
