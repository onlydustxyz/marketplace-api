package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.AccountingObserverPort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.InvoiceStoragePort;
import onlydust.com.marketplace.accounting.domain.port.out.ProjectAccountingObserver;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    final AccountBookObserver accountBookObserver = mock(AccountBookObserver.class);
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

    @Nested
    class SingleInstance {
        AccountBookEventStorageStub accountBookEventStorage;
        CachedAccountBookProvider cachedAccountBookProvider;
        AccountingService accountingService;

        private void setupAccountingService() {
            accountBookEventStorage = new AccountBookEventStorageStub();
            cachedAccountBookProvider = new CachedAccountBookProvider(accountBookEventStorage);
            accountingService = new AccountingService(cachedAccountBookProvider, sponsorAccountStorage, currencyStorage, accountingObserver,
                    projectAccountingObserver, invoiceStoragePort, accountBookObserver);
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
            final int numberOfThreads = 30;
            final int numberOfIterationPerThread = 50;
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
            assertThat(cachedAccountBookProvider.get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(projectId1)))
                    .isEqualTo(PositiveAmount.of((long) (numberOfThreads * numberOfIterationPerThread)));
        }
    }

    @Nested
    class MultiInstance {

        final static int INSTANCE_COUNT = 50;
        final static long SPONSOR_INITIAL_ALLOWANCE = 60;
        AccountBookEventStorageStub accountBookEventStorage;
        final List<CachedAccountBookProvider> accountBookProviders = new ArrayList<>();
        final List<AccountingService> accountingServices = new ArrayList<>();

        private void setupAccountingService() {
            accountBookEventStorage = new AccountBookEventStorageStub();
            for (int i = 0; i < INSTANCE_COUNT; i++) {
                accountBookProviders.add(new CachedAccountBookProvider(accountBookEventStorage));
                accountingServices.add(new AccountingService(accountBookProviders.get(i), sponsorAccountStorage, currencyStorage, accountingObserver,
                        projectAccountingObserver, invoiceStoragePort, accountBookObserver));
            }
        }

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(invoiceStoragePort.invoiceOf(any())).thenReturn(Optional.of(invoice));
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            sponsorAccount = accountingServices.get(0).createSponsorAccountWithInitialAllowance(
                    sponsorId,
                    currency.id(),
                    null,
                    PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE)
            ).account();
        }

        @Test
        public void should_register_allocations_to_project() throws InterruptedException {
            final int numberOfThreads = INSTANCE_COUNT;
            final int numberOfIterationPerThread = 50;
            final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            final CountDownLatch latch = new CountDownLatch(numberOfThreads);
            final var thrown = new ConcurrentLinkedQueue<Throwable>();

            // Given
            final var amount = PositiveAmount.of(1L);

            // When
            for (int t = 0; t < numberOfThreads; t++) {
                final int threadId = t;
                service.execute(() -> {
                    System.out.println("Instance " + Thread.currentThread().getName() + " started");
                    final var accountingService = accountingServices.get(threadId);
                    for (int i = 0; i < numberOfIterationPerThread; i++) {
                        try {
                            accountingService.allocate(sponsorAccount.id(), projectId1, amount, currency.id());
                        } catch (Exception e) {
                            thrown.add(e);
                        }
                    }
                    latch.countDown();
                    System.out.println("Instance " + Thread.currentThread().getName() + " ended");
                });
            }
            latch.await();

            // Then
            assertThat(thrown).isNotEmpty();
            assertThat(accountBookProviders.get(0).get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(sponsorAccount.id())))
                    .isEqualTo(PositiveAmount.of(0L));

            final var expectedEvents = new ArrayList<IdentifiedAccountBookEvent<?>>();
            expectedEvents.add(IdentifiedAccountBookEvent.of(1, new AccountBookAggregate.MintEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                    PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            for (int i = 1; i <= SPONSOR_INITIAL_ALLOWANCE; i++) {
                expectedEvents.add(IdentifiedAccountBookEvent.of(i + 1, new AccountBookAggregate.TransferEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                        AccountBook.AccountId.of(projectId1), PositiveAmount.of(1L))));
            }
            assertThat(accountBookEventStorage.getAll(currency)).containsExactlyElementsOf(expectedEvents);
        }

        @Test
        public void should_create_rewards() throws InterruptedException {
            final int numberOfThreads = INSTANCE_COUNT;
            final int numberOfIterationPerThread = 50;
            final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
            final CountDownLatch latch = new CountDownLatch(numberOfThreads);
            final var thrown = new ConcurrentLinkedQueue<Throwable>();

            // Given
            accountingServices.get(0).allocate(sponsorAccount.id(), projectId1, PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE), currency.id());
            final var amount = PositiveAmount.of(1L);

            // When
            for (int t = 0; t < numberOfThreads; t++) {
                final int threadId = t;
                service.execute(() -> {
                    System.out.println("Instance " + Thread.currentThread().getName() + " started");
                    final var accountingService = accountingServices.get(threadId);
                    for (int i = 0; i < numberOfIterationPerThread; i++) {
                        try {
                            accountingService.createReward(projectId1, RewardId.random(), amount, currency.id());
                        } catch (Exception e) {
                            thrown.add(e);
                        }
                    }
                    latch.countDown();
                    System.out.println("Instance " + Thread.currentThread().getName() + " ended");
                });
            }
            latch.await();

            // Then
            assertThat(thrown).isNotEmpty();
            assertThat(accountBookProviders.get(0).get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(sponsorAccount.id())))
                    .isEqualTo(PositiveAmount.of(0L));
            assertThat(accountBookProviders.get(0).get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(projectId1)))
                    .isEqualTo(PositiveAmount.of(0L));

            final var events = accountBookEventStorage.getAll(currency);
            assertThat(events).hasSize((int) (SPONSOR_INITIAL_ALLOWANCE + 2));
            assertThat(events.get(0)).isEqualTo(IdentifiedAccountBookEvent.of(1,
                    new AccountBookAggregate.MintEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                            PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            assertThat(events.get(1)).isEqualTo(IdentifiedAccountBookEvent.of(2,
                    new AccountBookAggregate.TransferEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                            AccountBook.AccountId.of(projectId1), PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            for (int i = 1; i <= SPONSOR_INITIAL_ALLOWANCE; i++) {
                assertThat(events.get(i + 1).id()).isEqualTo(i + 2);
                assertThat(events.get(i + 1).data()).isInstanceOf(AccountBookAggregate.TransferEvent.class);
                final var event = (AccountBookAggregate.TransferEvent) events.get(i + 1).data();
                assertThat(event.from()).isEqualTo(AccountBook.AccountId.of(projectId1));
                assertThat(event.to()).isNotNull();
                assertThat(event.amount()).isEqualTo(PositiveAmount.of(1L));

                assertThat(accountBookProviders.get(0).get(currency).state()
                        .balanceOf(event.to()))
                        .isEqualTo(PositiveAmount.of(1L));
            }
        }
    }


}
