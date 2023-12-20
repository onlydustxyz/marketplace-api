package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.model.GithubAccount;
import onlydust.com.marketplace.api.domain.model.GithubMembership;
import onlydust.com.marketplace.api.domain.model.GithubRepo;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.GithubStoragePort;
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
                , true, false, false
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
                , true, false, false
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
        final String githubPAT = faker.rickAndMorty().character() + faker.random().nextLong();
        final User authenticatedUser = User.builder().githubUserId(5L).githubLogin(faker.pokemon().name()).build();

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
        when(githubSearchPort.searchOrganizationsByGithubPersonalToken(githubPAT))
                .thenReturn(githubAccounts);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubPAT, authenticatedUser.getGithubLogin(),
                "org1"))
                .thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubPAT, authenticatedUser.getGithubLogin(),
                "org2"))
                .thenReturn(GithubMembership.MEMBER);
        when(githubStoragePort.findInstalledAccountsByIds(List.of(1L, 2L))).thenReturn(List.of());
        final List<GithubAccount> organizationsForGithubPersonalToken =
                githubAccountService.getOrganizationsForAuthenticatedUserAndGithubPersonalToken(githubPAT,
                        authenticatedUser);

        // Then
        assertEquals(List.of(githubAccounts.get(0).toBuilder().isCurrentUserAdmin(true).build(),
                        githubAccounts.get(1).toBuilder().isCurrentUserAdmin(false).build(),
                        GithubAccount.builder().id(5L).login(authenticatedUser.getGithubLogin()).isPersonal(true).isCurrentUserAdmin(true).build()),
                organizationsForGithubPersonalToken);
    }

    @Test
    void should_return_user_organizations_given_installed_organizations() {
        // Given
        final GithubAccountService githubAccountService = new GithubAccountService(githubStoragePort, githubSearchPort);
        final String githubPAT = faker.rickAndMorty().character() + faker.random().nextLong();
        final User user = User.builder().githubLogin(faker.pokemon().name()).githubUserId(5L).build();
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
        when(githubSearchPort.searchOrganizationsByGithubPersonalToken(githubPAT))
                .thenReturn(githubAccounts);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubPAT, user.getGithubLogin(), "org1"))
                .thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(githubPAT, user.getGithubLogin(), "org2"))
                .thenReturn(GithubMembership.EXTERNAL);
        when(githubStoragePort.findInstalledAccountsByIds(List.of(1L, 2L, 5L))).thenReturn(List.of(
                GithubAccount.builder()
                        .id(2L)
                        .login(githubAccounts.get(1).getLogin())
                        .installed(true)
                        .installationId(1L)
                        .build(),
                GithubAccount.builder()
                        .id(user.getGithubUserId())
                        .login(user.getGithubLogin())
                        .installed(true)
                        .installationId(3L)
                        .build()
        ));
        final List<GithubAccount> organizationsForGithubPersonalToken =
                githubAccountService.getOrganizationsForAuthenticatedUserAndGithubPersonalToken(githubPAT, user);

        // Then
        assertEquals(List.of(
                        githubAccounts.get(0).toBuilder().isCurrentUserAdmin(true).build(),
                        githubAccounts.get(1)
                                .toBuilder()
                                .installed(true)
                                .installationId(1L)
                                .isCurrentUserAdmin(false)
                                .isPersonal(false)
                                .build(),
                        GithubAccount.builder()
                                .installed(true)
                                .id(user.getGithubUserId())
                                .login(user.getGithubLogin())
                                .isPersonal(true)
                                .isCurrentUserAdmin(true)
                                .installationId(3L)
                                .build())
                , organizationsForGithubPersonalToken);
    }
}
