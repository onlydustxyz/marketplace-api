package onlydust.com.marketplace.api.configuration;

import com.onlydust.marketplace.api.cron.properties.NodeGuardiansBoostProperties;
import onlydust.com.marketplace.api.node.guardians.NodeGuardiansApiAdapter;
import onlydust.com.marketplace.api.node.guardians.NodeGuardiansApiProperties;
import onlydust.com.marketplace.api.node.guardians.NodeGuardiansHttpClient;
import onlydust.com.marketplace.project.domain.port.output.NodeGuardiansApiPort;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NodeGuardiansConfiguration {

    @Bean
    @ConfigurationProperties(value = "infrastructure.node-guardians", ignoreUnknownFields = false)
    public NodeGuardiansApiProperties nodeGuardiansApiProperties() {
        return new NodeGuardiansApiProperties();
    }

    @Bean
    public NodeGuardiansHttpClient nodeGuardiansHttpClient(final NodeGuardiansApiProperties nodeGuardiansApiProperties) {
        return new NodeGuardiansHttpClient(nodeGuardiansApiProperties);
    }

    @Bean
    public NodeGuardiansApiPort nodeGuardiansApiPort(final NodeGuardiansHttpClient nodeGuardiansHttpClient) {
        return new NodeGuardiansApiAdapter(nodeGuardiansHttpClient);
    }

    @Bean
    @ConfigurationProperties(value = "application.node-guardians", ignoreUnknownFields = false)
    public NodeGuardiansBoostProperties nodeGuardiansBoostProperties() {
        return new NodeGuardiansBoostProperties();
    }
}
