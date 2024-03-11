package onlydust.com.marketplace.api.infrastructure.accounting;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.RewardStatusData;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountingObserverAdapterTest {
    final RewardStatusStorage rewardStatusStorage = mock(RewardStatusStorage.class);
    final RewardStatusFacadePort rewardStatusFacadePort = mock(RewardStatusFacadePort.class);
    final AccountingObserverAdapter accountingObserverAdapter = new AccountingObserverAdapter(rewardStatusStorage, rewardStatusFacadePort);
    final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
    final RewardId rewardId = RewardId.random();
    final BigDecimal usdEquivalent = BigDecimal.TEN;
    final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        reset(rewardStatusStorage, rewardStatusFacadePort);

        when(rewardStatusStorage.notPaid(billingProfileId)).thenReturn(List.of(new RewardStatusData(rewardId)));
        when(rewardStatusFacadePort.usdEquivalent(rewardId)).thenReturn(usdEquivalent);

    }

    // TODO : migrate to accounting
//    @Test
//    void onBillingProfileUpdated() {
//        // When
//        accountingObserverAdapter.onBillingProfileUpdated(new BillingProfileVerificationUpdated(billingProfileId.value(), OldBillingProfileType.INDIVIDUAL,
//        null, null, null
//                , null, null, null, null, null, null, null));
//
//        // Then
//        final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
//        verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
//        final var rewardStatus = rewardStatusCaptor.getValue();
//        assertThat(rewardStatus.amountUsdEquivalent()).contains(usdEquivalent);
//    }

    @Test
    void onBillingProfilePayoutSettingsUpdated() {
        // When
        accountingObserverAdapter.onBillingProfilePayoutSettingsUpdated(billingProfileId.value(), null);

        // Then
        final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
        verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
        final var rewardStatus = rewardStatusCaptor.getValue();
        assertThat(rewardStatus.amountUsdEquivalent()).contains(usdEquivalent);
    }

    // TODO : migrate to accounting
//    @Test
//    void onBillingProfileSelectedIndividual() {
//        // When
//        accountingObserverAdapter.onBillingProfileSelected(userId,
//                OldIndividualBillingProfile.builder()
//                        .id(billingProfileId.value())
//                        .userId(userId)
//                        .status(OldVerificationStatus.VERIFIED)
//                        .build());
//
//        // Then
//        final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
//        verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
//        final var rewardStatus = rewardStatusCaptor.getValue();
//        assertThat(rewardStatus.amountUsdEquivalent()).contains(usdEquivalent);
//    }
//
//    @Test
//    void onBillingProfileSelectedCompany() {
//        // When
//        accountingObserverAdapter.onBillingProfileSelected(userId,
//                OldCompanyBillingProfile.builder()
//                        .id(billingProfileId.value())
//                        .userId(userId)
//                        .status(OldVerificationStatus.VERIFIED)
//                        .build());
//
//        // Then
//        final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatus.class);
//        verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
//        final var rewardStatus = rewardStatusCaptor.getValue();
//        assertThat(rewardStatus.amountUsdEquivalent()).contains(usdEquivalent);
//    }
}