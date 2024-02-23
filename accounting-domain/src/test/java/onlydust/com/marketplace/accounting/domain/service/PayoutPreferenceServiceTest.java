package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PayoutPreferenceServiceTest {

    private final PayoutPreferenceStoragePort payoutPreferenceStoragePort = mock(PayoutPreferenceStoragePort.class);
    private final BillingProfileStoragePort billingProfileStoragePort = mock(BillingProfileStoragePort.class);
    private final PayoutPreferenceService payoutPreferenceService = new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort);

    @BeforeEach
    void setUp() {
        reset(payoutPreferenceStoragePort, billingProfileStoragePort);
    }

    @Test
    void should_forbid_to_set_payout_preference_given_a_user_not_member_of_billing_profile() {
        // Given
        final ProjectId projectId = ProjectId.of(UUID.randomUUID());
        final UserId userId = UserId.of(UUID.randomUUID());
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(false);
        Exception exception = null;
        try {
            payoutPreferenceService.setPayoutPreference(projectId, billingProfileId, userId);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(403, ((OnlyDustException) exception).getStatus());
        assertEquals("User %s is not member of billing profile %s".formatted(userId.value(), billingProfileId.value()), exception.getMessage());
    }

    @Test
    void should_forbid_to_set_payout_preference_given_a_user_which_has_not_received_a_reward_on_given_project() {
        // Given
        final ProjectId projectId = ProjectId.of(UUID.randomUUID());
        final UserId userId = UserId.of(UUID.randomUUID());
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(true);
        when(payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId)).thenReturn(false);
        Exception exception = null;
        try {
            payoutPreferenceService.setPayoutPreference(projectId, billingProfileId, userId);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(403, ((OnlyDustException) exception).getStatus());
        assertEquals("Cannot set payout preference for user %s on project %s because user has not received any rewards on it"
                .formatted(userId.value(), projectId.value()), exception.getMessage());
    }

    @Test
    void should_set_payout_preference() {
        final ProjectId projectId = ProjectId.of(UUID.randomUUID());
        final UserId userId = UserId.of(UUID.randomUUID());
        final BillingProfile.Id billingProfileId = BillingProfile.Id.of(UUID.randomUUID());

        // When
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(true);
        when(payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId)).thenReturn(true);
        payoutPreferenceService.setPayoutPreference(projectId, billingProfileId, userId);

        // Then
        verify(payoutPreferenceStoragePort).save(projectId, billingProfileId, userId);
    }
}
