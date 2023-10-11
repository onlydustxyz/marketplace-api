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

    final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    final RetriedGithubInstallationFacade.Config config = RetriedGithubInstallationFacade.Config.builder().retryCount(3).retryInterval(0).build();
    final RetriedGithubInstallationFacade githubInstallationService = new RetriedGithubInstallationFacade(new GithubInstallationService(githubStoragePort), config);
    private final Faker faker = new Faker();
    final Long installationId = (long) faker.number().numberBetween(1000, 2000);

    @Test
    void should_get_an_account_by_installation_id() {

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

    @Test
    void should_retry_if_not_found() {

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
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(expectedAccount));

        final var githubAccount = githubInstallationService.getAccountByInstallationId(installationId);

        // Then
        assertEquals(githubAccount, Optional.of(expectedAccount));
    }
}
