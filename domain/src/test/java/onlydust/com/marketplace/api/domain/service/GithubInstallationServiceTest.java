package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class GithubInstallationServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_get_an_account_by_installation_id() {
        // Given
        final Long installationId = (long) faker.number().numberBetween(1000, 2000);
        final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
        final GithubInstallationService githubInstallationService = new GithubInstallationService(githubStoragePort);

        // When
        final var expectedAccount = new GithubAccount(
                4534322L,
                "onlydustxyz",
                "Organization",
                "htmlUrl",
                "avatarUrl",
                installationId,
                List.of(new GithubRepo(
                        123446L,
                        "marketplace",
                        "htmlUrl",
                        new Date(),
                        "description",
                        1L,
                        12L
                ))
        );

        Mockito.when(githubStoragePort.findAccountByInstallationId(installationId))
                .thenReturn(Optional.of(expectedAccount));

        final var githubAccount = githubInstallationService.getAccountByInstallationId(installationId);

        // Then
        assertEquals(githubAccount, Optional.of(expectedAccount));
    }
}
