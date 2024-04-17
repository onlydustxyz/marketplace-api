package onlydust.com.marketplace.api.bootstrap.configuration;

import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerIOConfiguration {

    @Bean
    public MailNotificationPort mailNotificationPort() {
        return (email, rewardViews) -> {
            // TODO : implement customer-io adapter
        };
    }
}
