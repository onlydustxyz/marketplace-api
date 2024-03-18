package onlydust.com.marketplace.accounting.domain.service;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingRewardStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.MailNotificationPort;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.MoneyView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileAdminView;
import onlydust.com.marketplace.accounting.domain.view.ShortProjectView;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RewardServiceTest {

    private final Faker faker = new Faker();
    private final AccountingRewardStoragePort accountingRewardStoragePort = mock(AccountingRewardStoragePort.class);
    private final MailNotificationPort mailNotificationPort = mock(MailNotificationPort.class);
    private final RewardService rewardService = new RewardService(accountingRewardStoragePort, mailNotificationPort);


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
        final List<BackofficeRewardView> rewardViews = List.of(
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
        verify(accountingRewardStoragePort).markRewardsAsPaymentNotified(rewardViews.stream().map(BackofficeRewardView::id).toList());
    }


    @Test
    void should_search_for_batch_payment() {
        // Given
        final List<Invoice.Id> invoiceIds = List.of(Invoice.Id.of(UUID.randomUUID()));

        // When
        when(accountingRewardStoragePort.searchRewards(List.of(Invoice.Status.APPROVED), invoiceIds))
                .thenReturn(List.of(
                        generateRewardStubForCurrency(Currencies.ETH),
                        generateRewardStubForCurrency(Currencies.EUR),
                        generateRewardStubForCurrency(Currencies.OP),
                        generateRewardStubForCurrency(Currencies.USD),
                        generateRewardStubForCurrency(Currencies.USD),
                        generateRewardStubForCurrency(Currencies.USDC),
                        generateRewardStubForCurrency(Currencies.APT),
                        generateRewardStubForCurrency(Currencies.LORDS),
                        generateRewardStubForCurrency(Currencies.STRK),
                        BackofficeRewardView.builder()
                                .id(RewardId.random())
                                .status(RewardStatus.PROCESSING)
                                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                                        .admins(List.of(
                                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                                        faker.internet().avatar(),
                                                        faker.internet().emailAddress(),
                                                        faker.name().firstName(),
                                                        faker.name().lastName())
                                        ))
                                        .billingProfileName(faker.gameOfThrones().character())
                                        .billingProfileType(BillingProfile.Type.COMPANY)
                                        .billingProfileId(BillingProfile.Id.random())
                                        .build())
                                .requestedAt(ZonedDateTime.now())
                                .githubUrls(List.of())
                                .sponsors(List.of())
                                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(),
                                        faker.weather().description(), faker.name().username()))
                                .processedAt(ZonedDateTime.now())
                                .money(new MoneyView(BigDecimal.ONE, Currencies.USDC, null, null))
                                .transactionReferences(List.of(faker.random().hex()))
                                .paidToAccountNumbers(List.of(faker.random().hex()))
                                .build(),
                        BackofficeRewardView.builder()
                                .id(RewardId.random())
                                .status(RewardStatus.PROCESSING)
                                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                                        .admins(List.of(
                                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                                        faker.internet().avatar(),
                                                        faker.internet().emailAddress(),
                                                        faker.name().firstName(),
                                                        faker.name().lastName())
                                        ))
                                        .billingProfileName(faker.gameOfThrones().character())
                                        .billingProfileType(BillingProfile.Type.COMPANY)
                                        .billingProfileId(BillingProfile.Id.random())
                                        .build())
                                .requestedAt(ZonedDateTime.now())
                                .githubUrls(List.of())
                                .sponsors(List.of())
                                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(),
                                        faker.weather().description(), faker.name().username()))
                                .transactionReferences(List.of(faker.random().hex()))
                                .paidToAccountNumbers(List.of(faker.random().hex()))
                                .money(new MoneyView(BigDecimal.ONE, Currencies.USDC, null, null))
                                .build()
                ));
        final List<BackofficeRewardView> rewardViews = rewardService.searchRewardsByInvoiceIds(invoiceIds);

        // Then
        assertEquals(3, rewardViews.size());
        assertEquals(Currency.Code.USDC, rewardViews.get(0).money().currency().code());
        assertEquals(Currency.Code.LORDS, rewardViews.get(1).money().currency().code());
        assertEquals(Currency.Code.STRK, rewardViews.get(2).money().currency().code());
    }

    private BackofficeRewardView generateRewardStubForCurrency(final Currency currency) {
        return generateRewardStubForCurrencyAndEmail(currency, faker.rickAndMorty().character());
    }

    private BackofficeRewardView generateRewardStubForCurrencyAndEmail(final Currency currency, final String email) {
        return BackofficeRewardView.builder()
                .id(RewardId.random())
                .status(RewardStatus.PROCESSING)
                .billingProfileAdmin(ShortBillingProfileAdminView.builder()
                        .admins(List.of(
                                new ShortBillingProfileAdminView.Admin(faker.name().username(),
                                        faker.internet().avatar(),
                                        email,
                                        faker.name().firstName(),
                                        faker.name().lastName())
                        ))
                        .billingProfileName(faker.gameOfThrones().character())
                        .billingProfileType(BillingProfile.Type.COMPANY)
                        .billingProfileId(BillingProfile.Id.random())
                        .build())
                .requestedAt(ZonedDateTime.now())
                .githubUrls(List.of())
                .sponsors(List.of())
                .project(new ShortProjectView(ProjectId.random(), faker.rickAndMorty().character(), faker.internet().url(), faker.weather().description(),
                        faker.name().username()))
                .money(new MoneyView(BigDecimal.ONE, currency, null, null))
                .transactionReferences(List.of())
                .paidToAccountNumbers(List.of())
                .build();
    }
}
