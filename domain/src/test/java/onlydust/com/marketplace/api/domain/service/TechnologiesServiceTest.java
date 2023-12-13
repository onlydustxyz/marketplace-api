package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.input.TechnologyStoragePort;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TechnologiesServiceTest {
    private final TrackingIssuePort trackingIssuePort = mock(TrackingIssuePort.class);
    private final TechnologyStoragePort technologyStoragePort = mock(TechnologyStoragePort.class);
    private final TechnologiesService technologiesService = new TechnologiesService(trackingIssuePort,
            technologyStoragePort);
    private final Faker faker = new Faker();

    @Test
    public void should_create_an_issue_upon_technology_suggestion() {
        final var githubUsername = faker.name().username();
        final var requester = User.builder().login(githubUsername).build();

        technologiesService.suggest("Rust", requester);
        verify(trackingIssuePort).createIssueForTechTeam("New technology suggestion: Rust",
                "Suggested by: " + githubUsername);
    }

    @Test
    public void should_get_all_technologies() {
        when(technologyStoragePort.getAllUsedTechnologies()).thenReturn(List.of("Java", "Kotlin", "Rust"));

        final var technologies = technologiesService.getAllUsedTechnologies();
        assertThat(technologies).containsExactly("Java", "Kotlin", "Rust");
    }
}