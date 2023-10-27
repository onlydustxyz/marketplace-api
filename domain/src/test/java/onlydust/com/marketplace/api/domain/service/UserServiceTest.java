package onlydust.com.marketplace.api.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.mocks.DeterministicDateProvider;
import onlydust.com.marketplace.api.domain.model.*;
import onlydust.com.marketplace.api.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.api.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.api.domain.view.RewardItemView;
import onlydust.com.marketplace.api.domain.view.RewardView;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private final Faker faker = new Faker();
    private final DeterministicDateProvider dateProvider = new DeterministicDateProvider();

    @Test
    void should_find_user_given_a_github_id() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();
        final User user =
                User.builder().id(UUID.randomUUID()).avatarUrl(githubUserIdentity.getGithubAvatarUrl()).githubUserId(githubUserIdentity.getGithubUserId()).login(githubUserIdentity.getGithubLogin()).hasAcceptedLatestTermsAndConditions(true).hasSeenOnboardingWizard(true).build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.of(user));
        when(userStoragePort.getPayoutInformationById(user.getId())).thenReturn(
                UserPayoutInformation.builder()
                        .hasValidPerson(true)
                        .hasValidCompany(false)
                        .hasValidLocation(true)
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .hasMissingAptosWallet(false)
                                .hasMissingEthWallet(false)
                                .hasMissingStarknetWallet(false)
                                .hasMissingOptimismWallet(false)
                                .hasMissingBankingAccount(false)
                                .build()).build());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity);

        // Then
        assertEquals(user, userByGithubIdentity);
        assertEquals(true, userByGithubIdentity.getHasValidPayoutInfos());
    }

    @Test
    void should_find_user_given_a_github_id_with_no_payment_requests() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();
        final User user =
                User.builder().id(UUID.randomUUID()).avatarUrl(githubUserIdentity.getGithubAvatarUrl()).githubUserId(githubUserIdentity.getGithubUserId()).login(githubUserIdentity.getGithubLogin()).hasAcceptedLatestTermsAndConditions(true).hasSeenOnboardingWizard(true).build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.of(user));
        when(userStoragePort.getPayoutInformationById(user.getId())).thenReturn(
                UserPayoutInformation.builder()
                        .hasValidPerson(false)
                        .hasValidCompany(false)
                        .hasValidLocation(false)
                        .hasPendingPayments(false)
                        .payoutSettings(UserPayoutInformation.PayoutSettings.builder()
                                .hasMissingAptosWallet(false)
                                .hasMissingEthWallet(false)
                                .hasMissingStarknetWallet(false)
                                .hasMissingOptimismWallet(false)
                                .hasMissingBankingAccount(false)
                                .build()).build());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity);

        // Then
        assertEquals(user, userByGithubIdentity);
        assertEquals(true, userByGithubIdentity.getHasValidPayoutInfos());
    }


    @Test
    void should_create_user_on_the_fly_when_user_with_github_id_doesnt_exist() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final GithubUserIdentity githubUserIdentity =
                GithubUserIdentity.builder().githubUserId(faker.number().randomNumber()).githubAvatarUrl(faker.internet().avatar()).githubLogin(faker.hacker().verb()).build();

        // When
        when(userStoragePort.getUserByGithubId(githubUserIdentity.getGithubUserId())).thenReturn(Optional.empty());
        final User userByGithubIdentity = userService.getUserByGithubIdentity(githubUserIdentity);

        // Then
        assertThat(userByGithubIdentity.getId()).isNotNull();
        assertEquals(User.builder().id(userByGithubIdentity.getId()).avatarUrl(githubUserIdentity.getGithubAvatarUrl()).githubUserId(githubUserIdentity.getGithubUserId()).login(githubUserIdentity.getGithubLogin()).roles(List.of(UserRole.USER)).hasAcceptedLatestTermsAndConditions(false).hasSeenOnboardingWizard(false).build(), userByGithubIdentity);
    }


    @Test
    void should_find_user_profile_given_an_id() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();

        // When
        userService.updateTermsAndConditionsAcceptanceDate(userId);

        // Then
        verify(userStoragePort, times(1)).updateTermsAndConditionsAcceptanceDate(userId, dateProvider.now());
    }

    @Test
    void should_accept_lead_invitation() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();

        final UserProfile profile =
                UserProfile.builder().bio(faker.lorem().sentence()).website(faker.internet().url()).location(faker.address().city()).cover(UserProfileCover.CYAN).technologies(Map.of(faker.programmingLanguage().name(), faker.number().randomDigit(), faker.programmingLanguage().name(), faker.number().randomDigit())).contacts(List.of(Contact.builder().contact(faker.internet().url()).channel(Contact.Channel.WHATSAPP).visibility(Contact.Visibility.PUBLIC).build(), Contact.builder().contact(faker.internet().emailAddress()).channel(Contact.Channel.EMAIL).visibility(Contact.Visibility.PRIVATE).build())).build();

        final UserProfileView userProfileView = UserProfileView.builder().id(userId).bio(profile.getBio()).build();

        // When
        when(userStoragePort.getProfileById(userId)).thenReturn(userProfileView);
        final UserProfileView updatedUser = userService.updateProfile(userId, profile);

        // Then
        verify(userStoragePort, times(1)).saveProfile(userId, profile);
        assertThat(updatedUser.getBio()).isEqualTo(userProfileView.getBio());
    }


    @Test
    void should_validate_user_payout_profile_before_updating_it_given_wrong_aptos_address() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder().aptosAddress(faker.rickAndMorty().character()).build()).build();

        // When
        OnlyDustException onlyDustException = null;
        try {
            userService.updatePayoutInformation(userId, userPayoutInformation);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(400, onlyDustException.getStatus());
        assertEquals("Invalid wallet address format", onlyDustException.getMessage());
    }

    @Test
    void should_validate_user_payout_profile_before_updating_it_given_wrong_eth_address() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder().ethAddress(faker.rickAndMorty().character()).build()).build();

        // When
        OnlyDustException onlyDustException = null;
        try {
            userService.updatePayoutInformation(userId, userPayoutInformation);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(400, onlyDustException.getStatus());
        assertEquals("Invalid wallet address format", onlyDustException.getMessage());
    }

    @Test
    void should_validate_user_payout_profile_before_updating_it_given_wrong_stark_address() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder().starknetAddress(faker.rickAndMorty().character()).build()).build();

        // When
        OnlyDustException onlyDustException = null;
        try {
            userService.updatePayoutInformation(userId, userPayoutInformation);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(400, onlyDustException.getStatus());
        assertEquals("Invalid wallet address format", onlyDustException.getMessage());
    }

    @Test
    void should_validate_user_payout_info_before_updating_it_given_wrong_optimism_address() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder().optimismAddress(faker.rickAndMorty().character()).build()).build();

        // When
        OnlyDustException onlyDustException = null;
        try {
            userService.updatePayoutInformation(userId, userPayoutInformation);
        } catch (OnlyDustException e) {
            onlyDustException = e;
        }

        // Then
        assertNotNull(onlyDustException);
        assertEquals(400, onlyDustException.getStatus());
        assertEquals("Invalid wallet address format", onlyDustException.getMessage());
    }

    @Test
    void should_validate_user_payout_info_and_update_it_given_valid_wallet_addresses() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
        final UUID userId = UUID.randomUUID();
        final UserPayoutInformation userPayoutInformation =
                UserPayoutInformation.builder().payoutSettings(UserPayoutInformation.PayoutSettings.builder().optimismAddress("0x" + faker.crypto().md5()).starknetAddress("0X" + faker.random().hex()).ethName("0x" + faker.random().hex()).aptosAddress("0X" + faker.random().hex()).build()).build();

        // When
        userService.updatePayoutInformation(userId, userPayoutInformation);

        // Then
        verify(userStoragePort, times(1)).savePayoutInformationForUserId(userId, userPayoutInformation);
    }

    @Test
    void should_validate_reward_is_linked_to_user_given_a_valid_recipient_id() {
        // Given
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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
        final UserStoragePort userStoragePort = mock(UserStoragePort.class);
        final UserFacadePort userService = new UserService(userStoragePort, dateProvider);
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


}
