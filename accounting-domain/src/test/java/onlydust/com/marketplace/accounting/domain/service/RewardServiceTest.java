package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.ProjectShortView;
import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.UserView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final AccountingFacadePort accountingFacadePort = mock(AccountingFacadePort.class);
    private final SponsorStoragePort sponsorStoragePort = mock(SponsorStoragePort.class);
    private final MailNotificationPort mailNotificationPort = mock(MailNotificationPort.class);
    private final RewardService rewardService = new RewardService(accountingRewardStoragePort, mailNotificationPort, accountingFacadePort, sponsorStoragePort);


    @BeforeEach
    void setUp() {
        reset(accountingRewardStoragePort, mailNotificationPort);
    }

    @Test
    void should_notify_new_rewards_were_paid() {
        // Given
        final String email1 = faker.rickAndMorty().character();
        final String email2 = faker.gameOfThrones().character();
        final var r11 = generateRewardStubForCurrencyAndEmail(Currencies.USD, email1);
        final var r21 = generateRewardStubForCurrencyAndEmail(Currencies.STRK, email2);
        final var r12 = generateRewardStubForCurrencyAndEmail(Currencies.OP, email1);
        final var r22 = generateRewardStubForCurrencyAndEmail(Currencies.APT, email2);
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
        verify(mailNotificationPort, times(1)).sendRewardsPaidMail(email1, List.of(r11, r12));
        verify(mailNotificationPort, times(1)).sendRewardsPaidMail(email2, List.of(r21, r22));
        verify(accountingRewardStoragePort).markRewardsAsPaymentNotified(rewardViews.stream().map(RewardDetailsView::id).toList());
    }


    private RewardDetailsView generateRewardStubForCurrencyAndEmail(final Currency currency, final String email) {
        return RewardDetailsView.builder()
                .id(RewardId.random())
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
                                email,
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
