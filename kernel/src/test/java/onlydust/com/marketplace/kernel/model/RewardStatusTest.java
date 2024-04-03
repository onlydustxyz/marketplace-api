package onlydust.com.marketplace.kernel.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static onlydust.com.marketplace.kernel.model.RewardStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class RewardStatusTest {

    @Test
    void should_get_reward_status_given_a_recipient() {
        assertEquals(PENDING_BILLING_PROFILE, PENDING_BILLING_PROFILE.asRecipient());
        for (RewardStatus rewardStatus : List.of(PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_COMPANY, PENDING_VERIFICATION, PAYMENT_BLOCKED,
                PAYOUT_INFO_MISSING, LOCKED, PENDING_REQUEST,
                PROCESSING, COMPLETE)) {
            assertThrowImpossibleStatus(rewardStatus::asRecipient, "Impossible %s status as recipient".formatted(rewardStatus.name()));
        }
    }

    @Test
    void should_get_reward_status_given_a_billing_profile_member() {
        assertEquals(PENDING_COMPANY, PENDING_VERIFICATION.asBillingProfileMember());
        assertEquals(PENDING_COMPANY, PAYMENT_BLOCKED.asBillingProfileMember());
        assertEquals(PENDING_COMPANY, PAYOUT_INFO_MISSING.asBillingProfileMember());
        assertEquals(PENDING_COMPANY, LOCKED.asBillingProfileMember());
        assertEquals(PENDING_COMPANY, PENDING_REQUEST.asBillingProfileMember());
        assertEquals(PROCESSING, PROCESSING.asBillingProfileMember());
        assertEquals(COMPLETE, COMPLETE.asBillingProfileMember());
        for (RewardStatus rewardStatus : List.of(PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY)) {
            assertThrowImpossibleStatus(rewardStatus::asBillingProfileMember, "Impossible %s status as billing profile member".formatted(rewardStatus.name()));
        }
    }

    @Test
    void should_get_reward_status_given_a_billing_profile_admin() {
        assertEquals(PENDING_VERIFICATION, PENDING_VERIFICATION.asBillingProfileAdmin());
        assertEquals(PAYMENT_BLOCKED, PAYMENT_BLOCKED.asBillingProfileAdmin());
        assertEquals(PAYOUT_INFO_MISSING, PAYOUT_INFO_MISSING.asBillingProfileAdmin());
        assertEquals(LOCKED, LOCKED.asBillingProfileAdmin());
        assertEquals(PENDING_REQUEST, PENDING_REQUEST.asBillingProfileAdmin());
        assertEquals(PROCESSING, PROCESSING.asBillingProfileAdmin());
        assertEquals(COMPLETE, COMPLETE.asBillingProfileAdmin());
        for (RewardStatus rewardStatus : List.of(PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY)) {
            assertThrowImpossibleStatus(rewardStatus::asBillingProfileAdmin, "Impossible %s status as billing profile admin".formatted(rewardStatus.name()));
        }
    }

    @Test
    void should_get_reward_status_given_a_project_lead() {
        assertEquals(PENDING_SIGNUP, PENDING_SIGNUP.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, PENDING_VERIFICATION.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, PAYMENT_BLOCKED.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, PAYOUT_INFO_MISSING.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, LOCKED.asProjectLead());
        assertEquals(PENDING_CONTRIBUTOR, PENDING_REQUEST.asProjectLead());
        assertEquals(PROCESSING, PROCESSING.asProjectLead());
        assertEquals(COMPLETE, COMPLETE.asProjectLead());
        for (RewardStatus rewardStatus : List.of(PENDING_CONTRIBUTOR, PENDING_COMPANY)) {
            assertThrowImpossibleStatus(rewardStatus::asProjectLead, "Impossible %s status as project lead".formatted(rewardStatus.name()));
        }
    }

    @Test
    void should_get_reward_status_given_backoffice_user() {
        assertEquals(PENDING_SIGNUP, PENDING_SIGNUP.asBackofficeUser());
        assertEquals(PENDING_BILLING_PROFILE, PENDING_BILLING_PROFILE.asBackofficeUser());
        assertEquals(PENDING_VERIFICATION, PENDING_VERIFICATION.asBackofficeUser());
        assertEquals(PAYMENT_BLOCKED, PAYMENT_BLOCKED.asBackofficeUser());
        assertEquals(PAYOUT_INFO_MISSING, PAYOUT_INFO_MISSING.asBackofficeUser());
        assertEquals(LOCKED, LOCKED.asBackofficeUser());
        assertEquals(PENDING_REQUEST, PENDING_REQUEST.asBackofficeUser());
        assertEquals(PROCESSING, PROCESSING.asBackofficeUser());
        assertEquals(COMPLETE, COMPLETE.asBackofficeUser());
        for (RewardStatus rewardStatus : List.of(PENDING_CONTRIBUTOR, PENDING_COMPANY)) {
            assertThrowImpossibleStatus(rewardStatus::asBackofficeUser, "Impossible %s status as backoffice user".formatted(rewardStatus.name()));
        }
    }

    void assertThrowImpossibleStatus(final Supplier<RewardStatus> rewardStatusSupplier, final String message) {
        Assertions.assertThatThrownBy(rewardStatusSupplier::get)
                .hasMessage(message)
                .isInstanceOf(OnlyDustException.class);

    }


    @Nested
    class ShouldGetRewardStatusForUser {


        @Test
        void given_a_recipient() {
            // Given
            final UUID rewardId = UUID.randomUUID();
            final long rewardRecipientId = 1L;
            final UUID rewardBillingProfileId = null;
            final long userGithubUserId = 1L;
            final List<UserBillingProfile> billingProfiles = List.of();

            // When
            final RewardStatus rewardStatusForUser = PENDING_BILLING_PROFILE.getRewardStatusForUser(rewardId, rewardRecipientId, rewardBillingProfileId,
                    userGithubUserId, billingProfiles);

            // Then
            assertEquals(PENDING_BILLING_PROFILE, rewardStatusForUser);
        }

        @Test
        void given_a_billing_profile_member() {
            // Given
            final UUID rewardId = UUID.randomUUID();
            final long rewardRecipientId = 1L;
            final UUID rewardBillingProfileId = UUID.randomUUID();
            final long userGithubUserId = 1L;
            final List<UserBillingProfile> billingProfiles = List.of(UserBillingProfile.builder()
                    .role(UserBillingProfile.Role.MEMBER)
                    .id(rewardBillingProfileId)
                    .build());

            // When
            final RewardStatus rewardStatusForUser = PAYOUT_INFO_MISSING.getRewardStatusForUser(rewardId, rewardRecipientId, rewardBillingProfileId,
                    userGithubUserId, billingProfiles);

            // Then
            assertEquals(PENDING_COMPANY, rewardStatusForUser);
        }

        @Test
        void given_a_billing_profile_admin() {
            // Given
            final UUID rewardId = UUID.randomUUID();
            final long rewardRecipientId = 1L;
            final UUID rewardBillingProfileId = UUID.randomUUID();
            final long userGithubUserId = 2L;
            final List<UserBillingProfile> billingProfiles = List.of(UserBillingProfile.builder()
                    .role(UserBillingProfile.Role.ADMIN)
                    .id(rewardBillingProfileId)
                    .build());

            // When
            final RewardStatus rewardStatusForUser = PAYOUT_INFO_MISSING.getRewardStatusForUser(rewardId, rewardRecipientId, rewardBillingProfileId,
                    userGithubUserId, billingProfiles);

            // Then
            assertEquals(PAYOUT_INFO_MISSING, rewardStatusForUser);
        }

        @Test
        void given_an_old_billing_profile_member() {
            // Given
            final UUID rewardId = UUID.randomUUID();
            final long rewardRecipientId = 1L;
            final UUID rewardBillingProfileId = UUID.randomUUID();
            final long userGithubUserId = 1L;
            final List<UserBillingProfile> billingProfiles = List.of(UserBillingProfile.builder()
                    .role(UserBillingProfile.Role.ADMIN)
                    .id(UUID.randomUUID())
                    .build());

            // When
            final RewardStatus rewardStatusForUser = PROCESSING.getRewardStatusForUser(rewardId, rewardRecipientId, rewardBillingProfileId,
                    userGithubUserId, billingProfiles);

            // Then
            assertEquals(PROCESSING, rewardStatusForUser);
        }

        @Test
        void given_an_invalid_user() {
            // Given
            final UUID rewardId = UUID.randomUUID();
            final long rewardRecipientId = 1L;
            final UUID rewardBillingProfileId = UUID.randomUUID();
            final long userGithubUserId = 2L;
            final List<UserBillingProfile> billingProfiles = List.of(UserBillingProfile.builder()
                    .role(UserBillingProfile.Role.ADMIN)
                    .id(UUID.randomUUID())
                    .build());

            // When
            Assertions.assertThatThrownBy(() -> PAYOUT_INFO_MISSING.getRewardStatusForUser(rewardId, rewardRecipientId, rewardBillingProfileId,
                            userGithubUserId, billingProfiles))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Error getting reward status for reward %s and user %s: Impossible %s status as old billing profile member"
                            .formatted(rewardId, userGithubUserId, PAYOUT_INFO_MISSING.name()));
        }
    }

}