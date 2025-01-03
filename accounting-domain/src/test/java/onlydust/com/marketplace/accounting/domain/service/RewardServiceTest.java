package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.InvoiceView;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.notification.RewardsPaid;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingSponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.*;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final AccountingFacadePort accountingFacadePort = mock(AccountingFacadePort.class);
    private final AccountingSponsorStoragePort sponsorStoragePort = mock(AccountingSponsorStoragePort.class);
    private final NotificationPort notificationPort = mock(NotificationPort.class);
    private final RewardService rewardService = new RewardService(accountingRewardStoragePort, accountingFacadePort, sponsorStoragePort, notificationPort);


    @BeforeEach
    void setUp() {
        reset(accountingRewardStoragePort, notificationPort);
    }

    @Test
    void should_notify_new_rewards_were_paid() {
        // Given
        final ShortContributorView recipient1 = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                UserId.random(), faker.internet().emailAddress());
        final ShortContributorView recipient2 = new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)),
                faker.rickAndMorty().character(), faker.gameOfThrones().character(),
                UserId.random(), faker.internet().emailAddress());
        final var r11 = generateRewardStubForCurrencyAndEmail(Currencies.USD, recipient1);
        final var r21 = generateRewardStubForCurrencyAndEmail(Currencies.STRK, recipient2);
        final var r12 = generateRewardStubForCurrencyAndEmail(Currencies.OP, recipient1);
        final var r22 = generateRewardStubForCurrencyAndEmail(Currencies.APT, recipient2);
        final List<RewardDetailsView> rewardViews = List.of(
                r11,
                r12,
                r21,
                r22
        );

        // When
        when(accountingRewardStoragePort.findPaidRewardsToNotify())
                .thenReturn(rewardViews);
        rewardService.notifyAllNewPaidRewards();

        // Then
        verify(notificationPort).push(recipient1.userId(), RewardsPaid.builder().shortRewards(
                Stream.of(r11, r12).map(rewardDetailsView -> ShortReward.builder().
                        id(rewardDetailsView.id())
                        .amount(rewardDetailsView.money().amount())
                        .projectName(rewardDetailsView.project().name())
                        .currencyCode(rewardDetailsView.money().currency().code().toString())
                        .dollarsEquivalent(rewardDetailsView.money().getDollarsEquivalentValue())
                        .sentByGithubLogin(rewardDetailsView.requester().login())
                        .contributionsCount(rewardDetailsView.githubUrls().size())
                        .build()).toList()).build());
        verify(notificationPort).push(recipient2.userId(), RewardsPaid.builder().shortRewards(
                Stream.of(r21, r22).map(rewardDetailsView -> ShortReward.builder().
                        id(rewardDetailsView.id())
                        .amount(rewardDetailsView.money().amount())
                        .projectName(rewardDetailsView.project().name())
                        .currencyCode(rewardDetailsView.money().currency().code().toString())
                        .dollarsEquivalent(rewardDetailsView.money().getDollarsEquivalentValue())
                        .sentByGithubLogin(rewardDetailsView.requester().login())
                        .contributionsCount(rewardDetailsView.githubUrls().size())
                        .build()).toList()).build());
        verify(accountingRewardStoragePort).markRewardsAsPaymentNotified(rewardViews.stream().map(RewardDetailsView::id).toList());
    }


    private RewardDetailsView generateRewardStubForCurrencyAndEmail(final Currency currency, final ShortContributorView recipient) {
        return RewardDetailsView.builder()
                .id(RewardId.random())
                .requester(new ShortContributorView(GithubUserId.of(faker.number().randomNumber(10, true)), faker.rickAndMorty().character(),
                        faker.gameOfThrones().character(),
                        UserId.random(), faker.internet().emailAddress()))
                .recipient(recipient)
                .status(RewardStatus.builder()
                        .projectId(ProjectId.random().value())
                        .recipientId(faker.number().randomNumber(4, true))
                        .status(RewardStatus.Input.PROCESSING)
                        .build())
                .billingProfile(new CompanyBillingProfile(faker.gameOfThrones().character(), UserId.random()))
                .requestedAt(ZonedDateTime.now())
                .githubUrls(List.of())
                .sponsors(List.of())
                .project(new ProjectShortView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(), faker.weather().description(),
                        faker.name().username()))
                .money(new MoneyView(BigDecimal.ONE, currency, null, null))
                .invoice(new InvoiceView(
                        Invoice.Id.random(),
                        new Invoice.BillingProfileSnapshot(BillingProfile.Id.random(), BillingProfile.Type.INDIVIDUAL, null, null, null, List.of()),
                        new UserView(
                                faker.number().randomNumber(),
                                faker.name().username(),
                                URI.create(faker.internet().avatar()),
                                faker.internet().emailAddress(),
                                UserId.random(),
                                faker.name().firstName()
                        ),
                        ZonedDateTime.now().minusDays(3),
                        Money.of(faker.number().randomNumber(), currency),
                        ZonedDateTime.now().plusDays(20),
                        Invoice.Number.fromString("OD-NAME-001"),
                        Invoice.Status.APPROVED,
                        List.of(),
                        null,
                        null,
                        null
                ))
                .receipts(List.of())
                .pendingPayments(new HashMap<>())
                .build();
    }
}
