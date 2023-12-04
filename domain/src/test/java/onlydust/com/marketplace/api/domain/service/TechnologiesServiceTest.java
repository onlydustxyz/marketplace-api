package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TechnologiesServiceTest {
    private final TrackingIssuePort trackingIssuePort = mock(TrackingIssuePort.class);
    private final TechnologiesService technologiesService = new TechnologiesService(trackingIssuePort);
    private final Faker faker = new Faker();

    @Test
    public void should_create_an_issue_upon_technology_suggestion() {
        final var githubUsername = faker.name().username();
        final var requester = User.builder().login(githubUsername).build();

        technologiesService.suggest("Rust", requester);
        verify(trackingIssuePort).createIssueForTechTeam("New technology suggestion: Rust", "Suggested by: " + githubUsername);
    }
}