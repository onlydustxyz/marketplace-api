package onlydust.com.marketplace.kernel.model;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardStatusTest {
    private final Faker faker = new Faker();
    private final UUID projectId = UUID.randomUUID();
    private final UUID billingProfileId = UUID.randomUUID();
    private final Long recipientId = faker.number().randomNumber(4, true);

    private RewardStatus rewardStatus(RewardStatus.Input status) {
        return RewardStatus.builder()
                .projectId(projectId)
                .billingProfileId(billingProfileId)
                .recipientId(recipientId)
                .status(status)
                .build();
    }

    private AuthenticatedUser recipient() {
        return AuthenticatedUser.builder().githubUserId(recipientId).build();
    }

    private AuthenticatedUser projectLead() {
        return AuthenticatedUser.builder()
                .githubUserId(faker.number().randomNumber(4, true))
                .projectsLed(List.of(projectId))
                .build();
    }

    private AuthenticatedUser billingProfileAdmin() {
        return AuthenticatedUser.builder()
                .githubUserId(faker.number().randomNumber(4, true))
                .administratedBillingProfiles(List.of(billingProfileId))
                .build();
    }

    private AuthenticatedUser nobody() {
        return AuthenticatedUser.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().randomNumber(4, true))
                .build();
    }

    private AuthenticatedUser backOfficeUser() {
        return AuthenticatedUser.builder().roles(List.of(AuthenticatedUser.Role.INTERNAL_SERVICE)).build();
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class, names = {"PENDING_VERIFICATION", "GEO_BLOCKED", "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"}, mode =
            EnumSource.Mode.EXCLUDE)
    void valid_status_as_recipient(RewardStatus.Input status) {
        assertEquals(status.toString(), rewardStatus(status).as(recipient()).name());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class, names = {"PENDING_VERIFICATION", "GEO_BLOCKED", "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"})
    void pending_company_as_recipient(RewardStatus.Input status) {
        assertEquals(RewardStatus.Output.PENDING_COMPANY, rewardStatus(status).as(recipient()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class, names = {"PENDING_BILLING_PROFILE", "PENDING_VERIFICATION", "GEO_BLOCKED", "INDIVIDUAL_LIMIT_REACHED",
            "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"}, mode = EnumSource.Mode.EXCLUDE)
    void valid_status_as_project_lead(RewardStatus.Input status) {
        assertEquals(status.toString(), rewardStatus(status).as(projectLead()).name());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class, names = {"PENDING_BILLING_PROFILE", "PENDING_VERIFICATION", "GEO_BLOCKED", "INDIVIDUAL_LIMIT_REACHED",
            "PAYOUT_INFO_MISSING", "LOCKED", "PENDING_REQUEST"})
    void pending_contributor_as_project_lead(RewardStatus.Input status) {
        assertEquals(RewardStatus.Output.PENDING_CONTRIBUTOR, rewardStatus(status).as(projectLead()));
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class)
    void valid_status_as_billing_profile_admin(RewardStatus.Input status) {
        assertEquals(status.toString(), rewardStatus(status).as(billingProfileAdmin()).name());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class)
    void valid_status_as_backoffice_user(RewardStatus.Input status) {
        assertEquals(status.toString(), rewardStatus(status).as(backOfficeUser()).name());
    }

    @ParameterizedTest
    @EnumSource(value = RewardStatus.Input.class)
    void forbidden_if_nobody(RewardStatus.Input status) {
        assertThatThrownBy(() -> rewardStatus(status).as(nobody()))
                .isInstanceOf(OnlyDustException.class)
                .hasMessageContaining("is not authorized to view this reward status");
    }

}