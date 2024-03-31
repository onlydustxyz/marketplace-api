package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.accounting.domain.service.AccountBookProvider;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountingServiceConcurrencyTest {
    final SponsorAccountStorageStub sponsorAccountStorage = new SponsorAccountStorageStub();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingObserverPort accountingObserver = mock(AccountingObserverPort.class);
    final ProjectAccountingObserver projectAccountingObserver = mock(ProjectAccountingObserver.class);
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    AccountBookEventStorageStub accountBookEventStorage;
    AccountBookProvider accountBookProvider;
    AccountingService accountingService;
    SponsorAccount sponsorAccount;
    final Currency currency = Currencies.USDC;
    final SponsorId sponsorId = SponsorId.random();
    final ProjectId projectId1 = ProjectId.random();
    final Faker faker = new Faker();
    final Invoice invoice = Invoice.of(BillingProfileView.builder()
                    .id(BillingProfile.Id.random())
                    .type(BillingProfile.Type.INDIVIDUAL)
                    .kyc(Kyc.builder().id(UUID.randomUUID()).ownerId(UserId.random()).status(VerificationStatus.VERIFIED).country(Country.fromIso3("FRA")).firstName(faker.name().firstName()).address(faker.address().fullAddress()).usCitizen(false).build())
                    .payoutInfo(PayoutInfo.builder().build())
                    .name("OnlyDust")
                    .build(), 1, UserId.random())
            .status(Invoice.Status.APPROVED)
            .rewards(List.of());

    private void setupAccountingService() {
        accountBookEventStorage = new AccountBookEventStorageStub();
        accountBookProvider = new AccountBookProvider(accountBookEventStorage);
        accountingService = new AccountingService(accountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver,
                projectAccountingObserver, invoiceStoragePort);
    }

    @BeforeEach
    void setup() {
        setupAccountingService();
        when(invoiceStoragePort.invoiceOf(any())).thenReturn(Optional.of(invoice));
        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(currencyStorage.all()).thenReturn(Set.of(currency));
        sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(
                sponsorId,
                currency.id(),
                null,
                PositiveAmount.of(1_000_000L)
        ).account();
    }

    @Test
    public void should_register_allocations_to_project() throws InterruptedException {
        final int numberOfThreads = 10;
        final int numberOfIterationPerThread = 1000;
        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);

        // Given
        final var amount = PositiveAmount.of(1L);

        // When
        for (int t = 0; t < numberOfThreads; t++) {
            service.execute(() -> {
                System.out.println("Thread " + Thread.currentThread().getName() + " started");
                for (int i = 0; i < numberOfIterationPerThread; i++) {
                    accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
                    accountingService.unallocate(projectId1, sponsorAccount.id(), amount, currency.id());
                    accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
                }
                latch.countDown();
                System.out.println("Thread " + Thread.currentThread().getName() + " ended");
            });
        }
        latch.await();

        // Then
        assertThat(accountBookProvider.get(currency).state()
                .balanceOf(AccountBook.AccountId.of(projectId1)))
                .isEqualTo(PositiveAmount.of((long) (numberOfThreads * numberOfIterationPerThread)));
    }


}
