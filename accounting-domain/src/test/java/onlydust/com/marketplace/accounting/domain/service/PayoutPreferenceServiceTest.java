package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.IndividualBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.PayoutPreferenceStoragePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class PayoutPreferenceServiceTest {

    private final PayoutPreferenceStoragePort payoutPreferenceStoragePort = mock(PayoutPreferenceStoragePort.class);
    private final BillingProfileStoragePort billingProfileStoragePort = mock(BillingProfileStoragePort.class);
    private final AccountingObserverPort accountingObserverPort = mock(AccountingObserverPort.class);
    private final PayoutPreferenceService payoutPreferenceService = new PayoutPreferenceService(payoutPreferenceStoragePort, billingProfileStoragePort,
            accountingObserverPort);

    final ProjectId projectId = ProjectId.random();
    final UserId userId = UserId.random();
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();


    @BeforeEach
    void setUp() {
        reset(payoutPreferenceStoragePort, billingProfileStoragePort);
    }

    @Test
    void should_forbid_to_set_payout_preference_given_a_user_not_member_of_billing_profile() {
        // Given
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(IndividualBillingProfile.builder()
                .id(billingProfileId)
                .name("Billing profile")
                .enabled(true)
                .status(VerificationStatus.VERIFIED)
                .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build()));

        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(false);

        // When
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
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());
    }

    @Test
    void should_forbid_to_set_payout_preference_given_a_user_which_has_not_received_a_reward_on_given_project() {
        // Given
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(IndividualBillingProfile.builder()
                .id(billingProfileId)
                .name("Billing profile")
                .enabled(true)
                .status(VerificationStatus.VERIFIED)
                .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build()));
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(true);
        when(payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId)).thenReturn(false);

        // When
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
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());
    }

    @Test
    void should_forbid_to_set_payout_preference_given_a_disabled_billing_profile() {
        // Given
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(IndividualBillingProfile.builder()
                .id(billingProfileId)
                .name("Billing profile")
                .enabled(false)
                .status(VerificationStatus.VERIFIED)
                .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build()));
        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(true);
        when(payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId)).thenReturn(true);

        // When
        Exception exception = null;
        try {
            payoutPreferenceService.setPayoutPreference(projectId, billingProfileId, userId);
        } catch (Exception e) {
            exception = e;
        }

        // Then
        assertTrue(exception instanceof OnlyDustException);
        assertEquals(403, ((OnlyDustException) exception).getStatus());
        assertEquals("Cannot set payout preference for user %s on project %s because billing profile %s is disabled"
                .formatted(userId.value(), projectId.value(), billingProfileId.value()), exception.getMessage());
        verify(accountingObserverPort, never()).onPayoutPreferenceChanged(any(), any(), any());
    }

    @Test
    void should_set_payout_preference() {
        when(billingProfileStoragePort.findById(billingProfileId)).thenReturn(Optional.of(IndividualBillingProfile.builder()
                .id(billingProfileId)
                .name("Billing profile")
                .enabled(true)
                .status(VerificationStatus.VERIFIED)
                .kyc(Kyc.initForUserAndBillingProfile(userId, billingProfileId))
                .owner(new BillingProfile.User(userId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                .build()));

        when(billingProfileStoragePort.isUserMemberOf(billingProfileId, userId)).thenReturn(true);
        when(payoutPreferenceStoragePort.hasUserReceivedSomeRewardsOnProject(userId, projectId)).thenReturn(true);

        // When
        payoutPreferenceService.setPayoutPreference(projectId, billingProfileId, userId);

        // Then
        verify(payoutPreferenceStoragePort).save(projectId, billingProfileId, userId);
        verify(accountingObserverPort).onPayoutPreferenceChanged(billingProfileId, userId, projectId);
    }
}
