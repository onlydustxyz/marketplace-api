package onlydust.com.marketplace.kernel.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static onlydust.com.marketplace.kernel.model.RewardStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


class RewardStatusTest {

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_BILLING_PROFILE", "COMPLETE"})
    void valid_status_as_recipient(RewardStatus status) {
        assertEquals(status, status.asRecipient());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_BILLING_PROFILE", "COMPLETE"}, mode = EnumSource.Mode.EXCLUDE)
    void invalid_status_as_recipient(RewardStatus status) {
        assertThrowImpossibleStatus(status::asRecipient, "Impossible %s status as recipient".formatted(status.name()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PROCESSING", "COMPLETE"})
    void valid_status_as_billing_profile_member(RewardStatus status) {
        assertEquals(status, status.asBillingProfileMember());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_VERIFICATION", "GEO_BLOCKED", "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"})
    void pending_company_as_billing_profile_member(RewardStatus status) {
        assertEquals(PENDING_COMPANY, status.asBillingProfileMember());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_SIGNUP", "INDIVIDUAL_LIMIT_REACHED", "PENDING_CONTRIBUTOR", "PENDING_BILLING_PROFILE",
            "PENDING_COMPANY"})
    void invalid_status_as_billing_profile_member(RewardStatus status) {
        assertThrowImpossibleStatus(status::asBillingProfileMember, "Impossible %s status as billing profile member".formatted(status.name()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_VERIFICATION", "GEO_BLOCKED", "INDIVIDUAL_LIMIT_REACHED", "PAYOUT_INFO_MISSING", "LOCKED",
            "PENDING_REQUEST", "PROCESSING", "COMPLETE"})
    void valid_status_as_billing_profile_admin(RewardStatus status) {
        assertEquals(status, status.asBillingProfileAdmin());

        for (RewardStatus rewardStatus : List.of(PENDING_SIGNUP, PENDING_CONTRIBUTOR, PENDING_BILLING_PROFILE, PENDING_COMPANY)) {
            assertThrowImpossibleStatus(rewardStatus::asBillingProfileAdmin, "Impossible %s status as billing profile admin".formatted(rewardStatus.name()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_VERIFICATION", "GEO_BLOCKED", "INDIVIDUAL_LIMIT_REACHED", "PAYOUT_INFO_MISSING", "LOCKED",
            "PENDING_REQUEST", "PROCESSING", "COMPLETE"}, mode = EnumSource.Mode.EXCLUDE)
    void invalid_status_as_billing_profile_admin(RewardStatus status) {
        assertThrowImpossibleStatus(status::asBillingProfileAdmin, "Impossible %s status as billing profile admin".formatted(status.name()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_SIGNUP", "PROCESSING", "COMPLETE"})
    void valid_status_as_project_lead(RewardStatus status) {
        assertEquals(status, status.asProjectLead());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_BILLING_PROFILE", "PENDING_VERIFICATION", "GEO_BLOCKED", "INDIVIDUAL_LIMIT_REACHED",
            "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"})
    void pending_contributor_status_as_project_lead(RewardStatus status) {
        assertEquals(PENDING_CONTRIBUTOR, status.asProjectLead());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_CONTRIBUTOR", "PENDING_COMPANY"})
    void invalid_status_as_project_lead(RewardStatus status) {
        assertThrowImpossibleStatus(status::asProjectLead, "Impossible %s status as project lead".formatted(status.name()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_CONTRIBUTOR", "PENDING_COMPANY"}, mode = EnumSource.Mode.EXCLUDE)
    void valid_status_as_backoffice_user(RewardStatus status) {
        assertEquals(status, status.asBackofficeUser());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.class, names = {"PENDING_CONTRIBUTOR", "PENDING_COMPANY"})
    void invalid_status_as_backoffice_user(RewardStatus status) {
        assertThrowImpossibleStatus(status::asBackofficeUser, "Impossible %s status as backoffice user".formatted(status.name()));
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