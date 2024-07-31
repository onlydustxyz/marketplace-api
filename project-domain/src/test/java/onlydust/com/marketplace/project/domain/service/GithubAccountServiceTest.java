package onlydust.com.marketplace.project.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.GithubAccount;
import onlydust.com.marketplace.project.domain.model.GithubAppInstallationStatus;
import onlydust.com.marketplace.project.domain.model.GithubMembership;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.project.domain.port.output.GithubStoragePort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubAccountServiceTest {

    final GithubStoragePort githubStoragePort = mock(GithubStoragePort.class);
    final RetriedGithubInstallationFacade.Config config =
            RetriedGithubInstallationFacade.Config.builder().retryCount(3).retryInterval(0).build();
    final RetriedGithubInstallationFacade githubInstallationService =
            new RetriedGithubInstallationFacade(new GithubAccountService(githubStoragePort,
                    mock(GithubSearchPort.class)), config);
    private final Faker faker = new Faker();
    final Long installationId = (long) faker.number().numberBetween(1000, 2000);

    @Test
    void should_get_an_account_by_installation_id() {

        // When
        final var expectedAccount = new GithubAccount(
                4534322L,
                installationId,
                "onlydustxyz",
                "OnlyDust",
                "Organization",
                "htmlUrl",
                "avatarUrl",
                List.of(GithubRepo.builder()
                        .id(123446L)
                        .owner("onlydustxyz")
                        .name("marketplace")
                        .htmlUrl("htmlUrl")
                        .updatedAt(new Date())
                        .description("description")
                        .starsCount(1L)
                        .forksCount(12L)
                        .build()
                ),
                List.of(123446L)
                , GithubAppInstallationStatus.COMPLETE, false, false
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
                installationId,
                "onlydustxyz",
                "OnlyDust",
                "Organization",
                "htmlUrl",
                "avatarUrl",
                List.of(GithubRepo.builder()
                        .id(123446L)
                        .owner("onlydustxyz")
                        .name("marketplace")
                        .htmlUrl("htmlUrl")
                        .updatedAt(new Date())
                        .description("description")
                        .starsCount(1L)
                        .forksCount(12L)
                        .build()
                ),
                List.of(123446L)
                , GithubAppInstallationStatus.COMPLETE, false, false
        );

        Mockito.when(githubStoragePort.findAccountByInstallationId(installationId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(expectedAccount));

        final var githubAccount = githubInstallationService.getAccountByInstallationId(installationId);

        // Then
        assertEquals(githubAccount, Optional.of(expectedAccount));
    }

    final GithubSearchPort githubSearchPort = mock(GithubSearchPort.class);


    @Test
    void should_return_user_organizations_given_no_installed_organizations() {
        // Given
        final GithubAccountService githubAccountService = new GithubAccountService(githubStoragePort, githubSearchPort);
        final var githubUserId = 5L;
        final var authenticatedUser = AuthenticatedUser.builder().githubUserId(githubUserId).login(faker.pokemon().name()).build();

        // When
        final List<GithubAccount> githubAccounts = List.of(
                GithubAccount.builder()
                        .id(1L)
                        .login("org1")
                        .build(),
                GithubAccount.builder()
                        .id(2L)
                        .login("org2")
                        .build()
        );
        when(githubSearchPort.searchOrganizationsByGithubUserId(githubUserId))
                .thenReturn(githubAccounts);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubUserId, authenticatedUser.login(),
                "org1"))
                .thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubUserId, authenticatedUser.login(),
                "org2"))
                .thenReturn(GithubMembership.MEMBER);
        when(githubStoragePort.findInstalledAccountsByIds(List.of(1L, 2L))).thenReturn(List.of());
        final List<GithubAccount> organizationsForGithubPersonalToken =
                githubAccountService.getOrganizationsForAuthenticatedUser(authenticatedUser);

        // Then
        assertEquals(List.of(githubAccounts.get(0).toBuilder().isCurrentUserAdmin(true).build(),
                        githubAccounts.get(1).toBuilder().isCurrentUserAdmin(false).build(),
                        GithubAccount.builder().id(5L).login(authenticatedUser.login()).isPersonal(true).isCurrentUserAdmin(true).build()),
                organizationsForGithubPersonalToken);
    }

    @Test
    void should_return_user_organizations_given_installed_organizations() {
        // Given
        final GithubAccountService githubAccountService = new GithubAccountService(githubStoragePort, githubSearchPort);
        final var githubUserId = 5L;
        final var user = AuthenticatedUser.builder().login(faker.pokemon().name()).githubUserId(githubUserId).build();
        // When
        final List<GithubAccount> githubAccounts = List.of(
                GithubAccount.builder()
                        .id(1L)
                        .login("org1")
                        .build(),
                GithubAccount.builder()
                        .id(2L)
                        .login("org2")
                        .build()
        );
        when(githubSearchPort.searchOrganizationsByGithubUserId(githubUserId))
                .thenReturn(githubAccounts);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubUserId, user.login(), "org1"))
                .thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubUserId, user.login(), "org2"))
                .thenReturn(GithubMembership.EXTERNAL);
        when(githubStoragePort.findInstalledAccountsByIds(List.of(1L, 2L, 5L))).thenReturn(List.of(
                GithubAccount.builder()
                        .id(2L)
                        .login(githubAccounts.get(1).getLogin())
                        .installationStatus(GithubAppInstallationStatus.COMPLETE)
                        .installationId(1L)
                        .build(),
                GithubAccount.builder()
                        .id(user.githubUserId())
                        .login(user.login())
                        .installationStatus(GithubAppInstallationStatus.COMPLETE)
                        .installationId(3L)
                        .build()
        ));
        final List<GithubAccount> organizationsForGithubPersonalToken =
                githubAccountService.getOrganizationsForAuthenticatedUser(user);

        // Then
        assertEquals(List.of(
                        githubAccounts.get(0).toBuilder().isCurrentUserAdmin(true).build(),
                        githubAccounts.get(1)
                                .toBuilder()
                                .installationStatus(GithubAppInstallationStatus.COMPLETE)
                                .installationId(1L)
                                .isCurrentUserAdmin(false)
                                .isPersonal(false)
                                .build(),
                        GithubAccount.builder()
                                .installationStatus(GithubAppInstallationStatus.COMPLETE)
                                .id(user.githubUserId())
                                .login(user.login())
                                .isPersonal(true)
                                .isCurrentUserAdmin(true)
                                .installationId(3L)
                                .build())
                , organizationsForGithubPersonalToken);
    }
}
