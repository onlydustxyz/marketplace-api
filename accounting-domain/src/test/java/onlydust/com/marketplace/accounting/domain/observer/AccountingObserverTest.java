package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingObserver;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static onlydust.com.marketplace.accounting.domain.stubs.BillingProfileHelper.newKyb;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.ETH;
import static onlydust.com.marketplace.accounting.domain.stubs.Currencies.USD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingObserverTest {
    private RewardStatusStorage rewardStatusStorage;
    private InvoiceStoragePort invoiceStorage;
    private AccountBookFacade accountBookFacade;
    RewardUsdEquivalentStorage rewardUsdEquivalentStorage;
    QuoteStorage quoteStorage;
    CurrencyStorage currencyStorage;
    AccountingObserver accountingObserver;
    ReceiptStoragePort receiptStorage;
    final Faker faker = new Faker();
    final Currency currency = ETH;
    final Currency usd = USD;
    final BigDecimal rewardAmount = BigDecimal.valueOf(faker.number().randomNumber(3, true));
    final RewardUsdEquivalent rewardUsdEquivalent = mock(RewardUsdEquivalent.class);
    final ZonedDateTime equivalenceSealingDate = ZonedDateTime.now().minusDays(1);
    final BigDecimal price = BigDecimal.valueOf(123.25);

    @BeforeEach
    void setUp() {
        rewardStatusStorage = mock(RewardStatusStorage.class);
        accountBookFacade = mock(AccountBookFacade.class);
        rewardUsdEquivalentStorage = mock(RewardUsdEquivalentStorage.class);
        quoteStorage = mock(QuoteStorage.class);
        currencyStorage = mock(CurrencyStorage.class);
        invoiceStorage = mock(InvoiceStoragePort.class);
        receiptStorage = mock(ReceiptStoragePort.class);
        accountingObserver = new AccountingObserver(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage,
                receiptStorage);
        when(currencyStorage.findByCode(usd.code())).thenReturn(Optional.of(usd));

        when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
            final var rewardId = invocation.getArgument(0, RewardId.class);
            return Optional.of(new RewardStatusData(rewardId));
        });
        when(rewardUsdEquivalentStorage.get(any())).thenReturn(Optional.of(rewardUsdEquivalent));
        when(rewardUsdEquivalent.rewardAmount()).thenReturn(rewardAmount);
        when(rewardUsdEquivalent.rewardCurrencyId()).thenReturn(currency.id());
        when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.of(equivalenceSealingDate));
        when(quoteStorage.nearest(currency.id(), usd.id(), equivalenceSealingDate))
                .thenReturn(Optional.of(new Quote(currency.id(), usd.id(), price, equivalenceSealingDate.minusSeconds(30).toInstant())));
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

            accountBook = AccountBookAggregate.fromEvents(
                    new AccountBookAggregate.MintEvent(sponsorAccountAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(sponsorAccountAccountId, projectAccountId, PositiveAmount.of(100L)),
                    new AccountBookAggregate.TransferEvent(projectAccountId, rewardAccountId, PositiveAmount.of(20L))
            );
        }

        @Test
        public void should_create_status_and_update_usd_equivalent() {
            // Given
            final var rewardStatus = new RewardStatusData(rewardId)
                    .sponsorHasEnoughFund(true)
                    .unlockDate(ZonedDateTime.now().toInstant().atZone(ZoneOffset.UTC))
                    .invoiceReceivedAt(null)
                    .paidAt(null)
                    .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            when(accountBookFacade.isFunded(rewardId)).thenReturn(true);
            when(accountBookFacade.unlockDateOf(rewardId)).thenReturn(rewardStatus.unlockDate().map(ZonedDateTime::toInstant));
            when(accountBookFacade.networksOf(rewardId)).thenReturn(rewardStatus.networks());
            when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(rewardStatus));

            // When
            accountingObserver.onRewardCreated(rewardId, accountBookFacade);

            // Then
            verify(rewardStatusStorage, times(2)).save(any());
            assertThat(rewardStatus.usdAmount()).isPresent();
        }
    }

    @Nested
    class OnRewardCancelled {
        RewardId rewardId = RewardId.random();

        @Test
        public void should_delete_status() {
            // When
            accountingObserver.onRewardCancelled(rewardId);

            // Then
            verify(rewardStatusStorage).delete(rewardId);
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

            final var reference = new Payment.Reference(Network.ETHEREUM, "0x1234", "ofux", "ofux.eth");

            final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            final var companyBillingProfile = BillingProfileView.builder()
                    .id(billingProfileId)
                    .type(BillingProfile.Type.COMPANY)
                    .payoutInfo(payoutInfo)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .build();

            var invoice = Invoice.of(companyBillingProfile, 1, UserId.random());
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

            accountingObserver.onRewardPaid(rewardId);
            {
                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var newRewardStatus = rewardStatusCaptor.getValue();
                assertThat(newRewardStatus.rewardId()).isEqualTo(rewardId);
                assertThat(newRewardStatus.paidAt()).isNotNull();

                verify(invoiceStorage, never()).update(invoice.status(Invoice.Status.PAID));
            }

            accountingObserver.onPaymentReceived(rewardId, reference);
            {
                // Then
                final var receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
                verify(receiptStorage).save(receiptCaptor.capture());
                final var receipt = receiptCaptor.getValue();
                assertThat(receipt.id()).isNotNull();
                assertThat(receipt.rewardId()).isEqualTo(rewardId);
                assertThat(receipt.network()).isEqualTo(reference.network());
                assertThat(receipt.createdAt()).isBefore(ZonedDateTime.now());
                assertThat(receipt.reference()).isEqualTo(reference.reference());
                assertThat(receipt.thirdPartyName()).isEqualTo(reference.thirdPartyName());
                assertThat(receipt.thirdPartyAccountNumber()).isEqualTo(reference.thirdPartyAccountNumber());
            }

            // When
            reset(rewardStatusStorage, invoiceStorage, receiptStorage);
            when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(rewardStatus.paidAt(ZonedDateTime.now())));
            when(rewardStatusStorage.get(rewardId2)).thenReturn(Optional.of(rewardStatus2.paidAt(ZonedDateTime.now())));
            when(invoiceStorage.invoiceOf(rewardId)).thenReturn(Optional.of(invoice));
            when(invoiceStorage.invoiceOf(rewardId2)).thenReturn(Optional.of(invoice));

            accountingObserver.onRewardPaid(rewardId2);
            {
                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var newRewardStatus = rewardStatusCaptor.getValue();
                assertThat(newRewardStatus.rewardId()).isEqualTo(rewardId2);
                assertThat(newRewardStatus.paidAt()).isNotNull();

                verify(invoiceStorage).update(invoice.status(Invoice.Status.PAID));
            }

            accountingObserver.onPaymentReceived(rewardId2, reference);
            {
                // Then
                final var receiptCaptor = ArgumentCaptor.forClass(Receipt.class);
                verify(receiptStorage).save(receiptCaptor.capture());
                final var receipt = receiptCaptor.getValue();
                assertThat(receipt.id()).isNotNull();
                assertThat(receipt.rewardId()).isEqualTo(rewardId2);
                assertThat(receipt.network()).isEqualTo(reference.network());
                assertThat(receipt.createdAt()).isBefore(ZonedDateTime.now());
                assertThat(receipt.reference()).isEqualTo(reference.reference());
                assertThat(receipt.thirdPartyName()).isEqualTo(reference.thirdPartyName());
                assertThat(receipt.thirdPartyAccountNumber()).isEqualTo(reference.thirdPartyAccountNumber());
            }
        }
    }

    @Nested
    class OnSponsorAccountBalanceChanged {
        RewardId rewardId1 = RewardId.random();
        RewardId rewardId2 = RewardId.random();
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
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(1);
            when(sponsorAccountStatement.awaitingPayments()).thenReturn(Map.of(
                    rewardId1, PositiveAmount.of(100L),
                    rewardId2, PositiveAmount.of(2000L)
            ));
            when(sponsorAccountStatement.accountBookFacade()).thenReturn(accountBookFacade);
            when(accountBookFacade.isFunded(rewardId1)).thenReturn(true);
            when(accountBookFacade.isFunded(rewardId2)).thenReturn(false);
            when(accountBookFacade.unlockDateOf(any())).thenReturn(Optional.of(unlockDate.toInstant()));
            when(accountBookFacade.networksOf(any())).thenReturn(Set.of(Network.ETHEREUM, Network.OPTIMISM));

            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId));
            });

            // When
            accountingObserver.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(2)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(2);
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId1)).findFirst().orElseThrow().sponsorHasEnoughFund()).isTrue();
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId2)).findFirst().orElseThrow().sponsorHasEnoughFund()).isFalse();
            assertThat(rewardStatuses).allMatch(r -> r.networks().containsAll(Set.of(Network.ETHEREUM, Network.OPTIMISM)));
            assertThat(rewardStatuses).allMatch(r -> r.unlockDate().orElseThrow().toInstant().equals(unlockDate.toInstant()));
        }
    }

    @Nested
    class OnSponsorAccountUpdated {
        RewardId rewardId1 = RewardId.random();
        RewardId rewardId2 = RewardId.random();
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
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(1);
            when(sponsorAccountStatement.awaitingPayments()).thenReturn(Map.of(
                    rewardId1, PositiveAmount.of(100L),
                    rewardId2, PositiveAmount.of(2000L)
            ));
            when(sponsorAccountStatement.accountBookFacade()).thenReturn(accountBookFacade);
            when(accountBookFacade.isFunded(rewardId1)).thenReturn(false);
            when(accountBookFacade.isFunded(rewardId2)).thenReturn(true);
            when(accountBookFacade.unlockDateOf(any())).thenReturn(Optional.of(unlockDate.plusDays(1).toInstant()));
            when(accountBookFacade.networksOf(any())).thenReturn(Set.of(Network.APTOS, Network.OPTIMISM));

            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId)
                        .sponsorHasEnoughFund(rewardId.equals(rewardId1))
                        .unlockDate(unlockDate)
                        .withAdditionalNetworks(Set.of(Network.ETHEREUM, Network.OPTIMISM)));
            });

            // When
            accountingObserver.onSponsorAccountBalanceChanged(sponsorAccountStatement);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(2)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(2);
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId1)).findFirst().orElseThrow().sponsorHasEnoughFund()).isFalse();
            assertThat(rewardStatuses.stream().filter(r -> r.rewardId().equals(rewardId2)).findFirst().orElseThrow().sponsorHasEnoughFund()).isTrue();
            assertThat(rewardStatuses).allMatch(r -> r.networks().containsAll(Set.of(Network.APTOS, Network.OPTIMISM)));
            assertThat(rewardStatuses).allMatch(r -> r.unlockDate().orElseThrow().toInstant().equals(unlockDate.plusDays(1).toInstant()));
            assertThat(rewardStatuses).allMatch(r -> r.usdAmount().isPresent());
        }
    }

    @Nested
    class OnInvoiceUploaded {
        Invoice invoice;

        @BeforeEach
        void setUp() {
            final var payoutInfo = PayoutInfo.builder().ethWallet(new WalletLocator(new Name("vitalik.eth"))).build();
            final var billingProfileId = BillingProfile.Id.random();
            final var companyBillingProfile = BillingProfileView.builder()
                    .id(billingProfileId)
                    .type(BillingProfile.Type.COMPANY)
                    .payoutInfo(payoutInfo)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random());

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
            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId));
            });

            // When
            accountingObserver.onInvoiceUploaded(BillingProfile.Id.random(), invoice.id(), true);

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
            final var companyBillingProfile = BillingProfileView.builder()
                    .id(billingProfileId)
                    .type(BillingProfile.Type.COMPANY)
                    .payoutInfo(payoutInfo)
                    .verificationStatus(VerificationStatus.VERIFIED)
                    .name("OnlyDust")
                    .kyb(newKyb(billingProfileId, UserId.random()))
                    .build();

            invoice = Invoice.of(companyBillingProfile, 1, UserId.random());

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
            when(rewardStatusStorage.get(any())).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId).invoiceReceivedAt(invoice.createdAt()));
            });

            // When
            accountingObserver.onInvoiceRejected(new InvoiceRejected(
                    faker.internet().emailAddress(),
                    3L,
                    faker.internet().slug(),
                    faker.name().firstName(),
                    invoice.number().toString(),
                    invoice.rewards().stream().map(r -> InvoiceRejected.ShortReward.builder()
                            .id(r.id())
                            .projectName(r.projectName())
                            .currencyCode(currency.code().toString())
                            .amount(r.amount().getValue())
                            .dollarsEquivalent(r.amount().getValue())
                            .build()).toList(),
                    "Invalid invoice"
            ));

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(3)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(3);
            assertThat(rewardStatuses).allMatch(r -> r.invoiceReceivedAt().isEmpty());
        }
    }

    @Nested
    class UpdateUsdEquivalent {
        final RewardId rewardId = RewardId.random();

        @Nested
        class GivenANonExistingReward {
            @BeforeEach
            void setup() {
                when(rewardUsdEquivalentStorage.get(rewardId)).thenReturn(Optional.empty());
                when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.empty());
            }

            @Test
            void should_fail_if_reward_does_not_exist() {
                assertThatThrownBy(() -> accountingObserver.updateUsdEquivalent(rewardId))
                        .isInstanceOf(OnlyDustException.class)
                        .hasMessage("RewardStatus not found for reward %s".formatted(rewardId));
            }
        }

        @Nested
        class GivenAReward {
            final RewardUsdEquivalent rewardUsdEquivalent = mock(RewardUsdEquivalent.class);
            final BigDecimal rewardAmount = BigDecimal.valueOf(faker.number().randomNumber(3, true));

            @BeforeEach
            void setup() {
                when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(new RewardStatusData(rewardId)));
                when(rewardUsdEquivalentStorage.get(rewardId)).thenReturn(Optional.of(rewardUsdEquivalent));
                when(rewardUsdEquivalent.rewardAmount()).thenReturn(rewardAmount);
                when(rewardUsdEquivalent.rewardCurrencyId()).thenReturn(currency.id());
            }

            @Test
            void should_reset_usd_equivalent_if_no_equivalence_date_found() {
                // Given
                when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.empty());

                // When
                accountingObserver.updateUsdEquivalent(rewardId);

                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var rewardStatus = rewardStatusCaptor.getValue();
                assertThat(rewardStatus.usdAmount()).isEmpty();
            }

            @Test
            void should_update_usd_equivalent_if_equivalence_date_found() {
                // Given
                final var equivalenceSealingDate = ZonedDateTime.now().minusDays(1);
                final var price = BigDecimal.valueOf(123.25);
                when(rewardUsdEquivalent.equivalenceSealingDate()).thenReturn(Optional.of(equivalenceSealingDate));
                when(quoteStorage.nearest(currency.id(), usd.id(), equivalenceSealingDate))
                        .thenReturn(Optional.of(new Quote(currency.id(), usd.id(), price, equivalenceSealingDate.minusSeconds(30).toInstant())));

                // When
                accountingObserver.updateUsdEquivalent(rewardId);

                // Then
                final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
                verify(rewardStatusStorage).save(rewardStatusCaptor.capture());
                final var rewardStatus = rewardStatusCaptor.getValue();
                assertThat(rewardStatus.usdAmount().get().convertedAmount().getValue()).isEqualTo(price.multiply(rewardAmount));
                assertThat(rewardStatus.usdAmount().get().conversionRate()).isEqualTo(price);
            }
        }
    }

    @Nested
    class RefreshRewardsUsdEquivalent {
        @Test
        void should_refresh_usd_equivalent_for_all_rewards_not_paid() {
            // Given
            final var rewardId1 = RewardId.random();
            final var rewardId2 = RewardId.random();
            when(rewardStatusStorage.notPaid()).thenReturn(List.of(
                    new RewardStatusData(rewardId1),
                    new RewardStatusData(rewardId2)
            ));

            // When
            accountingObserver.refreshRewardsUsdEquivalents();

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(2)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(2);
            assertThat(rewardStatuses).allMatch(r -> r.usdAmount().isPresent());
        }
    }
}
