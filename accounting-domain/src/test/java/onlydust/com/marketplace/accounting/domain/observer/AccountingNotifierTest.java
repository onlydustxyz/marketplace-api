package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.notification.BillingProfileVerificationFailed;
import onlydust.com.marketplace.accounting.domain.notification.InvoiceRejected;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.accounting.domain.notification.dto.NotificationBillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.EmailStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingNotifier;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingNotifierTest {
    private BillingProfileStoragePort billingProfileStoragePort;
    AccountingNotifier accountingNotifier;
    InvoiceStoragePort invoiceStoragePort;
    AccountingRewardStoragePort accountingRewardStoragePort;
    NotificationPort notificationPort;
    EmailStoragePort emailStoragePort;
    final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
        invoiceStoragePort = mock(InvoiceStoragePort.class);
        notificationPort = mock(NotificationPort.class);
        emailStoragePort = mock(EmailStoragePort.class);
        accountingNotifier = new AccountingNotifier(billingProfileStoragePort, accountingRewardStoragePort, invoiceStoragePort, notificationPort,
                emailStoragePort);
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
            final MoneyView moneyView = new MoneyView(BigDecimal.ONE, Currency.crypto("OP", Currency.Code.OP, 3));
            final ProjectShortView shortProjectView = ProjectShortView.builder()
                    .name(faker.name().fullName())
                    .shortDescription(faker.rickAndMorty().character())
                    .logoUrl(faker.internet().url())
                    .id(ProjectId.random())
                    .slug(faker.lorem().characters())
                    .build();
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
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

            // When
            accountingNotifier.onRewardCreated(rewardId, mock(AccountBookFacade.class));

            // Then
            final var notificationPushCaptor = ArgumentCaptor.forClass(RewardReceived.class);
            verify(notificationPort).push(eq(recipient.userId().value()), notificationPushCaptor.capture());
            assertThat(notificationPushCaptor.getValue().contributionCount()).isEqualTo(1);
            assertThat(notificationPushCaptor.getValue().sentByGithubLogin()).isEqualTo(requester.login());
            assertThat(notificationPushCaptor.getValue().shortReward().getAmount()).isEqualTo(moneyView.amount());
        }
    }

    @Nested
    class OnRewardCanceled {
        RewardId rewardId = RewardId.random();

        @Test
        public void should_cancel_reward() {
            // Given
            final var amount = BigDecimal.ONE;
            final var recipientId = UserId.random();
            final var projectName = faker.name().fullName();
            final var reward = RewardDetailsView.builder()
                    .money(new MoneyView(amount, Currency.crypto("OP", Currency.Code.OP, 3)))
                    .id(rewardId)
                    .project(ProjectShortView.builder()
                            .name(projectName)
                            .shortDescription(faker.rickAndMorty().character())
                            .logoUrl(faker.internet().url())
                            .id(ProjectId.random())
                            .slug(faker.lorem().characters())
                            .build())
                    .recipient(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                            faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                            recipientId, faker.internet().emailAddress()))
                    .requester(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                            faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                            UserId.random(), faker.internet().emailAddress()))
                    .status(mock(RewardStatus.class))
                    .requestedAt(ZonedDateTime.now())
                    .githubUrls(List.of("https://github.com/onlydust/onlydust"))
                    .sponsors(List.of())
                    .build();

            when(accountingRewardStoragePort.getReward(rewardId)).thenReturn(Optional.of(reward));

            // When
            accountingNotifier.onRewardCancelled(rewardId);

            // Then
            final var notificationCaptor = ArgumentCaptor.forClass(RewardCanceled.class);
            verify(notificationPort).push(eq(recipientId.value()), notificationCaptor.capture());
            final var notification = notificationCaptor.getValue();
            assertThat(notification.shortReward().getId()).isEqualTo(rewardId);
            assertThat(notification.shortReward().getProjectName()).isEqualTo(projectName);
            assertThat(notification.shortReward().getAmount()).isEqualTo(amount);
            assertThat(notification.shortReward().getCurrencyCode()).isEqualTo(Currency.Code.OP.toString());
            assertThat(notification.shortReward().getDollarsEquivalent()).isNull();
        }

        @Test
        public void should_cancel_reward_and_not_notify_mail_given_a_recipient_not_signed_up() {
            // Given
            final var reward = RewardDetailsView.builder()
                    .money(mock(MoneyView.class))
                    .id(rewardId)
                    .project(ProjectShortView.builder()
                            .name(faker.name().fullName())
                            .shortDescription(faker.rickAndMorty().character())
                            .logoUrl(faker.internet().url())
                            .id(ProjectId.random())
                            .slug(faker.lorem().characters())
                            .build())
                    .recipient(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                            faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                            UserId.random(), null))
                    .requester(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                            faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                            UserId.random(), faker.internet().emailAddress()))
                    .status(mock(RewardStatus.class))
                    .requestedAt(ZonedDateTime.now())
                    .githubUrls(List.of("https://github.com/onlydust/onlydust"))
                    .sponsors(List.of())
                    .build();

            when(accountingRewardStoragePort.getReward(rewardId)).thenReturn(Optional.of(reward));

            // When
            accountingNotifier.onRewardCancelled(rewardId);

            // Then
            verifyNoInteractions(notificationPort);
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
                    .members(Set.of(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now())))
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

            when(invoiceStoragePort.get(invoice.id())).thenReturn(Optional.of(invoice));
        }

        @Test
        public void should_update_reward_status_data() {
            // Given
            final var invoiceCreator = BillingProfileCoworkerView.builder()
                    .userId(UserId.random())
                    .login(faker.name().username())
                    .email(faker.internet().emailAddress())
                    .firstName(faker.name().firstName())
                    .githubUserId(GithubUserId.of(faker.number().randomNumber(10, true)))
                    .role(BillingProfile.User.Role.ADMIN)
                    .joinedAt(ZonedDateTime.now())
                    .invitedAt(null)
                    .rewardCount(0)
                    .billingProfileAdminCount(1)
                    .build();

            when(billingProfileStoragePort.findBillingProfileAdmin(invoice.createdBy(), invoice.billingProfileSnapshot().id()))
                    .thenReturn(Optional.of(invoiceCreator));
            for (Invoice.Reward reward : invoice.rewards()) {
                when(accountingRewardStoragePort.getReward(reward.id()))
                        .thenReturn(Optional.of(RewardDetailsView.builder()
                                .money(new MoneyView(reward.amount().getValue(), reward.amount().getCurrency()))
                                .id(reward.id())
                                .project(ProjectShortView.builder()
                                        .name(reward.projectName())
                                        .shortDescription(faker.rickAndMorty().character())
                                        .logoUrl(faker.internet().url())
                                        .id(ProjectId.random())
                                        .slug(faker.lorem().characters())
                                        .build())
                                .recipient(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                                        faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                                        UserId.random(), faker.internet().emailAddress()))
                                .requester(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                                        faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                                        UserId.random(), faker.internet().emailAddress()))
                                .status(mock(RewardStatus.class))
                                .requestedAt(ZonedDateTime.now())
                                .githubUrls(List.of("https://github.com/onlydust/onlydust"))
                                .sponsors(List.of())
                                .build()));
            }

            // When
            accountingNotifier.onInvoiceRejected(invoice.id(), "Invalid invoice");

            // Then
            final var rejectedArgumentCaptor = ArgumentCaptor.forClass(InvoiceRejected.class);
            verify(notificationPort).push(eq(invoiceCreator.userId().value()), rejectedArgumentCaptor.capture());
            assertThat(rejectedArgumentCaptor.getValue().rejectionReason()).isEqualTo("Invalid invoice");
            assertThat(rejectedArgumentCaptor.getValue().invoiceName()).isEqualTo(invoice.number().value());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getProjectName()).isEqualTo(invoice.rewards().get(0).projectName());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getAmount()).isEqualTo(invoice.rewards().get(0).amount().getValue());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getCurrencyCode()).isEqualTo(invoice.rewards().get(0).amount().getCurrency().code().toString());
        }

        @Test
        void should_not_send_notification_given_a_billing_profile_admin_not_found() {
            // When
            assertThatThrownBy(() -> accountingNotifier.onInvoiceRejected(invoice.id(), "Invalid invoice"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Billing profile admin not found for billing profile %s".formatted(invoice.billingProfileSnapshot().id()));
        }
    }

    @Nested
    class OnBillingProfileUpdated {
        @Test
        void should_notify_billing_profile_verification_failed() {
            // Given
            final UUID kybId = UUID.randomUUID();
            final BillingProfile.Id billingProfileId = BillingProfile.Id.random();
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = new BillingProfileVerificationUpdated(kybId, billingProfileId,
                    VerificationType.KYC, VerificationStatus.CLOSED, null, UserId.random(), null, faker.rickAndMorty().character(), null);
            final UUID userId = UUID.randomUUID();
            final ShortContributorView shortContributorView = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.of(userId), faker.internet().emailAddress());
            final Kyc kyc =
                    Kyc.builder().id(billingProfileVerificationUpdated.getVerificationId())
                            .billingProfileId(billingProfileId).status(VerificationStatus.VERIFIED).ownerId(UserId.random()).build();

            // When
            when(billingProfileStoragePort.getBillingProfileOwnerById(billingProfileVerificationUpdated.getUserId()))
                    .thenReturn(Optional.of(shortContributorView));
            when(billingProfileStoragePort.findKycById(billingProfileVerificationUpdated.getVerificationId()))
                    .thenReturn(Optional.of(kyc));
            accountingNotifier.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(notificationPort).push(userId, BillingProfileVerificationFailed.builder()
                    .billingProfileId(billingProfileId)
                    .verificationStatus(billingProfileVerificationUpdated.getVerificationStatus())
                    .build());
        }
    }

    @Nested
    class OnBillingProfileExternalVerificationRequested {

        @Test
        void should_send_email_to_external_user() {
            // Given
            final BillingProfileChildrenKycVerification billingProfileChildrenKycVerification =
                    new BillingProfileChildrenKycVerification(new NotificationBillingProfile(UUID.randomUUID(), faker.name().name()),
                            new IndividualKycIdentity(faker.internet().emailAddress(), faker.name().firstName(), faker.name().lastName()),
                            faker.internet().url());

            // When
            accountingNotifier.onBillingProfileExternalVerificationRequested(billingProfileChildrenKycVerification);

            // Then
            verify(emailStoragePort).send(billingProfileChildrenKycVerification.individualKycIdentity().email(), billingProfileChildrenKycVerification);
        }
    }
}
