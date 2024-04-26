package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.*;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingObserver;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.observer.MailObserver;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.accounting.domain.AccountBookTest.accountBookFromEvents;
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
    private BillingProfileStoragePort billingProfileStoragePort;
    QuoteStorage quoteStorage;
    CurrencyStorage currencyStorage;
    AccountingObserver accountingObserver;
    ReceiptStoragePort receiptStorage;
    MailObserver mailObserver;
    AccountingRewardStoragePort accountingRewardStoragePort;
    NotificationPort notificationPort;
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
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        mailObserver = mock(MailObserver.class);
        accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
        notificationPort = mock(NotificationPort.class);
        when(currencyStorage.findByCode(usd.code())).thenReturn(Optional.of(usd));
        accountingObserver = new AccountingObserver(rewardStatusStorage, rewardUsdEquivalentStorage, quoteStorage, currencyStorage, invoiceStorage,
                receiptStorage, billingProfileStoragePort, mailObserver, accountingRewardStoragePort, notificationPort);

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

            accountBook = accountBookFromEvents(
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
            final MoneyView moneyView = new MoneyView(BigDecimal.ONE, Currency.crypto("OP", Currency.Code.OP, 3));
            final ProjectShortView shortProjectView = ProjectShortView.builder()
                    .name(faker.name().fullName())
                    .shortDescription(faker.rickAndMorty().character())
                    .logoUrl(faker.internet().url())
                    .id(ProjectId.random())
                    .slug(faker.lorem().characters())
                    .build();
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final RewardDetailsView rewardDetailsView = RewardDetailsView.builder()
                    .money(moneyView)
                    .id(rewardId)
                    .project(shortProjectView)
                    .recipient(recipient)
                    .sponsors(List.of())
                    .billingProfile(mock(BillingProfile.class))
                    .status(mock(RewardStatus.class))
                    .requestedAt(ZonedDateTime.now())
                    .githubUrls(List.of("https://github.com/onlydust/onlydust"))
                    .requester(requester)
                    .build();
            when(accountingRewardStoragePort.getReward(rewardId))
                    .thenReturn(Optional.of(rewardDetailsView));

            when(accountBookFacade.isFunded(rewardId)).thenReturn(true);
            when(accountBookFacade.unlockDateOf(rewardId)).thenReturn(rewardStatus.unlockDate().map(ZonedDateTime::toInstant));
            when(accountBookFacade.networksOf(rewardId)).thenReturn(rewardStatus.networks());
            when(rewardStatusStorage.get(rewardId)).thenReturn(Optional.of(rewardStatus));

            // When
            accountingObserver.onRewardCreated(rewardId, accountBookFacade);

            // Then
            verify(rewardStatusStorage, times(2)).save(any());
            assertThat(rewardStatus.usdAmount()).isPresent();
            verify(mailObserver).send(new RewardCreated(recipient.email(), rewardDetailsView.githubUrls().size(),
                    requester.login(), recipient.login(), ShortReward.builder()
                    .amount(rewardDetailsView.money().amount())
                    .currencyCode(rewardDetailsView.money().currency().code().toString())
                    .dollarsEquivalent(rewardDetailsView.money().getDollarsEquivalentValue())
                    .id(rewardId)
                    .projectName(shortProjectView.name())
                    .build(), recipient.userId().value()));
        }
    }

    @Nested
    class OnRewardCancelled {
        RewardId rewardId = RewardId.random();

        @Test
        public void should_cancel_reward() {
            // When
            final MoneyView moneyView = new MoneyView(BigDecimal.ONE, Currency.crypto("OP", Currency.Code.OP, 3));
            final ProjectShortView shortProjectView = ProjectShortView.builder()
                    .name(faker.name().fullName())
                    .shortDescription(faker.rickAndMorty().character())
                    .logoUrl(faker.internet().url())
                    .id(ProjectId.random())
                    .slug(faker.lorem().characters())
                    .build();
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final ShortRewardDetailsView rewardDetailsView = ShortRewardDetailsView.builder()
                    .money(moneyView)
                    .id(rewardId)
                    .project(shortProjectView)
                    .recipient(recipient)
                    .requester(requester)
                    .build();
            when(accountingRewardStoragePort.getShortReward(rewardId))
                    .thenReturn(Optional.of(rewardDetailsView));
            accountingObserver.onRewardCancelled(rewardId);

            // Then
            verify(rewardStatusStorage).delete(rewardId);
            verify(mailObserver).send(new RewardCanceled(recipient.email(), recipient.login(), ShortReward.builder()
                    .amount(rewardDetailsView.money().amount())
                    .currencyCode(rewardDetailsView.money().currency().code().toString())
                    .dollarsEquivalent(rewardDetailsView.money().getDollarsEquivalentValue())
                    .id(rewardId)
                    .projectName(shortProjectView.name())
                    .build(), recipient.userId().value()));
        }

        @Test
        public void should_cancel_reward_and_not_notify_mail_given_a_recipient_not_signed_up() {
            // When
            final MoneyView moneyView = mock(MoneyView.class);
            final ProjectShortView shortProjectView = ProjectShortView.builder()
                    .name(faker.name().fullName())
                    .shortDescription(faker.rickAndMorty().character())
                    .logoUrl(faker.internet().url())
                    .id(ProjectId.random())
                    .slug(faker.lorem().characters())
                    .build();
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), null);
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            when(accountingRewardStoragePort.getShortReward(rewardId))
                    .thenReturn(Optional.of(ShortRewardDetailsView.builder()
                            .money(moneyView)
                            .id(rewardId)
                            .project(shortProjectView)
                            .recipient(recipient)
                            .requester(requester)
                            .build()));
            accountingObserver.onRewardCancelled(rewardId);

            // Then
            verify(rewardStatusStorage).delete(rewardId);
            verifyNoInteractions(mailObserver);
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

            final var reference = new Payment.Reference(ZonedDateTime.now(), Network.ETHEREUM, "0x1234", "ofux", "ofux.eth");

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

            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
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

            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
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
            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
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
            when(rewardStatusStorage.get(any(RewardId.class))).then(invocation -> {
                final var rewardId = invocation.getArgument(0, RewardId.class);
                return Optional.of(new RewardStatusData(rewardId).invoiceReceivedAt(invoice.createdAt()));
            });
            final InvoiceRejected invalidInvoice = new InvoiceRejected(
                    faker.internet().emailAddress(),
                    3L,
                    faker.internet().slug(),
                    faker.name().firstName(),
                    UUID.randomUUID(),
                    invoice.number().toString(),
                    invoice.rewards().stream().map(r -> ShortReward.builder()
                            .id(r.id())
                            .projectName(r.projectName())
                            .currencyCode(currency.code().toString())
                            .amount(r.amount().getValue())
                            .dollarsEquivalent(r.amount().getValue())
                            .build()).toList(),
                    "Invalid invoice"
            );

            // When
            accountingObserver.onInvoiceRejected(invalidInvoice);

            // Then
            final var rewardStatusCaptor = ArgumentCaptor.forClass(RewardStatusData.class);
            verify(rewardStatusStorage, times(3)).save(rewardStatusCaptor.capture());
            final var rewardStatuses = rewardStatusCaptor.getAllValues();
            assertThat(rewardStatuses).hasSize(3);
            assertThat(rewardStatuses).allMatch(r -> r.invoiceReceivedAt().isEmpty());
            verify(mailObserver, times(1)).send(invalidInvoice);
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
            when(rewardStatusStorage.notRequested()).thenReturn(List.of(
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

    @Nested
    class OnBillingProfileUpdated {

        @Test
        void should_refresh_usd_equivalent_given_a_kyc() {
            // Given
            final UUID kycId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kycId, VerificationType.KYC,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final Kyc kyc =
                    Kyc.builder().id(kycId).billingProfileId(BillingProfile.Id.random()).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            when(billingProfileStoragePort.findKycById(kycId)).thenReturn(Optional.of(kyc));
            accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusStorage).notRequested(kyc.getBillingProfileId());
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
            verify(notificationPort).notify(billingProfileVerificationUpdated);
        }

        @Test
        void should_prevent_given_a_kyc_not_found() {
            // Given
            final UUID kycId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kycId, VerificationType.KYC,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);

            // When
            when(billingProfileStoragePort.findKycById(kycId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("KYC %s not found".formatted(kycId));
            verifyNoInteractions(rewardStatusStorage);
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
            verifyNoInteractions(notificationPort);
        }

        @Test
        void should_prevent_given_a_kyb_not_found() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, VerificationType.KYB,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);

            // When
            when(billingProfileStoragePort.findKybById(kybId)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("KYB %s not found".formatted(kybId));
            verifyNoInteractions(rewardStatusStorage);
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
            verifyNoInteractions(notificationPort);
        }


        @Test
        void should_refresh_usd_equivalent_given_a_kyb() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, VerificationType.KYB,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final Kyb kyb =
                    Kyb.builder().id(kybId).billingProfileId(BillingProfile.Id.random()).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            when(billingProfileStoragePort.findKybById(kybId)).thenReturn(Optional.of(kyb));
            accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusStorage).notRequested(kyb.getBillingProfileId());
            verify(notificationPort).notify(billingProfileVerificationUpdated);
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
        }

        @Test
        void should_refresh_usd_equivalent_given_a_kyc_children() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, VerificationType.KYC,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), faker.gameOfThrones().character());
            final Kyb kyb =
                    Kyb.builder().id(kybId).billingProfileId(BillingProfile.Id.random()).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId()))
                    .thenReturn(Optional.of(kyb));
            accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(rewardStatusStorage).notRequested(kyb.getBillingProfileId());
            verify(notificationPort).notify(billingProfileVerificationUpdated);
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
        }

        @Test
        void should_prevent_given_a_children_kyc_not_found() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, VerificationType.KYC,
                    VerificationStatus.VERIFIED, null, UserId.random(), null, faker.rickAndMorty().character(), faker.chuckNorris().fact());

            // When
            when(billingProfileStoragePort.findKybByParentExternalId(billingProfileVerificationUpdated.getParentExternalApplicantId())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("KYB not found for parentExternalApplicantId %s".formatted(billingProfileVerificationUpdated.getParentExternalApplicantId()));
            verifyNoInteractions(rewardStatusStorage);
            verifyNoInteractions(accountingRewardStoragePort);
            verifyNoInteractions(mailObserver);
            verifyNoInteractions(notificationPort);
        }

        @Test
        void should_notify_billing_profile_verification_failed() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, VerificationType.KYC,
                    VerificationStatus.CLOSED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final UUID userId = UUID.randomUUID();
            final ShortContributorView shortContributorView = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.of(userId), faker.internet().emailAddress());
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final Kyc kyc =
                    Kyc.builder().id(billingProfileVerificationUpdated.getVerificationId())
                            .billingProfileId(billingProfileId).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            when(billingProfileStoragePort.getBillingProfileOwnerById(billingProfileVerificationUpdated.getUserId()))
                    .thenReturn(Optional.of(shortContributorView));
            when(billingProfileStoragePort.findKycById(billingProfileVerificationUpdated.getVerificationId()))
                    .thenReturn(Optional.of(kyc));
            accountingObserver.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(mailObserver).send(new BillingProfileVerificationFailed(shortContributorView.email(), UserId.of(userId), billingProfileId,
                    shortContributorView.login(),
                    billingProfileVerificationUpdated.getVerificationStatus()));
            verify(notificationPort).notify(billingProfileVerificationUpdated);
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
            when(rewardStatusStorage.removeBillingProfile(billingProfileId)).thenReturn(updatedRewardIds);
            accountingObserver.onBillingProfileDeleted(billingProfileId);

            // Then
            verify(rewardStatusStorage).removeBillingProfile(billingProfileId);
            verify(rewardStatusStorage).get(updatedRewardIds);
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
            when(rewardStatusStorage.removeBillingProfile(billingProfileId)).thenReturn(updatedRewardIds);
            accountingObserver.onBillingProfileEnableChanged(billingProfileId, false);

            // Then
            verify(rewardStatusStorage).removeBillingProfile(billingProfileId);
            verify(rewardStatusStorage).get(updatedRewardIds);
        }
    }

    @Nested
    class OnBillingProfileEnabled {
        @Test
        void should_refresh_usd_equivalent() {
            // Given
            final var billingProfileId = BillingProfile.Id.random();

            // When
            accountingObserver.onBillingProfileEnableChanged(billingProfileId, true);

            // Then
            verify(rewardStatusStorage, never()).removeBillingProfile(billingProfileId);
            verify(rewardStatusStorage, never()).get(any(List.class));
        }
    }
}
