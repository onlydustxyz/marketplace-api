package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.mocks.DeterministicDateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.input.ProjectObserverPort;
import onlydust.com.marketplace.api.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.GithubSearchPort;
import onlydust.com.marketplace.api.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.*;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.api.domain.view.UserPayoutInformationTest.fakeValidUserPayoutInformation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private final Faker faker = new Faker();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();
    private UserStoragePort userStoragePort;
    private ProjectStoragePort projectStoragePort;
    private GithubSearchPort githubSearchPort;
    private ImageStoragePort imageStoragePort;
    private UserService userService;
    private ProjectObserverPort projectObserverPort;
    private UserObserverPort userObserverPort;

    @BeforeEach
    void setUp() {
        projectObserverPort = mock(ProjectObserverPort.class);
        userObserverPort = mock(UserObserverPort.class);
        userStoragePort = mock(UserStoragePort.class);
        projectStoragePort = mock(ProjectStoragePort.class);
        githubSearchPort = mock(GithubSearchPort.class);
        imageStoragePort = mock(ImageStoragePort.class);
        userService = new UserService(projectObserverPort, userObserverPort, userStoragePort, dateProvider,
                projectStoragePort, githubSearchPort, imageStoragePort);
    }

    @Test
    void should_find_user_given_a_github_id_and_update_it() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder()
                        .githubUserId(faker.number().randomNumber())
                        .githubAvatarUrl(faker.internet().avatar())
                        .githubLogin(faker.hacker().verb())
                        .email(faker.internet().emailAddress())
                        .build();

        final User user = User.builder()
                .id(UUID.randomUUID())
                .githubAvatarUrl(githubUserIdentity.getGithubAvatarUrl())
                .githubUserId(githubUserIdentity.getGithubUserId())
                .githubLogin(githubUserIdentity.getGithubLogin())
                .githubEmail(githubUserIdentity.getEmail())
                .hasAcceptedLatestTermsAndConditions(true)
                .hasSeenOnboardingWizard(true)
                .build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.of(user));
        when(userStoragePort.getPayoutInformationById(user.getId())).thenReturn(fakeValidUserPayoutInformation());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, times(1)).updateUserLastSeenAt(user.getId(), dateProvider.now());
        assertEquals(user, userByGithubIdentity);
        assertEquals(true, userByGithubIdentity.getHasValidPayoutInfos());
    }

    @Test
    void should_find_user_given_a_github_id_but_not_update_it_when_read_only_is_true() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();
        final User user =
                User.builder().id(UUID.randomUUID()).githubAvatarUrl(githubUserIdentity.getGithubAvatarUrl()).githubUserId(githubUserIdentity.getGithubUserId()).githubLogin(githubUserIdentity.getGithubLogin()).hasAcceptedLatestTermsAndConditions(true).hasSeenOnboardingWizard(true).build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.of(user));
        when(userStoragePort.getPayoutInformationById(user.getId())).thenReturn(fakeValidUserPayoutInformation());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, true);

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any(), any());
        assertEquals(user, userByGithubIdentity);
        assertEquals(true, userByGithubIdentity.getHasValidPayoutInfos());
    }

    @Test
    void should_create_user_on_the_fly_when_user_with_github_id_doesnt_exist() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber())
                        .githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();
        final Date createdAt = new Date();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.empty());
        when(userStoragePort.createUser(any())).thenReturn(User.builder().createdAt(createdAt).build());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity, false);

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any(), any());
        assertThat(userByGithubIdentity.getId()).isNotNull();
        assertEquals(User.builder().id(userByGithubIdentity.getId())
                .githubAvatarUrl(githubUserIdentity.getGithubAvatarUrl())
                .githubUserId(githubUserIdentity.getGithubUserId())
                .githubLogin(githubUserIdentity.getGithubLogin())
                .roles(List.of(UserRole.USER))
                .hasAcceptedLatestTermsAndConditions(false)
                .createdAt(createdAt)
                .hasSeenOnboardingWizard(false).build(), userByGithubIdentity);
    }

    @Test
    void should_throw_exception_when_user_with_github_id_doesnt_exist_and_read_only_is_true() {
        // Given
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.empty());

        // Then
        verify(userStoragePort, never()).updateUserLastSeenAt(any(), any());
        assertThatThrownBy(() -> userService.getUserByGithubIdentity(githubUserIdentity, true))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("User %d not found".formatted(githubUserIdentity.getGithubUserId()));
    }


    @Test
    void should_find_user_profile_given_an_id() {
        // Given
        final UUID userId = UUID.randomUUID();
        final UserProfileView userProfileView =
                UserProfileView.builder().id(userId).avatarUrl(faker.pokemon().name()).githubId(faker.number().randomNumber()).login(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getProfileById(userId)).thenReturn(userProfileView);
        final UserProfileView profileById = userService.getProfileById(userId);

        // Then
        assertEquals(userProfileView, profileById);
    }

    @Test
    void should_find_user_profile_given_a_github_id() {
        // Given
        final UUID userId = UUID.randomUUID();
        final Long githubUserId = faker.number().randomNumber();
        final UserProfileView userProfileView =
                UserProfileView.builder().id(userId).avatarUrl(faker.pokemon().name()).githubId(githubUserId).login(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getProfileById(githubUserId)).thenReturn(userProfileView);
        final UserProfileView profileById = userService.getProfileById(githubUserId);

        // Then
        assertEquals(userProfileView, profileById);
    }

    @Test
    void should_find_user_profile_given_a_github_login() {
        // Given
        final UUID userId = UUID.randomUUID();
        final Long githubUserId = faker.number().randomNumber();
        final String login = faker.name().username();
        final UserProfileView userProfileView =
                UserProfileView.builder().id(userId).avatarUrl(faker.pokemon().name()).githubId(githubUserId).login(login).build();

        // When
        when(userStoragePort.getProfileByLogin(login)).thenReturn(userProfileView);
        final UserProfileView profileByLogin = userService.getProfileByLogin(login);

        // Then
        assertEquals(userProfileView, profileByLogin);
    }

    @Test
    void should_markUserAsOnboarded() {
        // Given
        dateProvider.setNow(faker.date().birthday(0, 1));
        final UUID userId = UUID.randomUUID();

        // When
        userService.markUserAsOnboarded(userId);

        // Then
        verify(userStoragePort, times(1)).updateOnboardingWizardDisplayDate(userId, dateProvider.now());
    }

    @Test
    void should_updateTermsAndConditionsAcceptanceDate() {
        // Given
        dateProvider.setNow(faker.date().birthday(0, 1));
        final UUID userId = UUID.randomUUID();

        // When
        userService.updateTermsAndConditionsAcceptanceDate(userId);

        // Then
        verify(userStoragePort, times(1)).updateTermsAndConditionsAcceptanceDate(userId, dateProvider.now());
    }

    @Test
    void should_accept_lead_invitation() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final Long githubUserId = faker.number().randomNumber();

        // When
        userService.acceptInvitationToLeadProject(githubUserId, projectId);

        // Then
        verify(userStoragePort, times(1)).acceptProjectLeaderInvitation(githubUserId, projectId);
    }

    @Test
    void should_apply_on_project() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();

        // When
        userService.applyOnProject(userId, projectId);

        // Then
        verify(userStoragePort, times(1)).createApplicationOnProject(userId, projectId);
    }

    @Test
    void should_update_profile() {
        // Given
        final UUID userId = UUID.randomUUID();

        final UserProfile profile =
                UserProfile.builder()
                        .avatarUrl(faker.internet().avatar())
                        .bio(faker.lorem().sentence())
                        .website(faker.internet().url()).location(faker.address().city())
                        .cover(UserProfileCover.CYAN)
                        .technologies(Map.of(faker.programmingLanguage().name(), faker.number().randomNumber(),
                                faker.programmingLanguage().name(), faker.number().randomNumber()))
                        .contacts(List.of(Contact.builder().contact(faker.internet().url()).channel(Contact.Channel.WHATSAPP).visibility(Contact.Visibility.PUBLIC).build(), Contact.builder().contact(faker.internet().emailAddress()).channel(Contact.Channel.EMAIL).visibility(Contact.Visibility.PRIVATE).build())).build();

        final UserProfileView userProfileView = UserProfileView.builder().id(userId).bio(profile.getBio()).build();

        // When
        when(userStoragePort.getProfileById(userId)).thenReturn(userProfileView);
        final UserProfileView updatedUser = userService.updateProfile(userId, profile);

        // Then
        verify(userStoragePort, times(1)).saveProfile(userId, profile);
        assertThat(updatedUser.getBio()).isEqualTo(userProfileView.getBio());
    }

    @Test
    void should_validate_user_payout_info_and_update_it_given_valid_wallet_addresses() {
        // Given
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                        .optimismAddress(Optimism.accountAddress("0x2C6277931328e2028C3DB10625D767de19151e92"))
                        .starknetAddress(StarkNet.accountAddress(
                                "0x00b112c41d5a1a2282ecbe1ca4f4eead5a6c19269e884fc23522ecb0581e3597"))
                        .ethWallet(Ethereum.wallet("0xd8dA6BF26964aF9D7eEd9e03E53415D37aA96045"))
                        .aptosAddress(Aptos.accountAddress(
                                "0Xeeff357ea5c1a4e7bc11b2b17ff2dc2dcca69750bfef1e1ebcaccf8c8018175b"))
                        .build()).build();

        // When
        userService.updatePayoutInformation(userId, userPayoutInformation);

        // Then
        verify(userStoragePort, times(1)).savePayoutInformationForUserId(userId, userPayoutInformation);
    }

    @Test
    void should_validate_reward_is_linked_to_user_given_a_valid_recipient_id() {
        // Given
        final UUID rewardId = UUID.randomUUID();
        final long recipientId = 1L;
        final RewardView expectedReward =
                RewardView.builder().id(rewardId).to(GithubUserIdentity.builder().githubUserId(recipientId).build()).build();

        // When
        when(userStoragePort.findRewardById(rewardId))
                .thenReturn(expectedReward);
        final RewardView rewardView = userService.getRewardByIdForRecipientId(rewardId, recipientId);

        // Then
        assertNotNull(rewardView);
        assertEquals(expectedReward, rewardView);
    }

    @Test
    void should_validate_reward_is_linked_to_user_given_a_invalid_recipient_id() {
        // Given
        final UUID rewardId = UUID.randomUUID();
        final long recipientId = 1L;
        final RewardView expectedReward =
                RewardView.builder().id(rewardId).to(GithubUserIdentity.builder().githubUserId(2L).build()).build();

        // When
        when(userStoragePort.findRewardById(rewardId))
                .thenReturn(expectedReward);
        OnlyDustException onlyDustException = null;
        try {
            userService.getRewardByIdForRecipientId(rewardId, recipientId);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only recipient user can read it's own reward", onlyDustException.getMessage());
    }

    @Test
    void should_validate_reward_items_are_linked_to_user_given_a_valid_recipient_id() {
        // Given
        final UUID rewardId = UUID.randomUUID();
        final long recipientId = 2L;
        final Page<RewardItemView> expectedPage = Page.<RewardItemView>builder()
                .content(List.of(
                        RewardItemView.builder().githubAuthorId(1L).recipientId(2L).build(),
                        RewardItemView.builder().githubAuthorId(1L).recipientId(2L).build()
                ))
                .build();

        // When
        when(userStoragePort.findRewardItemsPageById(rewardId, 0, 20))
                .thenReturn(expectedPage);
        final Page<RewardItemView> page = userService.getRewardItemsPageByIdForRecipientId(rewardId, recipientId, 0,
                20);

        // Then
        assertNotNull(page);
        assertEquals(expectedPage, page);
    }

    @Test
    void should_validate_reward_items_are_linked_to_user_given_a_invalid_recipient_id() {
        final UUID rewardId = UUID.randomUUID();
        final long recipientId = 3L;
        final Page<RewardItemView> expectedPage = Page.<RewardItemView>builder()
                .content(List.of(
                        RewardItemView.builder().githubAuthorId(1L).recipientId(3L).build(),
                        RewardItemView.builder().githubAuthorId(1L).recipientId(4L).build()
                ))
                .build();

        // When
        when(userStoragePort.findRewardItemsPageById(rewardId, 0, 20))
                .thenReturn(expectedPage);
        OnlyDustException onlyDustException = null;
        try {
            userService.getRewardItemsPageByIdForRecipientId(rewardId, recipientId, 0,
                    20);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Only recipient user can read it's own reward", onlyDustException.getMessage());
    }

    @Test
    void should_fail_to_claim_project_with_project_leads() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final var caller = User.builder().build();

        // When
        when(projectStoragePort.getById(projectId, caller))
                .thenReturn(ProjectDetailsView.builder().leaders(Set.of(ProjectLeaderLinkView.builder().build())).build());
        OnlyDustException onlyDustException = null;
        try {
            userService.claimProjectForAuthenticatedUser(projectId, caller);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Project must have no project (pending) leads to be claimable", onlyDustException.getMessage());
    }

    @Test
    void should_fail_to_claim_project_with_pending_project_leads() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final var caller = User.builder().build();

        // When
        when(projectStoragePort.getById(projectId, caller))
                .thenReturn(ProjectDetailsView.builder().invitedLeaders(Set.of(ProjectLeaderLinkView.builder().build())).build());
        OnlyDustException onlyDustException = null;
        try {
            userService.claimProjectForAuthenticatedUser(projectId, caller);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Project must have no project (pending) leads to be claimable", onlyDustException.getMessage());
    }

    @Test
    void should_fail_to_claim_project_with_no_organizations() {
        // Given
        final UUID projectId = UUID.randomUUID();
        final var caller = User.builder().build();

        // When
        when(projectStoragePort.getById(projectId, caller))
                .thenReturn(ProjectDetailsView.builder().build());
        OnlyDustException onlyDustException = null;
        try {
            userService.claimProjectForAuthenticatedUser(projectId, caller);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("Project must have at least one organization to be claimable", onlyDustException.getMessage());
    }

    @Test
    void should_fail_to_claim_project_if_user_not_github_admin_on_every_orga() {
        // Given
        final String githubAccessToken = faker.rickAndMorty().character();
        final User user =
                User.builder().githubUserId(faker.random().nextLong()).githubLogin(faker.pokemon().name()).build();
        final UUID projectId = UUID.randomUUID();

        // When
        when(projectStoragePort.getById(projectId, user))
                .thenReturn(ProjectDetailsView.builder()
                        .organizations(Set.of(
                                ProjectOrganizationView.builder()
                                        .login("org1")
                                        .id(1L)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org2")
                                        .id(2L)
                                        .isInstalled(true)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org3")
                                        .id(3L)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org4")
                                        .id(4L)
                                        .isInstalled(true)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org5")
                                        .id(5L)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org6")
                                        .id(6L)
                                        .isInstalled(true)
                                        .build()
                        ))
                        .build());
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org1")).thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org2")).thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org3")).thenReturn(GithubMembership.MEMBER);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org4")).thenReturn(GithubMembership.MEMBER);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org5")).thenReturn(GithubMembership.EXTERNAL);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org6")).thenReturn(GithubMembership.EXTERNAL);
        OnlyDustException onlyDustException = null;
        try {
            userService.claimProjectForAuthenticatedUser(projectId, user);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(403, onlyDustException.getStatus());
        assertEquals("User must be github admin on every organizations not installed and at least member on every " +
                     "organization already installed linked to the project",
                onlyDustException.getMessage());
    }

    @Test
    void should_claim_project() {
        final String githubAccessToken = faker.rickAndMorty().character();
        final User user = User.builder().id(UUID.randomUUID())
                .githubUserId(faker.random().nextLong())
                .githubLogin(faker.pokemon().name()).build();
        final UUID projectId = UUID.randomUUID();

        // When
        when(projectStoragePort.getById(projectId, user))
                .thenReturn(ProjectDetailsView.builder()
                        .organizations(Set.of(
                                ProjectOrganizationView.builder()
                                        .login("org1")
                                        .id(1L)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org2")
                                        .id(2L)
                                        .isInstalled(true)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org3")
                                        .id(3l)
                                        .isInstalled(true)
                                        .build(),
                                ProjectOrganizationView.builder()
                                        .login("org4")
                                        .id(4L)
                                        .isInstalled(true)
                                        .id(user.getGithubUserId())
                                        .build()
                        ))
                        .build());
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org1")).thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org2")).thenReturn(GithubMembership.ADMIN);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org3")).thenReturn(GithubMembership.MEMBER);
        when(githubSearchPort.getGithubUserMembershipForOrganization(user.getGithubUserId(), user.getGithubLogin(),
                "org4")).thenReturn(GithubMembership.EXTERNAL);
        userService.claimProjectForAuthenticatedUser(projectId, user);

        // Then
        verify(userStoragePort, times(1)).saveProjectLead(user.getId(), projectId);
    }

    @Test
    void should_upload_avatar() throws MalformedURLException {
        // Given
        final InputStream imageInputStream = mock(InputStream.class);
        final String imageUrl = faker.internet().image();

        // When
        when(imageStoragePort.storeImage(imageInputStream)).thenReturn(new URL(imageUrl));
        final URL url = userService.saveAvatarImage(imageInputStream);

        // Then
        assertThat(url.toString()).isEqualTo(imageUrl);
    }

    @Test
    void should_refresh_active_user_profiles() {
        // Given
        final var since = ZonedDateTime.now().minusDays(30);
        final var users = List.of(
                User.builder().githubUserId(faker.number().randomNumber()).build(),
                User.builder().githubUserId(faker.number().randomNumber()).build(),
                User.builder().githubUserId(faker.number().randomNumber()).build()
        );
        final var githubUserIdentities = List.of(
                GithubUserIdentity.builder().githubUserId(users.get(0).getGithubUserId()).email(faker.internet().emailAddress()).build(),
                GithubUserIdentity.builder().githubUserId(users.get(2).getGithubUserId()).email(faker.internet().emailAddress()).build()
        );
        final var updatedUserProfiles = List.of(
                User.builder().githubUserId(githubUserIdentities.get(0).getGithubUserId()).githubEmail(githubUserIdentities.get(0).getEmail()).build(),
                User.builder().githubUserId(githubUserIdentities.get(1).getGithubUserId()).githubEmail(githubUserIdentities.get(1).getEmail()).build()
        );

        when(userStoragePort.getUsersLastSeenSince(since)).thenReturn(users);

        when(githubSearchPort.getUserProfile(users.get(0).getGithubUserId())).thenReturn(Optional.of(githubUserIdentities.get(0)));
        when(githubSearchPort.getUserProfile(users.get(1).getGithubUserId())).thenReturn(Optional.empty());
        when(githubSearchPort.getUserProfile(users.get(2).getGithubUserId())).thenReturn(Optional.of(githubUserIdentities.get(1)));

        // When
        userService.refreshActiveUserProfiles(since);

        // Then
        verify(userStoragePort, times(1)).saveUsers(updatedUserProfiles);
    }
}
