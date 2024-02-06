package onlydust.com.marketplace.accounting.domain.model;

import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RewardStatusTest {
    RewardStatus status;

    @BeforeEach
    void setup() {
        status = new RewardStatus(RewardId.random()).rewardAmountUsdEquivalent(PositiveAmount.of(1000L));
    }

    @Test
    void pending_billing_profile() {
        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_BILLING_PROFILE);
    }

    @Test
    void kyc_rejected() {
        // Given
        status.isIndividual(true).kycbVerified(false);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_VERIFICATION);
    }

    @Test
    void kyb_rejected() {
        // Given
        status.isIndividual(false).kycbVerified(false);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_VERIFICATION);
    }

    @Test
    void us_recipient_with_strk() {
        // Given
        status.isIndividual(true).kycbVerified(true).usRecipient(true).rewardCurrency(Currencies.STRK);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PAYMENT_BLOCKED);
    }

    @Test
    void us_recipient_with_usdc() {
        // Given
        status.isIndividual(true).kycbVerified(true).usRecipient(true).rewardCurrency(Currencies.USDC).currentYearUsdTotal(PositiveAmount.ZERO);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PAYOUT_INFO_MISSING);
    }

    @Test
    void poor_individual() {
        // Given
        status.isIndividual(true).kycbVerified(true).usRecipient(false).currentYearUsdTotal(PositiveAmount.of(1000L));

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PAYOUT_INFO_MISSING);
    }

    @Test
    void rich_individual() {
        // Given
        status.isIndividual(true).kycbVerified(true).usRecipient(false).currentYearUsdTotal(PositiveAmount.of(4500L));

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PAYMENT_BLOCKED);
    }

    @Test
    void rich_company() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).currentYearUsdTotal(PositiveAmount.of(4500L));

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PAYOUT_INFO_MISSING);
    }


    @Test
    void sponsor_not_funded() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(false);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.LOCKED);
    }

    @Test
    void locked_tokens() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(true).unlockDate(ZonedDateTime.now().plusDays(1));

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.LOCKED);
    }

    @Test
    void unlocked_tokens() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(true).unlockDate(ZonedDateTime.now().minusDays(1));

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_REQUEST);
    }


    @Test
    void payment_not_requested() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(true);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_REQUEST);
    }

    @Test
    void manual_invoice_sent() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(true).paymentRequested(true);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PENDING_REQUEST);
    }

    @Test
    void invoice_approved() {
        // Given
        status.isIndividual(false).kycbVerified(true).usRecipient(false).payoutInfoFilled(true).sponsorHasEnoughFund(true).paymentRequested(true).invoiceApproved(true);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.PROCESSING);
    }

    @Test
    void paid() {
        // Given
        status.paid(true);

        // When
        assertThat(status.get()).isEqualTo(RewardStatus.Status.COMPLETE);
    }

}