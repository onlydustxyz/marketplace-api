package onlydust.com.marketplace.api.bootstrap.configuration;

import com.onlydust.customer.io.adapter.CustomerIOAdapter;
import onlydust.com.marketplace.kernel.port.output.MailPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomerIOConfiguration {

    @Bean
    public MailPort mailNotificationPort() {
        return new CustomerIOAdapter();
    }
}
