package onlydust.com.marketplace.api.bootstrap;

import com.auth0.jwt.interfaces.JWTVerifier;
import com.slack.api.RequestConfigurator;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.slack.SlackApiAdapter;
import onlydust.com.marketplace.api.slack.SlackProperties;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.project.domain.port.output.HackathonStoragePort;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootApplication
public class MarketplaceApiApplicationIT {

    public static void main(String[] args) {
        SpringApplication.run(MarketplaceApiApplication.class, args);
    }

    private final ImageStoragePort imageStoragePort = mock(ImageStoragePort.class);
    private final PdfStoragePort pdfStoragePort = mock(PdfStoragePort.class);

    @Bean
    @Primary
    public JWTVerifier jwtVerifier() {
        return new JwtVerifierStub();
    }

    @Bean
    @Primary
    public GithubAuthenticationPort githubAuthenticationPort() {
        return new Auth0ApiClientStub();
    }

    @Bean
    @Primary
    public ImageStoragePort imageStoragePort() {
        return imageStoragePort;
    }

    @Bean
    @Primary
    public PdfStoragePort pdfStoragePort() {
        return pdfStoragePort;
    }

    @Bean
    public SlackApiAdapter slackApiAdapter(final SlackProperties slackProperties,
                                           final UserStoragePort userStoragePort,
                                           final ProjectStoragePort projectStoragePort,
                                           final HackathonStoragePort hackathonStoragePort) throws SlackApiException, IOException {
        final var slackClient = mock(MethodsClient.class);
        final var response = new ChatPostMessageResponse();
        response.setOk(true);
        when(slackClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(response);
        when(slackClient.chatPostMessage(any(RequestConfigurator.class))).thenReturn(response);
        return spy(new SlackApiAdapter(slackProperties, slackClient, userStoragePort, projectStoragePort, hackathonStoragePort));
    }
}
