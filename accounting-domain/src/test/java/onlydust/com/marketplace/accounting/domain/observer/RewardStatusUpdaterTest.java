package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.RewardStatusStorage;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.RewardStatusUpdater;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.AccountBookTest.accountBookFromEvents;
import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyb;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RewardStatusUpdaterTest {
    private RewardStatusFacadePort rewardStatusFacadePort;
    private RewardStatusStorage rewardStatusStorage;
    private InvoiceStoragePort invoiceStorage;
    private AccountBookFacade accountBookFacade;
    private RewardStatusUpdater rewardStatusUpdater;
    private AccountingRewardStoragePort accountingRewardStoragePort;
    final Faker faker = new Faker();
    final Currency currency = ETH;

    @BeforeEach
    void setUp() {
        rewardStatusFacadePort = mock(RewardStatusFacadePort.class);
        rewardStatusStorage = mock(RewardStatusStorage.class);
        accountBookFacade = mock(AccountBookFacade.class);
        invoiceStorage = mock(InvoiceStoragePort.class);
        accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
        rewardStatusUpdater = new RewardStatusUpdater(rewardStatusFacadePort, rewardStatusStorage, invoiceStorage,
                accountingRewardStoragePort);

        when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
            final var rewardId = invocation.getArgument(0, RewardId.class);
            return Optional.of(new RewardStatusData(rewardId));
        });
    }

    @Nested
    class OnRewardCreated {
        final SponsorAccount.Id sponsorAccountId = SponsorAccount.Id.random();
        final ProjectId projectId1 = ProjectId.random();
        AccountBookAggregate accountBook;
        RewardId rewardId = RewardId.random();

        @BeforeEach
        void setUp() {
            final var sponsorAccountAccountId = AccountId.of(sponsorAccountId);
            final var projectAccountId = AccountId.of(projectId1);
            final var rewardAccountId = AccountId.of(rewardId);

            accountBook = accountBookFromEvents(
                    new AccountBookAggregate.MintEvent(sponsorAccountAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(sponsorAccountAccountId, projectAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(projectAccountId, rewardAccountId, PositiveAmount.of(20L))
            );
        }

        @Test
        public void should_create_status_and_update_usd_equivalent() {
            // When
            rewardStatusUpdater.onRewardCreated(rewardId, accountBookFacade);

            // Then
            rewardStatusFacadePort.refreshRewardsUsdEquivalentOf(rewardId);
        }
    }

    @Nested
    class OnRewardPaid {
        RewardId rewardId = RewardId.random();

        @Test
        public void should_update_reward_and_invoice_status() {
            // Given
            final var rewardStatus = new RewardStatusData(rewardId)
                    .sponsorHasEnoughFund(true)
                    .unlockDate(ZonedDateTime.now().toInstant().atZone(ZoneOffset.UTC))
                    .invoiceReceivedAt(null)
                    .paidAt(null)
                    .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            final var rewardId2 = RewardId.random();
            final var rewardStatus2 = new RewardStatusData(rewardId2)
                    .sponsorHasEnoughFund(true)
                    .unlockDate(ZonedDateTime.now().toInstant().atZone(ZoneOffset.UTC))
                    .invoiceReceivedAt(null)
                    .paidAt(null)
                    .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            final var companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN)))
                    .enabled(true)
                    .build();

            var invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo);
            invoice = invoice.rewards(List.of(
                    new Invoice.Reward(rewardId, ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of()),
                    new Invoice.Reward(rewardId2, ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of())
            ));

            // When
            reset(rewardStatusStorage, invoiceStorage);
            when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(rewardStatus.paidAt(ZonedDateTime.now())));
            when(rewardStatusStorage.get(rewardId2)).thenReturn(Optional.of(rewardStatus2));
            when(invoiceStorage.invoiceOf(rewardId)).thenReturn(Optional.of(invoice));
            when(invoiceStorage.invoiceOf(rewardId2)).thenReturn(Optional.of(invoice));

            rewardStatusUpdater.onRewardPaid(rewardId);
            {
                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var newRewardStatus = rewardStatusCaptor.getValue();
                assertThat(newRewardStatus.rewardId()).isEqualTo(rewardId);
                assertThat(newRewardStatus.paidAt()).isNotNull();

                verify(invoiceStorage, never()).update(invoice.status(Invoice.Status.PAID));
            }

            // When
            reset(rewardStatusStorage, invoiceStorage);
            when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(rewardStatus.paidAt(ZonedDateTime.now())));
            when(rewardStatusStorage.get(rewardId2)).thenReturn(Optional.of(rewardStatus2.paidAt(ZonedDateTime.now())));
            when(invoiceStorage.invoiceOf(rewardId)).thenReturn(Optional.of(invoice));
            when(invoiceStorage.invoiceOf(rewardId2)).thenReturn(Optional.of(invoice));

            rewardStatusUpdater.onRewardPaid(rewardId2);
            {
                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var newRewardStatus = rewardStatusCaptor.getValue();
                assertThat(newRewardStatus.rewardId()).isEqualTo(rewardId2);
                assertThat(newRewardStatus.paidAt()).isNotNull();

                verify(invoiceStorage).update(invoice.status(Invoice.Status.PAID));
            }
        }
    }

    @Nested
    class OnSponsorAccountBalanceChanged {
        SponsorAccountStatement sponsorAccountStatement;
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setUp() {
            sponsorAccount = mock(SponsorAccount.class);
            sponsorAccountStatement = mock(SponsorAccountStatement.class);
            when(sponsorAccountStatement.account()).thenReturn(sponsorAccount);
        }

        @Test
        public void should_update_reward_status_data() {
            // When
            rewardStatusUpdater.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            verify(rewardStatusFacadePort).refreshRelatedRewardsStatuses(sponsorAccountStatement);
        }
    }

    @Nested
    class OnSponsorAccountUpdated {
        SponsorAccountStatement sponsorAccountStatement;
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setUp() {
            sponsorAccount = mock(SponsorAccount.class);
            sponsorAccountStatement = mock(SponsorAccountStatement.class);
            when(sponsorAccountStatement.account()).thenReturn(sponsorAccount);
        }

        @Test
        public void should_update_reward_status_data() {
            // When
            rewardStatusUpdater.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            verify(rewardStatusFacadePort).refreshRelatedRewardsStatuses(sponsorAccountStatement);
        }
    }

    @Nested
    class OnInvoiceUploaded {
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            final var companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN)))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo);

            invoice.rewards(List.of(
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of()),
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of()),
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of())
            ));

            when(invoiceStorage.get(invoice.id())).thenReturn(Optional.of(invoice));
        }

        @Test
        public void should_update_reward_status_data() {
            // Given
            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId));
            });

            // When
            rewardStatusUpdater.onInvoiceUploaded(BillingProfile.Id.random(), invoice.id(), true);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(3)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(3);
            assertThat(rewardStatuses).allMatch(r -> r.invoiceReceivedAt().orElseThrow().equals(invoice.createdAt()));
        }
    }

    @Nested
    class OnInvoiceRejected {
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            final var companyBillingProfile = CompanyBillingProfile.builder()
                    .id(billingProfileId)
                    .status(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN)))
                    .enabled(true)
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random(), payoutInfo);

            invoice.rewards(List.of(
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of()),
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of()),
                    new Invoice.Reward(RewardId.random(), ZonedDateTime.now().minusDays(1), faker.lordOfTheRings().location(),
                            Money.of(BigDecimal.ONE, ETH), Money.of(2700L, USD), invoice.id(), List.of())
            ));

            when(invoiceStorage.get(invoice.id())).thenReturn(Optional.of(invoice));
        }

        @Test
        public void should_update_reward_status_data() {
            // Given
            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId).invoiceReceivedAt(invoice.createdAt()));
            });

            // When
            rewardStatusUpdater.onInvoiceRejected(invoice.id(), "Invalid invoice");

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(3)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(3);
            assertThat(rewardStatuses).allMatch(r -> r.invoiceReceivedAt().isEmpty());
        }
    }

    @Nested
    class OnBillingProfileUpdated {

        @Test
        void should_refresh_usd_equivalent_given_a_kyc() {
            // Given
            final UUID kycId = UUID.randomUUID();
            final var billingProfileId = BillingProfile.Id.random();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kycId, billingProfileId,
                    VerificationType.KYC, VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final Kyc kyc =
                    Kyc.builder().id(kycId).billingProfileId(billingProfileId).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            rewardStatusUpdater.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusFacadePort).refreshRewardsUsdEquivalentOf(kyc.getBillingProfileId());
            verifyNoInteractions(accountingRewardStoragePort);
        }

        @Test
        void should_refresh_usd_equivalent_given_a_kyb() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final var billingProfileId = BillingProfile.Id.random();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, billingProfileId,
                    VerificationType.KYB, VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final Kyb kyb =
                    Kyb.builder().id(kybId).billingProfileId(billingProfileId).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            rewardStatusUpdater.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusFacadePort).refreshRewardsUsdEquivalentOf(kyb.getBillingProfileId());
            verifyNoInteractions(accountingRewardStoragePort);
        }

        @Test
        void should_refresh_usd_equivalent_given_a_kyc_children() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final var billingProfileId = BillingProfile.Id.random();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, billingProfileId,
                    VerificationType.KYC, VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(),
                    faker.gameOfThrones().character());
            final Kyb kyb =
                    Kyb.builder().id(kybId).billingProfileId(billingProfileId).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            rewardStatusUpdater.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusFacadePort).refreshRewardsUsdEquivalentOf(kyb.getBillingProfileId());
            verifyNoInteractions(accountingRewardStoragePort);
        }
    }

    @Nested
    class OnBillingProfileDeleted {
        @Test
        void should_refresh_usd_equivalent() {
            // Given
            final var billingProfileId = BillingProfile.Id.random();
            final var updatedRewardIds = List.of(RewardId.random(), RewardId.random());

            // When
            when(accountingRewardStoragePort.removeBillingProfile(billingProfileId)).thenReturn(updatedRewardIds);
            rewardStatusUpdater.onBillingProfileDeleted(billingProfileId);

            // Then
            verify(accountingRewardStoragePort).removeBillingProfile(billingProfileId);
            verify(rewardStatusFacadePort).refreshRewardsUsdEquivalentOf(updatedRewardIds);
        }
    }

    @Nested
    class OnBillingProfileDisabled {
        @Test
        void should_refresh_usd_equivalent() {
            // Given
            final var billingProfileId = BillingProfile.Id.random();
            final var updatedRewardIds = List.of(RewardId.random(), RewardId.random());

            // When
            when(accountingRewardStoragePort.removeBillingProfile(billingProfileId)).thenReturn(updatedRewardIds);
            rewardStatusUpdater.onBillingProfileEnableChanged(billingProfileId, false);

            // Then
            verify(accountingRewardStoragePort).removeBillingProfile(billingProfileId);
            verify(rewardStatusFacadePort).refreshRewardsUsdEquivalentOf(updatedRewardIds);
        }
    }

    @Nested
    class OnBillingProfileEnabled {
        @Test
        void should_refresh_usd_equivalent() {
            // Given
            final var billingProfileId = BillingProfile.Id.random();

            // When
            rewardStatusUpdater.onBillingProfileEnableChanged(billingProfileId, true);

            // Then
            verify(accountingRewardStoragePort, never()).removeBillingProfile(billingProfileId);
            verify(rewardStatusStorage, never()).get(any(List.class));
        }
    }
}
