package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import org.junit.jupiter.api.*;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.ZonedDateTime;
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
    final RewardStatusFacadePort rewardStatusFacadePort = mock(RewardStatusFacadePort.class);
    final ReceiptStoragePort receiptStoragePort = mock(ReceiptStoragePort.class);
    final PermissionPort permissionPort = mock(PermissionPort.class);
    final OnlyDustWallets onlyDustWallets = mock(OnlyDustWallets.class);
    SponsorAccount sponsorAccount;
    final Currency currency = Currencies.USDC;
    final SponsorId sponsorId = SponsorId.random();
    final ProgramId programId = ProgramId.random();
    final Faker faker = new Faker();
    final Invoice invoice = Invoice.of(IndividualBillingProfile.builder()
                            .id(BillingProfile.Id.random())
                            .kyc(Kyc.builder()
                                    .id(UUID.randomUUID())
                                    .ownerId(UserId.random())
                                    .status(VerificationStatus.VERIFIED)
                                    .country(Country.fromIso3("FRA"))
                                    .firstName(faker.name().firstName())
                                    .address(faker.address().fullAddress())
                                    .consideredUsPersonQuestionnaire(false)
                                    .idDocumentCountry(Country.fromIso3("FRA"))
                                    .build())
                            .name("OnlyDust")
                            .enabled(true)
                            .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
                            .status(VerificationStatus.VERIFIED)
                            .build(), 1,
                    UserId.random(),
                    PayoutInfo.builder().build())
            .status(Invoice.Status.APPROVED)
            .rewards(List.of());

    @Nested
    class SingleInstance {
        AccountBookEventStorageStub accountBookEventStorage;
        CachedAccountBookProvider cachedAccountBookProvider;
        AccountingService accountingService;

        private void setupAccountingService() {
            accountBookEventStorage = new AccountBookEventStorageStub();
            cachedAccountBookProvider = new CachedAccountBookProvider(accountBookEventStorage, mock(AccountBookStorage.class), mock(AccountBookObserver.class));
            accountingService = new AccountingService(cachedAccountBookProvider,
                    sponsorAccountStorage,
                    currencyStorage,
                    accountingObserver,
                    projectAccountingObserver,
                    invoiceStoragePort,
                    rewardStatusFacadePort,
                    receiptStoragePort,
                    mock(BlockchainFacadePort.class),
                    mock(DepositStoragePort.class),
                    mock(TransactionStoragePort.class),
                    permissionPort,
                    onlyDustWallets,
                    mock(DepositObserverPort.class),
                    mock(AccountingSponsorStoragePort.class)
            );
        }

        @BeforeAll
        public static void init() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @AfterAll
        public static void cleanUp() {
            TransactionSynchronizationManager.clear();
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
            sponsorAccountStorage.save(sponsorAccount);
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
                    TransactionSynchronizationManager.initSynchronization();
                    try {
                        System.out.println("Thread " + Thread.currentThread().getName() + " started");
                        for (int i = 0; i < numberOfIterationPerThread; i++) {
                            accountingService.allocate(sponsorId, programId, amount, currency.id());
                            accountingService.unallocate(programId, sponsorId, amount, currency.id());
                            accountingService.allocate(sponsorId, programId, amount, currency.id());
                        }
                        latch.countDown();
                        System.out.println("Thread " + Thread.currentThread().getName() + " ended");
                    } finally {
                        TransactionSynchronizationManager.clear();
                    }
                });
            }
            latch.await();

            // Then
            assertThat(cachedAccountBookProvider.get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(programId)))
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
                accountBookProviders.add(new CachedAccountBookProvider(accountBookEventStorage, mock(AccountBookStorage.class),
                        mock(AccountBookObserver.class)));
                accountingServices.add(new AccountingService(accountBookProviders.get(i),
                        sponsorAccountStorage,
                        currencyStorage,
                        accountingObserver,
                        projectAccountingObserver,
                        invoiceStoragePort,
                        rewardStatusFacadePort,
                        receiptStoragePort,
                        mock(BlockchainFacadePort.class),
                        mock(DepositStoragePort.class),
                        mock(TransactionStoragePort.class),
                        permissionPort,
                        onlyDustWallets,
                        mock(DepositObserverPort.class),
                        mock(AccountingSponsorStoragePort.class)));
            }
        }

        @BeforeAll
        public static void init() {
            TransactionSynchronizationManager.initSynchronization();
        }

        @AfterAll
        public static void cleanUp() {
            TransactionSynchronizationManager.clear();
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
        public void should_register_allocations_to_program() throws InterruptedException {
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
                    TransactionSynchronizationManager.initSynchronization();
                    try {
                        final var accountingService = accountingServices.get(threadId);
                        for (int i = 0; i < numberOfIterationPerThread; i++) {
                            try {
                                accountingService.allocate(sponsorId, programId, amount, currency.id());
                            } catch (Exception e) {
                                thrown.add(e);
                            }
                        }
                        latch.countDown();
                    } finally {
                        TransactionSynchronizationManager.clear();
                    }
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
                        AccountBook.AccountId.of(programId), PositiveAmount.of(1L))));
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
            final var projectId = ProjectId.random();

            // Given
            accountingServices.get(0).allocate(sponsorId, programId, PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE), currency.id());
            accountingServices.get(0).grant(programId, projectId, PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE), currency.id());
            final var amount = PositiveAmount.of(1L);

            // When
            for (int t = 0; t < numberOfThreads; t++) {
                final int threadId = t;
                service.execute(() -> {
                    TransactionSynchronizationManager.initSynchronization();
                    try {
                        final var accountingService = accountingServices.get(threadId);
                        for (int i = 0; i < numberOfIterationPerThread; i++) {
                            try {
                                accountingService.createReward(projectId, RewardId.random(), amount, currency.id());
                            } catch (Exception e) {
                                thrown.add(e);
                            }
                        }
                        latch.countDown();
                    } finally {
                        TransactionSynchronizationManager.clear();
                    }
                });
            }
            latch.await();

            // Then
            assertThat(thrown).isNotEmpty();
            assertThat(accountBookProviders.get(0).get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(sponsorAccount.id())))
                    .isEqualTo(PositiveAmount.of(0L));
            assertThat(accountBookProviders.get(0).get(currency).state()
                    .balanceOf(AccountBook.AccountId.of(projectId)))
                    .isEqualTo(PositiveAmount.of(0L));

            final var events = accountBookEventStorage.getAll(currency);
            assertThat(events).hasSize((int) (SPONSOR_INITIAL_ALLOWANCE + 3));
            assertThat(events.get(0)).isEqualTo(IdentifiedAccountBookEvent.of(1,
                    new AccountBookAggregate.MintEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                            PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            assertThat(events.get(1)).isEqualTo(IdentifiedAccountBookEvent.of(2,
                    new AccountBookAggregate.TransferEvent(AccountBook.AccountId.of(sponsorAccount.id()),
                            AccountBook.AccountId.of(programId), PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            assertThat(events.get(2)).isEqualTo(IdentifiedAccountBookEvent.of(3,
                    new AccountBookAggregate.TransferEvent(AccountBook.AccountId.of(programId),
                            AccountBook.AccountId.of(projectId), PositiveAmount.of(SPONSOR_INITIAL_ALLOWANCE))));
            for (int i = 2; i <= SPONSOR_INITIAL_ALLOWANCE + 1; i++) {
                assertThat(events.get(i + 1).id()).isEqualTo(i + 2);
                assertThat(events.get(i + 1).data()).isInstanceOf(AccountBookAggregate.TransferEvent.class);
                final var event = (AccountBookAggregate.TransferEvent) events.get(i + 1).data();
                assertThat(event.from()).isEqualTo(AccountBook.AccountId.of(projectId));
                assertThat(event.to()).isNotNull();
                assertThat(event.amount()).isEqualTo(PositiveAmount.of(1L));

                assertThat(accountBookProviders.get(0).get(currency).state()
                        .balanceOf(event.to()))
                        .isEqualTo(PositiveAmount.of(1L));
            }
        }
    }


}
