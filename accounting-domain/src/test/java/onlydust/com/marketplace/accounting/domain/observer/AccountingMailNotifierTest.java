package onlydust.com.marketplace.accounting.domain.observer;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.*;
import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingMailNotifier;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.port.output.OutboxPort;
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

public class AccountingMailNotifierTest {
    private BillingProfileStoragePort billingProfileStoragePort;
    AccountingMailNotifier accountingMailNotifier;
    OutboxPort mailOutbox;
    InvoiceStoragePort invoiceStoragePort;
    AccountingRewardStoragePort accountingRewardStoragePort;
    final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        billingProfileStoragePort = mock(BillingProfileStoragePort.class);
        mailOutbox = mock(OutboxPort.class);
        accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
        invoiceStoragePort = mock(InvoiceStoragePort.class);
        accountingMailNotifier = new AccountingMailNotifier(billingProfileStoragePort, accountingRewardStoragePort, invoiceStoragePort, mailOutbox);
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
            accountingMailNotifier.onRewardCreated(rewardId, mock(AccountBookFacade.class));

            // Then
            verify(mailOutbox).push(new RewardCreated(recipient.email(), rewardDetailsView.githubUrls().size(),
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
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
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
            accountingMailNotifier.onRewardCancelled(rewardId);

            // Then
            verify(mailOutbox).push(new RewardCanceled(recipient.email(), recipient.login(), ShortReward.builder()
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
            final ShortContributorView recipient = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), null);
            final ShortContributorView requester = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                    faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                    UserId.random(), faker.internet().emailAddress());
            when(accountingRewardStoragePort.getShortReward(rewardId))
                    .thenReturn(Optional.of(ShortRewardDetailsView.builder()
                            .money(moneyView)
                            .id(rewardId)
                            .project(shortProjectView)
                            .recipient(recipient)
                            .requester(requester)
                            .build()));
            accountingMailNotifier.onRewardCancelled(rewardId);

            // Then
            verifyNoInteractions(mailOutbox);
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

            // When
            accountingMailNotifier.onInvoiceRejected(invoice.id(), "Invalid invoice");

            // Then
            final var rejectedArgumentCaptor = ArgumentCaptor.forClass(InvoiceRejected.class);
            verify(mailOutbox).push(rejectedArgumentCaptor.capture());
            assertThat(rejectedArgumentCaptor.getValue().rejectionReason()).isEqualTo("Invalid invoice");
            assertThat(rejectedArgumentCaptor.getValue().invoiceName()).isEqualTo(invoice.number().value());
            assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminEmail()).isEqualTo(invoiceCreator.email());
            assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminFirstName()).isEqualTo(invoiceCreator.firstName());
            assertThat(rejectedArgumentCaptor.getValue().billingProfileAdminGithubLogin()).isEqualTo(invoiceCreator.login());
            assertThat(rejectedArgumentCaptor.getValue().rewardCount()).isEqualTo(invoice.rewards().size());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getProjectName()).isEqualTo(invoice.rewards().get(0).projectName());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getAmount()).isEqualTo(invoice.rewards().get(0).amount().getValue());
            assertThat(rejectedArgumentCaptor.getValue().rewards().get(0).getCurrencyCode()).isEqualTo(invoice.rewards().get(0).amount().getCurrency().code().toString());

        }

        @Test
        void should_not_send_notification_given_a_billing_profile_admin_not_found() {
            // When
            assertThatThrownBy(() -> accountingMailNotifier.onInvoiceRejected(invoice.id(), "Invalid invoice"))
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
            accountingMailNotifier.onBillingProfileUpdated(billingProfileVerificationUpdated);

            // Then
            verify(mailOutbox).push(new BillingProfileVerificationFailed(shortContributorView.email(), UserId.of(userId), billingProfileId,
                    shortContributorView.login(),
                    billingProfileVerificationUpdated.getVerificationStatus()));
        }
    }
}
