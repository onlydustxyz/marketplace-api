package onlydust.com.marketplace.api.bootstrap;

import com.auth0.jwt.interfaces.JWTVerifier;
import onlydust.com.marketplace.accounting.domain.port.out.PdfStoragePort;
import onlydust.com.marketplace.api.bootstrap.helper.Auth0ApiClientStub;
import onlydust.com.marketplace.api.bootstrap.helper.JwtVerifierStub;
import onlydust.com.marketplace.api.bootstrap.helper.SlackNotificationStub;
import onlydust.com.marketplace.project.domain.port.output.GithubAuthenticationPort;
import onlydust.com.marketplace.project.domain.port.output.NotificationPort;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

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
    @Primary
    public NotificationPort notificationPort() {
        return new SlackNotificationStub();
    }
}
