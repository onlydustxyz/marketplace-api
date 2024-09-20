package onlydust.com.marketplace.accounting.domain;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.accounting.domain.service.AccountingService;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import onlydust.com.marketplace.accounting.domain.stubs.AccountBookEventStorageStub;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import onlydust.com.marketplace.accounting.domain.stubs.ERC20Tokens;
import onlydust.com.marketplace.accounting.domain.stubs.SponsorAccountStorageStub;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.accounting.domain.view.UserView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.Transaction.Type;
import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.Transaction.Type.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class AccountingServiceTest {
    final SponsorAccountStorageStub sponsorAccountStorage = new SponsorAccountStorageStub();
    final CurrencyStorage currencyStorage = mock(CurrencyStorage.class);
    final AccountingObserverPort accountingObserver = mock(AccountingObserverPort.class);
    final ProjectAccountingObserver projectAccountingObserver = mock(ProjectAccountingObserver.class);
    final InvoiceStoragePort invoiceStoragePort = mock(InvoiceStoragePort.class);
    AccountBookObserver accountBookObserver = mock(AccountBookObserver.class);
    final RewardStatusFacadePort rewardStatusFacadePort = mock(RewardStatusFacadePort.class);
    final ReceiptStoragePort receiptStoragePort = mock(ReceiptStoragePort.class);
    AccountBookEventStorageStub accountBookEventStorage;
    final BlockchainFacadePort blockchainFacadePort = mock(BlockchainFacadePort.class);
    final DepositStoragePort depositStoragePort = mock(DepositStoragePort.class);
    final TransactionStoragePort transactionStoragePort = mock(TransactionStoragePort.class);
    final PermissionPort permissionPort = mock(PermissionPort.class);
    final OnlyDustWallets onlyDustWallets = mock(OnlyDustWallets.class);
    final DepositObserverPort depositObserverPort = mock(DepositObserverPort.class);

    final AccountingSponsorStoragePort accountingSponsorStoragePort = mock(AccountingSponsorStoragePort.class);
    AccountingService accountingService;
    final Faker faker = new Faker();
    final String thirdPartyName = faker.name().fullName();
    final String thirdPartyAccountNumber = "0x" + faker.random().hex(10);
    final IndividualBillingProfile billingProfile = IndividualBillingProfile.builder()
            .id(BillingProfile.Id.random())
            .kyc(Kyc.builder()
                    .id(UUID.randomUUID())
                    .ownerId(UserId.random())
                    .status(VerificationStatus.VERIFIED)
                    .country(Country.fromIso3("FRA"))
                    .firstName(thirdPartyName)
                    .address(faker.address().fullAddress())
                    .consideredUsPersonQuestionnaire(false)
                    .idDocumentCountry(Country.fromIso3("FRA"))
                    .build())
            .name("OnlyDust")
            .enabled(true)
            .status(VerificationStatus.VERIFIED)
            .owner(new BillingProfile.User(UserId.random(), BillingProfile.User.Role.ADMIN, ZonedDateTime.now()))
            .build();

    final PayoutInfo payoutInfo = PayoutInfo.builder()
            .ethWallet(Ethereum.wallet(thirdPartyAccountNumber))
            .optimismAddress(Optimism.accountAddress(thirdPartyAccountNumber))
            .starknetAddress(StarkNet.accountAddress(thirdPartyAccountNumber))
            .build();

    final Invoice.BillingProfileSnapshot billingProfileSnapshot = Invoice.BillingProfileSnapshot.of(billingProfile, payoutInfo);
    final InvoiceView invoiceView = new InvoiceView(Invoice.Id.random(),
            billingProfileSnapshot,
            new UserView(faker.number().randomNumber(), faker.name().username(), URI.create(faker.internet().avatar()),
                    faker.internet().emailAddress(), UserId.random(), faker.name().firstName()),
            ZonedDateTime.now(),
            Money.of(100L, Currencies.USDC),
            ZonedDateTime.now().plusDays(30),
            Invoice.Number.of(1),
            Invoice.Status.APPROVED,
            List.of(), null, null, null);

    final Invoice invoice = Invoice.of(billingProfile, 1, invoiceView.createdBy().id(), payoutInfo)
            .status(invoiceView.status())
            .rewards(List.of());

    private Payment.Reference fakePaymentReference(Network network) {
        return new Payment.Reference(ZonedDateTime.now(), network, faker.random().hex(), thirdPartyName, thirdPartyAccountNumber);
    }

    private Transaction fakeTransaction(Network network, Amount amount) {
        return amount.isPositive() ?
                new Transaction(Transaction.Type.DEPOSIT, fakePaymentReference(network), PositiveAmount.of(amount)) :
                new Transaction(Transaction.Type.WITHDRAW, fakePaymentReference(network), PositiveAmount.of(amount.negate()));
    }

    private void assertOnRewardCreated(RewardId rewardId, boolean isFunded, ZonedDateTime unlockDate, Set<Network> networks) {
        final var accountBookFacadeCaptor = ArgumentCaptor.forClass(AccountBookFacade.class);
        verify(accountingObserver).onRewardCreated(eq(rewardId), accountBookFacadeCaptor.capture());
        final var accountBookFacade = accountBookFacadeCaptor.getValue();
        assertThat(accountBookFacade.isFunded(rewardId)).isEqualTo(isFunded);
        if (unlockDate == null)
            assertThat(accountBookFacade.unlockDateOf(rewardId)).isEmpty();
        else {
            assertThat(accountBookFacade.unlockDateOf(rewardId)).isPresent();
            assertThat(accountBookFacade.unlockDateOf(rewardId).orElseThrow()).isEqualTo(unlockDate.toInstant());
        }
        assertThat(accountBookFacade.networksOf(rewardId)).isEqualTo(networks);
        reset(accountingObserver);
        verify(rewardStatusFacadePort).create(accountBookFacade, rewardId);
        reset(rewardStatusFacadePort);
    }

    private void setupAccountingService() {
        accountBookEventStorage = new AccountBookEventStorageStub();
        accountingService = new AccountingService(new CachedAccountBookProvider(accountBookEventStorage, mock(AccountBookStorage.class), accountBookObserver),
                sponsorAccountStorage,
                currencyStorage,
                accountingObserver,
                projectAccountingObserver,
                invoiceStoragePort,
                rewardStatusFacadePort,
                receiptStoragePort,
                blockchainFacadePort,
                depositStoragePort,
                transactionStoragePort,
                permissionPort,
                onlyDustWallets,
                depositObserverPort,
                accountingSponsorStoragePort);
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
        when(invoiceStoragePort.invoiceViewOf(any())).thenReturn(Optional.of(invoiceView));
    }

    @Nested
    class GivenAnUnknownCurrency {
        final Currency currency = Currencies.USDC;
        final Currency.Id currencyId = currency.id();
        final SponsorId sponsorId = SponsorId.random();
        final SponsorAccount.Id sponsorAccountId = SponsorAccount.Id.random();
        final SponsorAccount sponsorAccount = new SponsorAccount(sponsorAccountId, sponsorId, currency, null);
        final ProgramId programId = ProgramId.random();

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currencyId)).thenReturn(Optional.empty());
            sponsorAccountStorage.save(sponsorAccount);
        }

        /*
         * Given a sponsor account
         * When I allocate money to a project in an unknown currency
         * Then The allocation is rejected
         */
        @Test
        void should_reject_allocation() {
            // When
            assertThatThrownBy(() -> accountingService.allocate(sponsorId, programId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a sponsor that has allocated money to a project
         * When I refund money from the project in an unknown currency
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(programId, sponsorId, PositiveAmount.of(10L), currencyId))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessage("Currency %s not found".formatted(currencyId));

            assertThat(accountBookEventStorage.events).isEmpty();
        }
    }

    @Nested
    class GivenNoSponsorAccount {
        final Currency currency = Currencies.USDC;
        final SponsorId sponsorId = SponsorId.random();
        final ProgramId programId = ProgramId.random();

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it
         * Then A new sponsor account is created and the mint is registered
         */
        @Test
        void should_create_sponsor_account_and_mint_virtual_balance() {
            // Given
            final var amountToMint = PositiveAmount.of(10L);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, amountToMint);

            // Then
            final var event = IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), amountToMint));
            assertThat(accountBookEventStorage.events.get(currency)).contains(event);
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccount.account().currency()).isEqualTo(currency);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().network()).isEmpty();
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();
            assertThat(sponsorAccount.allowance()).isEqualTo(amountToMint);

            verify(accountBookObserver).on(any(), any(),
                    eq(new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsorAccount.account().id())), amountToMint)));
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it and provide a receipt
         * Then A new sponsor account is created, the mint is registered and the physical balance is updated
         */
        @Test
        void should_create_sponsor_account_and_funds_it() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = fakeTransaction(Network.ETHEREUM, amount);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), null, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            final var event = IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(amount)));
            assertThat(accountBookEventStorage.events.get(currency)).contains(event);
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.of(10L));
            assertThat(sponsorAccount.account().currency()).isEqualTo(currency);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().network()).contains(transaction.network());
            assertThat(sponsorAccount.account().getTransactions()).containsExactly(transaction);
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();

            final var savedAccount = sponsorAccountStorage.get(sponsorAccount.account().id()).orElseThrow();
            assertThat(savedAccount.id()).isEqualTo(sponsorAccount.account().id());
            assertThat(savedAccount.unlockedBalance()).isEqualTo(sponsorAccount.account().unlockedBalance());
            assertThat(savedAccount.currency()).isEqualTo(sponsorAccount.account().currency());
            assertThat(savedAccount.sponsorId()).isEqualTo(sponsorAccount.account().sponsorId());
            assertThat(savedAccount.network()).isEqualTo(sponsorAccount.account().network());
            assertThat(savedAccount.getTransactions()).isEqualTo(sponsorAccount.account().getTransactions());
            assertThat(savedAccount.lockedUntil()).isEqualTo(sponsorAccount.account().lockedUntil());

            verify(accountBookObserver).on(any(), any(),
                    eq(new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsorAccount.account().id())), amount)));
        }

        /*
         * Given a newly created sponsor
         * When I create a locked sponsor account
         * Then The balance is always ZERO if the lock date is in the future
         */
        @Test
        void should_create_locked_sponsor_account_and_funds_it() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = fakeTransaction(Network.ETHEREUM, amount);
            final var lockedUntil = ZonedDateTime.now().plusDays(1);

            // When
            final var sponsorAccount = accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), lockedUntil, transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccount);

            // Then
            assertThat(accountBookEventStorage.events.get(currency))
                    .contains(IdentifiedAccountBookEvent.of(1, new MintEvent(AccountId.of(sponsorAccount.account().id()), amount)));
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccount.account().lockedUntil()).contains(lockedUntil.toInstant());

            final var savedAccount = sponsorAccountStorage.get(sponsorAccount.account().id()).orElseThrow();
            assertThat(savedAccount.unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(savedAccount.lockedUntil()).isEqualTo(sponsorAccount.account().lockedUntil());
        }

        /*
         * Given a sponsor with no account
         * When I refund money from a project
         * Then The refund is rejected
         */
        @Test
        void should_reject_unallocation_when_no_sponsor_account_found() {
            // When
            SponsorId otherSponsorId = SponsorId.random();
            assertThatThrownBy(() -> accountingService.unallocate(programId, otherSponsorId, PositiveAmount.of(10L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough funds to refund");

            assertThat(accountBookEventStorage.events).isEmpty();
        }

        /*
         * Given a newly created sponsor
         * When I allocate money on it and provide a receipt on a network that is not supported for the currency
         * Then The request is rejected
         */
        @Test
        void should_reject_funding_on_unsupported_currency() {
            // Given
            final var amount = PositiveAmount.of(10L);
            final var transaction = fakeTransaction(Network.STARKNET, amount);

            // When
            assertThatThrownBy(() -> accountingService.createSponsorAccountWithInitialBalance(sponsorId, currency.id(), null, transaction))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Currency USDC is not supported on network STARKNET");
        }
    }

    @Nested
    class GivenASponsorWithMultipleAccounts {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final ProgramId programId = ProgramId.random();
        SponsorAccount sponsorAccount1;
        SponsorAccount sponsorAccount2;
        SponsorAccount sponsorAccount3;

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            sponsorAccount1 = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, PositiveAmount.of(100L)).account();
            sponsorAccount2 = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, PositiveAmount.of(100L)).account();
            sponsorAccount3 = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, PositiveAmount.of(100L)).account();
            sponsorAccountStorage.save(sponsorAccount1, sponsorAccount2, sponsorAccount3);
            reset(accountBookObserver);
        }

        @Test
        void should_register_allocation_from_a_single_account_to_program() {
            // Given
            final var amount = PositiveAmount.of(20L);

            // When
            accountingService.allocate(sponsorId, programId, amount, currency.id());
            accountingService.unallocate(programId, sponsorId, amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(sponsorAccount1.id()), AccountId.of(programId), amount)),
                    IdentifiedAccountBookEvent.of(5, new RefundEvent(AccountId.of(programId), AccountId.of(sponsorAccount1.id()), amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);

            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount1.id()), AccountId.of(programId)), amount),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount1.id()), AccountId.of(programId)), amount)
            );
        }

        @Test
        void should_register_allocation_from_multiple_accounts_to_program() {
            // Given
            final var amount = PositiveAmount.of(250L);

            // When
            accountingService.allocate(sponsorId, programId, amount, currency.id());
            accountingService.unallocate(programId, sponsorId, amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(sponsorAccount1.id()), AccountId.of(programId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(5, new TransferEvent(AccountId.of(sponsorAccount2.id()), AccountId.of(programId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(6, new TransferEvent(AccountId.of(sponsorAccount3.id()), AccountId.of(programId), PositiveAmount.of(50L))),
                    IdentifiedAccountBookEvent.of(7, new RefundEvent(AccountId.of(programId), AccountId.of(sponsorAccount3.id()), PositiveAmount.of(50L))),
                    IdentifiedAccountBookEvent.of(8, new RefundEvent(AccountId.of(programId), AccountId.of(sponsorAccount2.id()), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(9, new RefundEvent(AccountId.of(programId), AccountId.of(sponsorAccount1.id()), PositiveAmount.of(100L)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);

            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount1.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount2.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount3.id()), AccountId.of(programId)), PositiveAmount.of(50L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount3.id()), AccountId.of(programId)), PositiveAmount.of(50L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount2.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount1.id()), AccountId.of(programId)), PositiveAmount.of(100L))
            );
        }

        @Test
        void should_not_register_allocation_from_multiple_accounts_to_program_when_not_enough_funds() {
            // Given
            final var amount = PositiveAmount.of(350L);

            // When
            assertThatThrownBy(() -> accountingService.allocate(sponsorId, programId, amount, currency.id())).isInstanceOf(OnlyDustException.class);
        }
    }

    @Nested
    class GivenASponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final ProgramId programId = ProgramId.random();
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), null, PositiveAmount.of(100L)).account();
            sponsorAccountStorage.save(sponsorAccount);
            reset(accountBookObserver);
        }

        /*
         * Given a sponsor with an account
         * When I allocate money to for the sponsor
         * Then The account allowance is updated
         */
        @Test
        void should_register_allowance() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));
            SponsorAccount.Id sponsorAccountId;
            // When
            {
                final var sponsorAccountStatement = accountingService.increaseAllowance(sponsorAccount.id(), amount);
                sponsorAccountId = sponsorAccountStatement.account().id();
                assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(100L).add(amount));
            }
            {
                final var sponsorAccountStatement = accountingService.increaseAllowance(sponsorAccount.id(), amount.negate());
                assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(100L));
            }

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(sponsorAccount.id()), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(sponsorAccount.id()), AccountId.ROOT, amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            verify(accountBookObserver).on(any(), any(),
                    eq(new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsorAccountId)), amount)));
            verify(accountBookObserver).on(any(), any(),
                    eq(new AccountBook.Transaction(Type.REFUND, List.of(AccountId.of(sponsorAccountId)), amount)));
        }

        /*
         * Given a sponsor account
         * When I refund money from OnlyDust of more than I sent
         * Then The refund is rejected
         */
        @Test
        void should_reject_refund_when_not_enough_received() {
            // When
            assertThatThrownBy(() -> accountingService.increaseAllowance(sponsorAccount.id(), Amount.of(-110L)))
                    // Then
                    .isInstanceOf(OnlyDustException.class).hasMessageContaining("Cannot refund 110");
        }

        /*
         * Given a sponsor account
         * When I allocate money to a project
         * Then The transfer is registered from my account to the project
         */
        @Test
        void should_register_allocations_to_program() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.allocate(sponsorId, programId, amount, currency.id());
            accountingService.unallocate(programId, sponsorId, amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(programId), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(programId), AccountId.of(sponsorAccount.id()), amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);

            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), amount),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), amount)
            );
        }

        /*
         * Given a sponsor account
         * When I refund money from a project
         * Then The refund is rejected if the sponsor has not allocated enough money
         */
        @Test
        void should_reject_unallocation_when_not_enough_allocated() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(programId, sponsorId, PositiveAmount.of(400L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough funds to refund");
        }

        /*
         * Given a sponsor, a project and a contributor with a sponsor account
         * When the contributor is rewarded by the project but the sponsor is not funded (no real money received)
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // When
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(10L), currency.id());
            accountingService.grant(programId, projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            assertOnRewardCreated(rewardId1, false, null, Set.of());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, ZonedDateTime.now(), network, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor account
         * When I fund money from a project without minting the corresponding amount
         * Then I can withdraw this money
         */
        @Test
        void should_fund_and_withdraw() {
            final var amount = Amount.of(faker.number().randomNumber());

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount));

            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(amount);

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount.negate()));

            // Then
            verify(accountingObserver, times(2)).onSponsorAccountBalanceChanged(any());
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor account
         * When I register a transaction
         * Then I can delete it
         */
        @Test
        void should_fund_and_remove_transaction() {
            final var amount = Amount.of(faker.number().randomNumber());
            final var transaction = fakeTransaction(network, amount);

            // When
            accountingService.fund(sponsorAccount.id(), transaction);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(amount);

            // When
            reset(accountingObserver);
            accountingService.delete(sponsorAccount.id(), transaction.id());
            // Then
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account in multiple times
         *    - project 1 rewards the contributor with the full amount
         * Then, the contributor can withdraw his money
         */
        @Test
        void should_allow_multiple_times_funding() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));
            verify(accountingObserver, times(3)).onSponsorAccountBalanceChanged(any());

            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, true, null, Set.of(network));

            final var payment = accountingService.pay(rewardId2, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789");

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(programId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(programId), AccountId.of(projectId2), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2)),
                            PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2),
                            AccountId.of(rewardId2)),
                            PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2),
                            AccountId.of(rewardId2),
                            AccountId.of(payment.id())), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2),
                            AccountId.of(rewardId2),
                            AccountId.of(payment.id())), PositiveAmount.of(100L))
            );

            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account partially
         *    - project 1 rewards the contributor several times
         * Then, the contributor cannot withdraw his money beyond funding
         */
        @Test
        void should_reject_withdraw_more_than_funded() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(50L)));

            accountingService.allocate(sponsorId, programId, PositiveAmount.of(80L), currency.id());
            accountingService.grant(programId, projectId2, PositiveAmount.of(80L), currency.id());
            accountingService.createReward(projectId2, rewardId1, PositiveAmount.of(40L), currency.id());
            assertOnRewardCreated(rewardId1, true, null, Set.of(network));

            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(40L), currency.id());
            assertOnRewardCreated(rewardId2, true, null, Set.of(network));

            {
                final var account = accountingService.getSponsorAccountStatement(sponsorAccount.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(80L));
            }
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();

            accountingService.pay(rewardId1, ZonedDateTime.now(), network, "0x123456789");

            {
                final var account = accountingService.getSponsorAccountStatement(sponsorAccount.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(40L));
                assertThat(account.account().balance()).isEqualTo(Amount.of(10L));
                assertThat(account.allowance()).isEqualTo(PositiveAmount.of(20L));
            }

            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, ZonedDateTime.now(), network, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId2));
        }

        /*
         * Given a sponsor account
         * When I fund it on a given network
         * Then I cannot fund it on a different network
         */
        @Test
        void should_forbid_different_networks_on_same_account() {
            // Given
            accountingService.fund(sponsorAccount.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            // When
            assertThatThrownBy(() -> accountingService.fund(sponsorAccount.id(), fakeTransaction(Network.STARKNET, PositiveAmount.of(100L))))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Cannot mix transactions from different networks");
        }

        @Test
        void should_cancel_a_reward() {
            // Given
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId1, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(40L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId1, currency.id(), PositiveAmount.of(60L), PositiveAmount.of(100L));
            reset(projectAccountingObserver);
            when(invoiceStoragePort.invoiceOf(rewardId1)).thenReturn(Optional.of(invoice.status(Invoice.Status.REJECTED)));
            when(invoiceStoragePort.invoiceViewOf(rewardId1)).thenReturn(Optional.of(invoiceView.toBuilder().status(Invoice.Status.REJECTED).build()));

            // When
            accountingService.cancel(rewardId1, currency.id());

            // Then
            verify(projectAccountingObserver).onAllowanceUpdated(projectId1, currency.id(), PositiveAmount.of(100L), PositiveAmount.of(100L));
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(programId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(programId), AccountId.of(projectId1), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(projectId1), AccountId.of(rewardId1), PositiveAmount.of(40L))),
                    IdentifiedAccountBookEvent.of(5, new FullRefundEvent(AccountId.of(rewardId1)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId1)),
                            PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId1),
                            AccountId.of(rewardId1)),
                            PositiveAmount.of(40L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId1),
                            AccountId.of(rewardId1)),
                            PositiveAmount.of(40L))
            );
            verify(rewardStatusFacadePort).delete(rewardId1);
        }

        @Test
        void should_prevent_a_reward_from_being_payable_if_already_paid() {
            // Given
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(100L)));
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId1, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(40L), currency.id());
            when(currencyStorage.all()).thenReturn(Set.of(currency));

            // When
            final var payments = accountingService.pay(Set.of(rewardId1));

            // Then
            assertThat(payments).hasSize(1);
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThat(accountingService.getPayableRewards(Set.of(rewardId1))).isEmpty();
            assertThatThrownBy(() -> accountingService.cancel(rewardId1, currency.id()))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s cannot be cancelled because it is included in an invoice".formatted(rewardId1));
            assertThatThrownBy(() -> accountingService.pay(rewardId1, ZonedDateTime.now(), network, "0x123456789"))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Reward %s is not payable".formatted(rewardId1));

            // When
            accountingService.cancel(payments.get(0));

            // Then
            assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
            assertThat(accountingService.getPayableRewards(Set.of(rewardId1))).hasSize(1);
            verify(rewardStatusFacadePort, never()).delete(rewardId1);
        }
    }

    @Nested
    class GivenALockedSponsorAccount {
        final Currency currency = Currencies.USDC;
        final Network network = Network.ETHEREUM;
        final SponsorId sponsorId = SponsorId.random();
        final ProgramId programId = ProgramId.random();
        final ProjectId projectId1 = ProjectId.random();
        final ProjectId projectId2 = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);
        SponsorAccount sponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            sponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, currency.id(), unlockDate, PositiveAmount.of(300L))
                    .account();
            sponsorAccountStorage.save(sponsorAccount);
            reset(accountBookObserver);
        }

        /*
         * Given a sponsor with a locked account
         * When I allocate money to for the sponsor
         * Then The account allowance is updated
         */
        @Test
        void should_register_allowance() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.increaseAllowance(sponsorAccount.id(), amount);
            accountingService.increaseAllowance(sponsorAccount.id(), amount.negate());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new MintEvent(AccountId.of(sponsorAccount.id()), amount)),
                    IdentifiedAccountBookEvent.of(3, new RefundEvent(AccountId.of(sponsorAccount.id()), AccountId.ROOT, amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsorAccount.id())), amount),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount.id())), amount)
            );
        }

        /*
         * Given a sponsor with a locked account
         * When I allocate money to a project
         * Then The transfer is registered from my account to the project
         */
        @Test
        void should_register_allocations_to_project() {
            // Given
            final var amount = PositiveAmount.of(faker.number().numberBetween(1L, 100L));

            // When
            accountingService.allocate(sponsorId, programId, amount, currency.id());
            accountingService.grant(programId, projectId1, amount, currency.id());
            accountingService.ungrant(projectId1, programId, amount, currency.id());

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(programId), amount)),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(programId), AccountId.of(projectId1), amount)),
                    IdentifiedAccountBookEvent.of(4, new RefundEvent(AccountId.of(projectId1), AccountId.of(programId), amount))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), amount),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId1)),
                            amount),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId1)), amount)
            );
            verify(accountingObserver).onFundsAllocatedToProgram(sponsorId, programId, amount, currency.id());
            verify(accountingObserver).onFundsRefundedByProject(projectId1, programId, amount, currency.id());
        }

        /*
         * Given a sponsor with a locked account
         * When I refund money from a project
         * Then The refund is rejected if the sponsor has not allocated enough money
         */
        @Test
        void should_reject_unallocation_when_not_enough_allocated() {
            // When
            assertThatThrownBy(() -> accountingService.unallocate(programId, sponsorId, PositiveAmount.of(400L), currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessageContaining("Not enough funds to refund");
        }

        /*
         * Given a sponsor, a project and a contributor with a locked account
         * When the contributor is rewarded by the project but the sponsor is not funded (no real money received)
         * Then The contributor cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // When
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(10L), currency.id());
            accountingService.grant(programId, projectId1, PositiveAmount.of(10L), currency.id());
            accountingService.createReward(projectId1, rewardId1, PositiveAmount.of(10L), currency.id());
            assertOnRewardCreated(rewardId1, false, unlockDate, Set.of());

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, ZonedDateTime.now(), network, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account in multiple times
         *    - project 1 rewards the contributor with the full amount
         * Then, the contributor can withdraw his money
         */
        @Test
        void should_allow_multiple_times_funding() {
            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(30L)));
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, PositiveAmount.of(40L)));

            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId2, PositiveAmount.of(100L), currency.id());
            accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, true, unlockDate, Set.of(network));

            // Then
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(2, new TransferEvent(AccountId.of(sponsorAccount.id()), AccountId.of(programId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(3, new TransferEvent(AccountId.of(programId), AccountId.of(projectId2), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(4, new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(100L)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2)),
                            PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsorAccount.id()), AccountId.of(programId), AccountId.of(projectId2),
                            AccountId.of(rewardId2)),
                            PositiveAmount.of(100L))
            );

            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given a sponsor, a project and a contributor
         * When
         *    - the sponsor funds its account partially
         *    - project 1 rewards the contributor several times
         * Then, the contributor cannot withdraw his money beyond funding
         */
        @Test
        void should_reject_withdraw() {
            // Given
            final var amount = PositiveAmount.of(faker.number().randomNumber());

            // When
            accountingService.fund(sponsorAccount.id(), fakeTransaction(network, amount));
            accountingService.increaseAllowance(sponsorAccount.id(), amount);

            accountingService.allocate(sponsorId, programId, amount, currency.id());
            accountingService.grant(programId, projectId1, amount, currency.id());
            accountingService.createReward(projectId1, rewardId1, amount, currency.id());
            assertOnRewardCreated(rewardId1, true, unlockDate, Set.of(network));

            assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId1, ZonedDateTime.now(), network, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId1));
        }

        /*
         * Given a sponsor account
         * When I update the unlock date
         * Then the unlock date is updated
         */
        @Test
        void should_update_unlock_date() {
            // Given
            final var unlockDate = ZonedDateTime.now().plusDays(2);

            // When
            final var newSponsorAccount = accountingService.updateSponsorAccount(sponsorAccount.id(), unlockDate);
            verify(accountingObserver).onSponsorAccountUpdated(newSponsorAccount);

            // Then
            assertThat(newSponsorAccount.account().lockedUntil()).contains(unlockDate.toInstant());
            assertThat(sponsorAccountStorage.get(sponsorAccount.id()).orElseThrow().lockedUntil()).contains(unlockDate.toInstant());
        }
    }

    @Nested
    class GivenSeveralSponsors {
        final SponsorId unlockedSponsorId1 = SponsorId.random();
        final SponsorId unlockedSponsorId2 = SponsorId.random();
        final SponsorId lockedSponsorId = SponsorId.random();
        final ZonedDateTime unlockDate = ZonedDateTime.now().plusDays(1);
        final ProgramId programId = ProgramId.random();
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        Currency currency;
        SponsorAccount unlockedSponsorSponsorAccount1;
        SponsorAccount unlockedSponsorSponsorAccount2;
        SponsorAccount lockedSponsorSponsorAccount;

        @BeforeEach
        void setup() {
            setupAccountingService();
            currency = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
            currency.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
            when(currencyStorage.all()).thenReturn(Set.of(currency));
            unlockedSponsorSponsorAccount1 = accountingService.createSponsorAccountWithInitialAllowance(unlockedSponsorId1, currency.id(), null,
                    PositiveAmount.of(100L)).account();
            unlockedSponsorSponsorAccount2 = accountingService.createSponsorAccountWithInitialAllowance(unlockedSponsorId2, currency.id(), null,
                    PositiveAmount.of(100L)).account();
            lockedSponsorSponsorAccount = accountingService.createSponsorAccountWithInitialAllowance(lockedSponsorId, currency.id(), unlockDate,
                    PositiveAmount.of(100L)).account();
            sponsorAccountStorage.save(unlockedSponsorSponsorAccount1, unlockedSponsorSponsorAccount2, lockedSponsorSponsorAccount);
            reset(accountBookObserver);
        }

        /*
         * Given 2 sponsor accounts
         * When Only the first account is funded
         * Then The contributor paid by the other account cannot withdraw his money
         */
        @Test
        void should_prevent_contributor_from_withdrawing_if_source_is_not_funded() {
            // Given
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(unlockedSponsorId2, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            assertOnRewardCreated(rewardId, false, null, Set.of());

            // When
            final var account = accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));
            assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(100L));

            assertThat(accountingService.isPayable(rewardId, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId));
        }

        /*
         * Given 2 sponsor accounts that rewarded 2 contributors via the same project
         * When Only sponsor account 1 funds its account
         * Then Only the first contributor can withdraw its reward
         */
        @Test
        void should_allow_contributor_to_withdraw_only_what_is_funded() {
            // Given
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(unlockedSponsorId2, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId, false, null, Set.of());

            accountingService.createReward(projectId, rewardId2, PositiveAmount.of(100L), currency.id());
            assertOnRewardCreated(rewardId2, false, null, Set.of());

            // When
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

            {
                final var account = accountingService.getSponsorAccountStatement(unlockedSponsorSponsorAccount1.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.of(100L));
            }
            accountingService.pay(rewardId, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789");
            {
                final var account = accountingService.getSponsorAccountStatement(unlockedSponsorSponsorAccount1.id()).orElseThrow();
                assertThat(account.awaitingPaymentAmount()).isEqualTo(PositiveAmount.ZERO);
            }

            // Then
            assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId2, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId2));
        }

        /*
         * Given a locked sponsor account and another unlocked sponsor
         * When A reward is created from both accounts
         * Then The reward is not payable
         */
        @Test
        void should_not_pay_partially_locked_rewards() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));
            accountingService.fund(lockedSponsorSponsorAccount.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(100L), currency.id());
            accountingService.allocate(lockedSponsorId, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            assertOnRewardCreated(rewardId, true, unlockDate, Set.of(Network.ETHEREUM));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isFalse();
            assertThatThrownBy(() -> accountingService.pay(rewardId, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789"))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s is not payable on ETHEREUM".formatted(rewardId));
        }

        /*
         * Given sponsor 1 that funded its account on Ethereum and sponsor 2 that funded its account on Optimism
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw his money on both networks
         */
        @Test
        void should_withdraw_on_both_networks() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorSponsorAccount2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));

            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(200L), currency.id());
            accountingService.allocate(unlockedSponsorId2, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(300L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(300L), currency.id());
            assertOnRewardCreated(rewardId, true, null, Set.of(Network.ETHEREUM, Network.OPTIMISM));

            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(200L));
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();

            accountingService.pay(rewardId, ZonedDateTime.now(), Network.ETHEREUM, "0x123456789");
            accountingService.pay(rewardId, ZonedDateTime.now(), Network.OPTIMISM, "0x123456789");

            // Then
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        /*
         * Given 2 sponsors that funded their account on Ethereum
         * When A contributor is rewarded by the project
         * Then The contributor can withdraw his money from both sponsor accounts
         */
        @Test
        void should_withdraw_on_both_sponsor_accounts() {
            // Given
            accountingService.fund(unlockedSponsorSponsorAccount1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorSponsorAccount2.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100L)));

            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(200L), currency.id());
            accountingService.allocate(unlockedSponsorId2, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(300L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(300L), currency.id());
            assertOnRewardCreated(rewardId, true, null, Set.of(Network.ETHEREUM));

            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(200L));
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.of(100L));

            // When
            assertThat(accountingService.isPayable(rewardId, currency.id())).isTrue();

            reset(accountingObserver);
            final var reference = fakePaymentReference(Network.ETHEREUM);
            accountingService.pay(rewardId, reference.timestamp(), Network.ETHEREUM, reference.reference());
            verify(accountingObserver, times(2)).onSponsorAccountBalanceChanged(any());
            final var capturedReceipt = ArgumentCaptor.forClass(Receipt.class);
            verify(receiptStoragePort).save(capturedReceipt.capture());
            final var savedReceipt = capturedReceipt.getValue();
            assertThat(savedReceipt.rewardId()).isEqualTo(rewardId);
            assertThat(savedReceipt.reference()).isEqualTo(reference.reference());
            assertThat(savedReceipt.network()).isEqualTo(reference.network());
            verify(accountingObserver).onRewardPaid(rewardId);

            // Then
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount1.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
            assertThat(sponsorAccountStorage.get(unlockedSponsorSponsorAccount2.id()).orElseThrow().unlockedBalance()).isEqualTo(Amount.ZERO);
        }

        @Test
        void should_cancel_a_reward() {
            // Given
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount2.id(), PositiveAmount.of(100L));
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(200L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(200L), PositiveAmount.of(200L));
            reset(projectAccountingObserver);

            accountingService.allocate(unlockedSponsorId2, programId, PositiveAmount.of(100L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(100L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(300L), PositiveAmount.of(300L));
            reset(projectAccountingObserver);

            accountingService.createReward(projectId, rewardId, PositiveAmount.of(250L), currency.id());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(50L), PositiveAmount.of(300L));
            reset(projectAccountingObserver);

            when(invoiceStoragePort.invoiceOf(rewardId)).thenReturn(Optional.empty());

            // When
            accountingService.cancel(rewardId, currency.id());

            // Then
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, currency.id(), PositiveAmount.of(300L), PositiveAmount.of(300L));
            final var events = List.of(
                    IdentifiedAccountBookEvent.of(6, new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId),
                            PositiveAmount.of(200L))),
                    IdentifiedAccountBookEvent.of(7, new TransferEvent(AccountId.of(programId), AccountId.of(projectId), PositiveAmount.of(200L))),
                    IdentifiedAccountBookEvent.of(8, new TransferEvent(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(programId),
                            PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(9, new TransferEvent(AccountId.of(programId), AccountId.of(projectId), PositiveAmount.of(100L))),
                    IdentifiedAccountBookEvent.of(10, new TransferEvent(AccountId.of(projectId), AccountId.of(rewardId), PositiveAmount.of(250L))),
                    IdentifiedAccountBookEvent.of(11, new FullRefundEvent(AccountId.of(rewardId)))
            );
            assertThat(accountBookEventStorage.events.get(currency)).containsAll(events);
            final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
            verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
            final var transactions = transactionsCaptor.getAllValues();
            // @formatter:off
            assertThat(transactions).containsExactly(
                    new AccountBook.Transaction(MINT, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id())), PositiveAmount.of(200L)),
                    new AccountBook.Transaction(MINT, List.of(AccountId.of(unlockedSponsorSponsorAccount2.id())), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(programId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(programId), AccountId.of(projectId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(50L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(unlockedSponsorSponsorAccount1.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(100L)),
                    new AccountBook.Transaction(REFUND, List.of(AccountId.of(unlockedSponsorSponsorAccount2.id()), AccountId.of(programId), AccountId.of(projectId), AccountId.of(rewardId)), PositiveAmount.of(50L))
            );
            // @formatter:on
            verify(rewardStatusFacadePort).delete(rewardId);
        }

        @ParameterizedTest
        @EnumSource(value = Invoice.Status.class, names = {"PAID", "APPROVED", "TO_REVIEW"})
        void should_forbid_cancelling_a_reward_linked_to_an_active_invoice(Invoice.Status status) {
            // Given
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(200L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            when(invoiceStoragePort.invoiceOf(rewardId)).thenReturn(Optional.of(invoice.status(status)));
            when(invoiceStoragePort.invoiceViewOf(rewardId)).thenReturn(Optional.of(invoiceView.toBuilder().status(status).build()));

            // When
            assertThatThrownBy(() -> accountingService.cancel(rewardId, currency.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Reward %s cannot be cancelled because it is included in an invoice".formatted(rewardId));

            verify(rewardStatusFacadePort, never()).delete(rewardId);
        }


        @ParameterizedTest
        @EnumSource(value = Invoice.Status.class, names = {"PAID", "APPROVED", "TO_REVIEW"}, mode = EnumSource.Mode.EXCLUDE)
        void should_forbid_cancelling_a_reward_linked_to_an_inactive_invoice(Invoice.Status status) {
            // Given
            accountingService.increaseAllowance(unlockedSponsorSponsorAccount1.id(), PositiveAmount.of(200L));
            accountingService.allocate(unlockedSponsorId1, programId, PositiveAmount.of(200L), currency.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(200L), currency.id());
            accountingService.createReward(projectId, rewardId, PositiveAmount.of(200L), currency.id());
            when(invoiceStoragePort.invoiceOf(rewardId)).thenReturn(Optional.of(invoice.status(status)));
            when(invoiceStoragePort.invoiceViewOf(rewardId)).thenReturn(Optional.of(invoiceView.toBuilder().status(status).build()));

            // When
            accountingService.cancel(rewardId, currency.id());

            // Then
            verify(rewardStatusFacadePort).delete(rewardId);
        }
    }

    @Nested
    class GivenAProjectWithBudget {
        final Currency usdc = Currency.of(ERC20Tokens.ETH_USDC); // build a copy of USDC currency to avoid side effects
        final Currency op = Currencies.OP;
        final SponsorId sponsorId = SponsorId.random();
        final ProgramId programId = ProgramId.random();
        final ProjectId projectId = ProjectId.random();
        final RewardId rewardId1 = RewardId.random();
        final RewardId rewardId2 = RewardId.random();
        final RewardId rewardId3 = RewardId.random();
        final RewardId rewardId4 = RewardId.random();
        final RewardId rewardId5 = RewardId.random();
        final RewardId rewardId6 = RewardId.random();
        SponsorAccount unlockedSponsorAccountUsdc1;
        SponsorAccount unlockedSponsorAccountUsdc2;
        SponsorAccount unlockedSponsorAccountOp;
        SponsorAccount lockedSponsorAccountUsdc;

        @BeforeEach
        void setup() {
            setupAccountingService();
            usdc.erc20().add(ERC20Tokens.OP_USDC);

            when(currencyStorage.get(usdc.id())).thenReturn(Optional.of(usdc));
            when(currencyStorage.get(op.id())).thenReturn(Optional.of(op));
            when(currencyStorage.all()).thenReturn(Set.of(usdc, op));

            final var lockDate = ZonedDateTime.now().plusDays(1);

            unlockedSponsorAccountUsdc1 =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), null, PositiveAmount.of(200L)).account();
            unlockedSponsorAccountUsdc2 =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), null, PositiveAmount.of(100L)).account();
            unlockedSponsorAccountOp = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, op.id(), null, PositiveAmount.of(100L)).account();
            lockedSponsorAccountUsdc =
                    accountingService.createSponsorAccountWithInitialAllowance(sponsorId, usdc.id(), lockDate, PositiveAmount.of(100L)).account();

            accountingService.allocate(sponsorId, programId, PositiveAmount.of(200L), usdc.id());
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), usdc.id());
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), op.id());
            accountingService.allocate(sponsorId, programId, PositiveAmount.of(100L), usdc.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(400L), usdc.id());
            accountingService.grant(programId, projectId, PositiveAmount.of(100L), op.id());

            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(400L), PositiveAmount.of(400L));
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, op.id(), PositiveAmount.of(100L), PositiveAmount.of(100L));

            accountingService.createReward(projectId, rewardId1, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId1, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(325L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId2, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId2, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(250L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId3, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId3, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(175L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId4, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId4, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(100L), PositiveAmount.of(400L));

            accountingService.createReward(projectId, rewardId5, PositiveAmount.of(90L), op.id());
            assertOnRewardCreated(rewardId5, false, null, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, op.id(), PositiveAmount.of(10L), PositiveAmount.of(100L));

            accountingService.createReward(projectId, rewardId6, PositiveAmount.of(75L), usdc.id());
            assertOnRewardCreated(rewardId6, false, lockDate, Set.of());
            verify(projectAccountingObserver).onAllowanceUpdated(projectId, usdc.id(), PositiveAmount.of(25L), PositiveAmount.of(400L));
        }

        /*
         * Given a project with a budget
         * When I reward contributors
         * Then We can list the payable rewards
         */
        @Test
        void should_return_no_payable_reward_if_none_fund() {
            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }

        @Test
        void should_return_payable_rewards_on_one_currency_and_one_network() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot)
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3));
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));
                final var payableRewards2 = accountingService.getPayableRewards(Set.of(rewardId2));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot)
                );
                assertThat(payableRewards1).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot)
                );
                assertThat(payableRewards2).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot)
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(150L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(90L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5));
                final var payableRewards5 = accountingService.getPayableRewards(Set.of(rewardId4, rewardId5));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
                assertThat(payableRewards5).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(200L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(50L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot)
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5));
                final var payableRewards3 = accountingService.getPayableRewards(Set.of(rewardId3));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot)
                );
                assertThat(payableRewards3).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot)
                );
            }
        }

        @Test
        void should_return_payable_rewards_on_multiple_currencies_on_multiple_networks() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(125L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(30L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                // rewardId1 is payable because it is entirely funded on network ETHEREUM
                // rewardId2 is NOT payable because it is NOT entirely funded on network ETHEREUM (we funded 125L but 150L would have been required)
                // rewardId3 is payable because it is entirely funded on networks ETHEREUM and OPTIMISM (50 are coming from the 125 on ETHEREUM and 25 from the
                // 30 on OPTIMISM)
                // rewardId5 is payable because it is entirely funded on network OPTIMISM (for currency OP)
                assertThat(payableRewards).hasSize(4);
                assertThat(payableRewards).containsOnlyOnce(
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
                assertThat(List.of(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot))).containsAll(payableRewards);
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId3));

                // Then
                assertThat(payableRewards).hasSize(3);
                assertThat(payableRewards).containsOnlyOnce(
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot)
                );
                assertThat(List.of(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot))).containsAll(payableRewards);
            }
        }

        @Test
        void should_return_payable_rewards_unless_they_are_locked() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(lockedSponsorAccountUsdc.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(100_000L)));
            accountingService.fund(unlockedSponsorAccountOp.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(100_000L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId4, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId2, rewardId4, rewardId5));

                // Then
                assertThat(payableRewards).containsExactlyInAnyOrder(
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId4, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId5, op.forNetwork(Network.OPTIMISM), PositiveAmount.of(90L), billingProfileSnapshot)
                );
            }
        }

        /*
        We don't want to just return a list of payable-rewards.
        We actually want to return a payable-list of rewards, meaning th whole list can be paid in one go, and in any order.
        Hence, in this test, we want to return only one reward (among rewardId1 and rewardId2), because if we were returning both of them,
        the second one wouldn't be payable anymore once the first one has been paid (there is enough funds to pay one reward, not both).
         */
        @Test
        void should_return_payable_rewards_up_to_sponsor_balance() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).hasSize(1);
                assertThat(List.of(
                        PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot),
                        PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L), billingProfileSnapshot))
                ).contains(payableRewards.get(0));
            }
            {
                // When
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));
                final var payableRewards2 = accountingService.getPayableRewards(Set.of(rewardId2));

                // Then
                assertThat(payableRewards1).containsExactly(PayableReward.of(rewardId1, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L),
                        billingProfileSnapshot));
                assertThat(payableRewards2).containsExactly(PayableReward.of(rewardId2, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(75L),
                        billingProfileSnapshot));
            }
        }

        @Test
        void should_return_partially_paid_payable_rewards() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(50L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            reset(accountingObserver);
            accountingService.fund(unlockedSponsorAccountUsdc2.id(), fakeTransaction(Network.OPTIMISM, PositiveAmount.of(25L)));
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());

            assertThat(accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6))).containsExactlyInAnyOrder(
                    PayableReward.of(rewardId3, usdc.forNetwork(Network.ETHEREUM), PositiveAmount.of(50L), billingProfileSnapshot),
                    PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L), billingProfileSnapshot)
            );

            reset(accountingObserver);
            final var reference = fakePaymentReference(Network.ETHEREUM);
            accountingService.pay(rewardId3, reference.timestamp(), Network.ETHEREUM, reference.reference());
            verify(accountingObserver).onSponsorAccountBalanceChanged(any());
            verify(receiptStoragePort).save(any());
            verify(accountingObserver, never()).onRewardPaid(rewardId3);
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId2, rewardId3, rewardId4, rewardId5, rewardId6));

                // Then
                assertThat(payableRewards).containsExactly(PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L),
                        billingProfileSnapshot));
            }
            {
                // When
                final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1, rewardId3));
                final var payableRewards1 = accountingService.getPayableRewards(Set.of(rewardId1));

                // Then
                assertThat(payableRewards).containsExactly(PayableReward.of(rewardId3, usdc.forNetwork(Network.OPTIMISM), PositiveAmount.of(25L),
                        billingProfileSnapshot));
                assertThat(payableRewards1).isEmpty();
            }
        }

        @Test
        void should_not_return_rewards_with_no_invoice() {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            when(invoiceStoragePort.invoiceOf(rewardId1)).thenReturn(Optional.empty());

            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }

        @ParameterizedTest
        @EnumSource(value = Invoice.Status.class, names = {"DRAFT", "TO_REVIEW", "REJECTED", "PAID"})
        void should_not_return_rewards_with_invoice_not_approved(Invoice.Status status) {
            // Given
            accountingService.fund(unlockedSponsorAccountUsdc1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(75L)));
            when(invoiceStoragePort.invoiceOf(rewardId1)).thenReturn(Optional.of(invoice.status(status)));
            when(invoiceStoragePort.invoiceViewOf(rewardId1)).thenReturn(Optional.of(invoiceView.toBuilder().status(status).build()));

            // When
            final var payableRewards = accountingService.getPayableRewards(Set.of(rewardId1));

            // Then
            assertThat(payableRewards).isEmpty();
        }
    }

    @Nested
    class Deposits {
        final SponsorId sponsorId = SponsorId.random();
        final UserId userId = UserId.random();

        @BeforeEach
        void setup() {
            when(blockchainFacadePort.sanitizedTransactionReference(any(), anyString())).thenAnswer(i -> i.getArgument(1));
            when(permissionPort.isUserSponsorLead(userId, sponsorId)).thenReturn(true);
        }

        @Test
        void should_reject_non_supported_networks() {
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.SEPA, "REF 123465"))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Network SEPA is not associated with a blockchain");
        }

        @Test
        void should_reject_when_user_not_sponsor_lead() {
            when(permissionPort.isUserSponsorLead(userId, sponsorId)).thenReturn(false);

            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.SEPA, "REF 123465"))
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("User %s is not allowed to create deposits for sponsor %s".formatted(userId, sponsorId));
        }

        @Test
        void should_reject_when_transaction_not_found() {
            // Given
            final var transactionReference = faker.crypto().sha256();

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transactionReference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transactionReference))
                    .thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transactionReference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction %s not found on blockchain Ethereum".formatted(transactionReference));
        }

        @Test
        void should_reject_when_transaction_not_a_transfer() {
            // Given
            final var transaction = Transaction.fake();

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction %s is not a transfer transaction".formatted(transaction.reference));
        }

        @ParameterizedTest
        @EnumSource(value = Blockchain.Transaction.Status.class, names = {"CONFIRMED"}, mode = EnumSource.Mode.EXCLUDE)
        void should_reject_when_transaction_not_confirmed(Blockchain.Transaction.Status status) {
            // Given
            final var transaction = TransferTransaction.fakeNative(status);

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction %s is not confirmed on blockchain ETHEREUM".formatted(transaction.reference));
        }

        @Test
        void should_reject_when_transaction_blockchain_is_not_supported() {
            // Given
            final var transaction = TransferTransaction.fakeNative(Blockchain.Transaction.Status.CONFIRMED);

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction's (%s) blockchain (ETHEREUM) is not supported for deposits".formatted(transaction.reference));
        }

        @Test
        void should_reject_when_transaction_recipient_does_not_match_onlydust_wallet() {
            // Given
            final var transaction = TransferTransaction.fakeNative(Blockchain.Transaction.Status.CONFIRMED);

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.of(transaction.recipientAddress + "42"));

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction's (%s) recipient (%s) is not equal to the OnlyDust wallet (%s) expected on blockchain ETHEREUM".formatted(transaction.reference, transaction.recipientAddress, transaction.recipientAddress + "42"));
        }

        @Test
        void should_reject_deposit_for_native_transfer_if_not_supported() {
            // Given
            final var transaction = TransferTransaction.fakeNative();
            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.of(transaction.recipientAddress));

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(currencyStorage.findByCode(Currency.Code.ETH)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Native currency not supported on blockchain Ethereum");
        }

        @Test
        void should_create_deposit_for_native_transfer() {
            // Given
            final var transaction = TransferTransaction.fakeNative();
            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.of(transaction.recipientAddress));

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(currencyStorage.findByCode(Currency.Code.ETH)).thenReturn(Optional.of(Currencies.ETH));

            // When
            final var deposit = accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference);

            // Then
            assertThat(deposit.id()).isNotNull();
            assertThat(deposit.sponsorId()).isEqualTo(sponsorId);
            assertThat(deposit.transaction()).isEqualTo(transaction);
            assertThat(deposit.status()).isEqualTo(Deposit.Status.DRAFT);
            assertThat(deposit.currency()).isEqualTo(Currencies.ETH);
            assertThat(deposit.billingInformation()).isNull();

            verify(depositStoragePort).save(deposit);
        }

        @Test
        void should_reject_deposit_for_erc20_transfer_if_not_supported() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.of(transaction.recipientAddress));

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(currencyStorage.findByErc20(Blockchain.ETHEREUM, transaction.address)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Currency %s not supported on blockchain Ethereum".formatted(transaction.address));
        }

        @Test
        void should_create_deposit_for_erc20_transfer() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            when(onlyDustWallets.get(Blockchain.ETHEREUM)).thenReturn(Optional.of(transaction.recipientAddress));

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(false);

            when(blockchainFacadePort.getTransaction(Blockchain.ETHEREUM, transaction.reference))
                    .thenReturn(Optional.of(transaction));

            when(currencyStorage.findByErc20(Blockchain.ETHEREUM, transaction.address))
                    .thenReturn(Optional.of(Currencies.USDC));

            // When
            final var deposit = accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference);

            // Then
            assertThat(deposit.id()).isNotNull();
            assertThat(deposit.sponsorId()).isEqualTo(sponsorId);
            assertThat(deposit.transaction()).isEqualTo(transaction);
            assertThat(deposit.status()).isEqualTo(Deposit.Status.DRAFT);
            assertThat(deposit.currency()).isEqualTo(Currencies.USDC);
            assertThat(deposit.billingInformation()).isNull();

            verify(depositStoragePort).save(deposit);
        }

        @Test
        void should_get_existing_deposit_for_transactionReference() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var existingDeposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(true);
            when(depositStoragePort.findByTransactionReference(transaction.reference)).thenReturn(Optional.of(existingDeposit));

            // When
            final var deposit = accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference);

            // Then
            assertThat(deposit).isEqualTo(existingDeposit);
            verify(depositStoragePort, never()).save(deposit);
        }

        @Test
        void should_throw_when_transaction_already_exists_and_not_linked_to_a_deposit() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(true);
            when(depositStoragePort.findByTransactionReference(transaction.reference)).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction %s already exists".formatted(transaction.reference));

            // Then
            verify(depositStoragePort, never()).save(any());
        }

        @Test
        void should_throw_when_transaction_already_exists_and_is_linked_to_a_non_draft_deposit() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var existingDeposit = Deposit.preview(sponsorId, transaction, Currencies.USDC).toBuilder().status(Deposit.Status.PENDING).build();

            when(transactionStoragePort.exists(Blockchain.ETHEREUM, transaction.reference)).thenReturn(true);
            when(depositStoragePort.findByTransactionReference(transaction.reference)).thenReturn(Optional.of(existingDeposit));

            // When
            assertThatThrownBy(() -> accountingService.previewDeposit(userId, sponsorId, Network.ETHEREUM, transaction.reference))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Transaction %s already exists".formatted(transaction.reference));

            // Then
            verify(depositStoragePort, never()).save(any());
        }

        @Test
        void should_prevent_rejecting_deposit_if_not_found() {
            // Given
            final var depositId = Deposit.Id.random();
            when(depositStoragePort.find(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.rejectDeposit(depositId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Deposit %s not found".formatted(depositId));
        }

        @ParameterizedTest
        @EnumSource(value = Deposit.Status.class, names = {"PENDING"}, mode = EnumSource.Mode.EXCLUDE)
        void should_prevent_rejecting_deposit_if_not_pending(Deposit.Status status) {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);

            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(status).build()));

            // When
            assertThatThrownBy(() -> accountingService.rejectDeposit(deposit.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Deposit %s is not pending".formatted(deposit.id()));
        }

        @Test
        void should_reject_deposit() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);
            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(Deposit.Status.PENDING).build()));

            // When
            accountingService.rejectDeposit(deposit.id());

            // Then
            verify(depositStoragePort).save(deposit.toBuilder().status(Deposit.Status.REJECTED).build());
        }

        @Test
        void should_prevent_approving_deposit_if_not_found() {
            // Given
            final var depositId = Deposit.Id.random();
            when(depositStoragePort.find(any())).thenReturn(Optional.empty());

            // When
            assertThatThrownBy(() -> accountingService.approveDeposit(depositId))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Deposit %s not found".formatted(depositId));
        }

        @ParameterizedTest
        @EnumSource(value = Deposit.Status.class, names = {"PENDING"}, mode = EnumSource.Mode.EXCLUDE)
        void should_prevent_approving_deposit_if_not_pending(Deposit.Status status) {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);

            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(status).build()));

            // When
            assertThatThrownBy(() -> accountingService.approveDeposit(deposit.id()))
                    // Then
                    .isInstanceOf(OnlyDustException.class)
                    .hasMessage("Deposit %s is not pending".formatted(deposit.id()));
        }

        @Test
        void should_approve_deposit() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);
            final var sponsor = SponsorView.builder().id(sponsorId).name("Sponsor").logoUrl(URI.create(faker.internet().url())).build();
            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(Deposit.Status.PENDING).build()));
            when(accountingSponsorStoragePort.getView(sponsorId)).thenReturn(Optional.of(sponsor));
            when(currencyStorage.get(Currencies.USDC.id())).thenReturn(Optional.of(Currencies.USDC));

            // When
            accountingService.approveDeposit(deposit.id());

            // Then
            verify(depositStoragePort).save(deposit.toBuilder().status(Deposit.Status.COMPLETED).build());

            final var sponsorAccountCaptor = ArgumentCaptor.forClass(SponsorAccountStatement.class);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccountCaptor.capture());
            final var sponsorAccount = sponsorAccountCaptor.getValue();
            assertThat(sponsorAccount.debt().getValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(sponsorAccount.account().sponsorId()).isEqualTo(sponsorId);
            assertThat(sponsorAccount.account().currency()).isEqualTo(Currencies.USDC);
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.of(transaction.amount()));
            assertThat(sponsorAccount.account().lockedUntil()).isEmpty();
            assertThat(sponsorAccount.allowance()).isEqualTo(PositiveAmount.of(transaction.amount()));

            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId)).hasSize(1);
            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId).get(0).id()).isEqualTo(sponsorAccount.account().id());

            assertThat(accountBookEventStorage.events.get(Currencies.USDC).stream().map(IdentifiedAccountBookEvent::data)).containsExactly(
                    new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(transaction.amount))
            );
        }

        @Test
        void should_approve_deposit_for_sponsor_with_partial_debt() {
            // Given
            final var transaction = TransferTransaction.fakeErc20(250);
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);
            final var sponsor = SponsorView.builder().id(sponsorId).name("Sponsor").logoUrl(URI.create(faker.internet().url())).build();

            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(Deposit.Status.PENDING).build()));
            when(accountingSponsorStoragePort.getView(sponsorId)).thenReturn(Optional.of(sponsor));
            when(currencyStorage.get(Currencies.USDC.id())).thenReturn(Optional.of(Currencies.USDC));

            final var sponsorAccountStatement = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, Currencies.USDC.id(), null,
                    PositiveAmount.of(100L));

            // When
            accountingService.approveDeposit(deposit.id());

            // Then
            verify(depositStoragePort).save(deposit.toBuilder().status(Deposit.Status.COMPLETED).build());

            final var sponsorAccountCaptor = ArgumentCaptor.forClass(SponsorAccountStatement.class);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccountCaptor.capture());
            final var sponsorAccount = sponsorAccountCaptor.getValue();
            assertThat(sponsorAccount.debt().getValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(sponsorAccount.account().id()).isEqualTo(sponsorAccountStatement.account().id());
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.of(transaction.amount()));
            assertThat(sponsorAccount.allowance()).isEqualTo(PositiveAmount.of(transaction.amount()));

            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId)).hasSize(1);
            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId).get(0).id()).isEqualTo(sponsorAccount.account().id());

            assertThat(accountBookEventStorage.events.get(Currencies.USDC).stream().map(IdentifiedAccountBookEvent::data)).containsExactly(
                    new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(100L)),
                    new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(BigDecimal.valueOf(150.0)))
            );
        }

        @Test
        void should_approve_deposit_for_sponsor_with_full_debt() {
            // Given
            final var transaction = TransferTransaction.fakeErc20(25);
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC);
            final var sponsor = SponsorView.builder().id(sponsorId).name("Sponsor").logoUrl(URI.create(faker.internet().url())).build();

            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit.toBuilder().status(Deposit.Status.PENDING).build()));
            when(accountingSponsorStoragePort.getView(sponsorId)).thenReturn(Optional.of(sponsor));
            when(currencyStorage.get(Currencies.USDC.id())).thenReturn(Optional.of(Currencies.USDC));

            final var sponsorAccountStatement = accountingService.createSponsorAccountWithInitialAllowance(sponsorId, Currencies.USDC.id(), null,
                    PositiveAmount.of(100L));

            reset(accountingObserver);

            // When
            accountingService.approveDeposit(deposit.id());

            // Then
            verify(depositStoragePort).save(deposit.toBuilder().status(Deposit.Status.COMPLETED).build());

            final var sponsorAccountCaptor = ArgumentCaptor.forClass(SponsorAccountStatement.class);
            verify(accountingObserver).onSponsorAccountBalanceChanged(sponsorAccountCaptor.capture());
            final var sponsorAccount = sponsorAccountCaptor.getValue();
            assertThat(sponsorAccount.debt().getValue()).isEqualByComparingTo(BigDecimal.valueOf(75));
            assertThat(sponsorAccount.account().id()).isEqualTo(sponsorAccountStatement.account().id());
            assertThat(sponsorAccount.account().unlockedBalance()).isEqualTo(Amount.of(transaction.amount()));
            assertThat(sponsorAccount.allowance()).isEqualTo(PositiveAmount.of(PositiveAmount.of(100L)));

            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId)).hasSize(1);
            assertThat(sponsorAccountStorage.getSponsorAccounts(sponsorId).get(0).id()).isEqualTo(sponsorAccount.account().id());

            assertThat(accountBookEventStorage.events.get(Currencies.USDC).stream().map(IdentifiedAccountBookEvent::data)).containsExactly(
                    new MintEvent(AccountId.of(sponsorAccount.account().id()), PositiveAmount.of(100L))
            );
        }

        @Test
        void should_submit_deposit() {
            // Given
            final var transaction = TransferTransaction.fakeErc20();
            final var deposit = Deposit.preview(sponsorId, transaction, Currencies.USDC).toBuilder().status(Deposit.Status.PENDING).build();
            final UserId userId = UserId.random();
            final Deposit.BillingInformation billingInformation = new Deposit.BillingInformation(null, null, null, null, null, null, null, null, null);

            // When
            when(depositStoragePort.find(deposit.id())).thenReturn(Optional.of(deposit));
            when(permissionPort.isUserSponsorLead(userId, deposit.sponsorId())).thenReturn(true);
            accountingService.submitDeposit(userId, deposit.id(), billingInformation);

            // Then
            verify(depositStoragePort).save(deposit.toBuilder()
                    .status(Deposit.Status.PENDING)
                    .billingInformation(billingInformation)
                    .build());
            verify(depositObserverPort).onDepositSubmittedByUser(userId, deposit.id());
        }

        record Transaction(String reference, ZonedDateTime timestamp, Blockchain blockchain, Status status) implements Blockchain.Transaction {
            static Transaction fake() {
                final var faker = new Faker();
                return new Transaction(faker.crypto().sha256(),
                        ZonedDateTime.now(),
                        Blockchain.ETHEREUM,
                        Status.CONFIRMED);
            }
        }

        record TransferTransaction(String reference,
                                   ZonedDateTime timestamp,
                                   Blockchain blockchain,
                                   Status status,
                                   String senderAddress,
                                   String recipientAddress,
                                   BigDecimal amount,
                                   String address) implements Blockchain.TransferTransaction {
            static final Faker faker = new Faker();

            @Override
            public Optional<String> contractAddress() {
                return Optional.ofNullable(address);
            }

            static TransferTransaction fakeNative() {
                return fakeNative(Status.CONFIRMED);
            }

            static TransferTransaction fakeNative(Status status) {
                return new TransferTransaction("0x" + faker.crypto().sha256(),
                        ZonedDateTime.now(),
                        Blockchain.ETHEREUM,
                        status,
                        faker.crypto().sha256(),
                        faker.crypto().sha256(),
                        BigDecimal.valueOf(faker.number().randomDouble(2, 1, 1000)),
                        null);
            }

            static TransferTransaction fakeErc20() {
                return fakeErc20(faker.number().randomDouble(2, 1, 1000));
            }

            static TransferTransaction fakeErc20(double amount) {
                return new TransferTransaction("0x" + faker.crypto().sha256(),
                        ZonedDateTime.now(),
                        Blockchain.ETHEREUM,
                        Status.CONFIRMED,
                        faker.crypto().sha256(),
                        faker.crypto().sha256(),
                        BigDecimal.valueOf(amount),
                        faker.crypto().sha256());
            }
        }
    }

    /*
     * diagram: https://app.diagrams.net/#G1r4LWMljxwrZyAPJkvC1_rxL8Zsj0v1WN
     */
    @Test
    void complete_test_case() {
        // Given
        final var sponsor1 = SponsorId.random();
        final var sponsor2 = SponsorId.random();
        final var programId1 = ProgramId.random();
        final var programId2 = ProgramId.random();
        final var projectId1 = ProjectId.random();
        final var projectId2 = ProjectId.random();
        final var rewardId1 = RewardId.random();
        final var rewardId2 = RewardId.random();
        final var currency = Currencies.ETH.withERC20(ERC20Tokens.STARKNET_ETH);

        when(currencyStorage.get(currency.id())).thenReturn(Optional.of(currency));
        when(currencyStorage.all()).thenReturn(Set.of(currency));

        // When
        final var sponsor1Account1 = accountingService.createSponsorAccountWithInitialAllowance(sponsor1, currency.id(), null,
                PositiveAmount.of(10_000L)).account();
        accountingService.fund(sponsor1Account1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(5_000L)));
        accountingService.fund(sponsor1Account1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(5_000L)));

        final var sponsor2Account1 = accountingService.createSponsorAccountWithInitialBalance(sponsor2, currency.id(), null,
                fakeTransaction(Network.ETHEREUM, PositiveAmount.of(3_000L))).account();
        accountingService.increaseAllowance(sponsor2Account1.id(), Amount.of(17_000L));

        final var sponsor2Account2 = accountingService.createSponsorAccountWithInitialBalance(sponsor2, currency.id(), ZonedDateTime.now().plusMonths(1),
                fakeTransaction(Network.STARKNET, PositiveAmount.of(50_000L))).account();
        accountingService.increaseAllowance(sponsor2Account2.id(), Amount.of(-30_000L));

        // Then
        assertAccount(sponsor1Account1.id(), 10_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1.id(), 20_000L, 3_000L, 3_000L);
        assertAccount(sponsor2Account2.id(), 20_000L, 50_000L, 0L);

        // When
        accountingService.allocate(sponsor1, programId1, PositiveAmount.of(8_000L), currency.id());
        accountingService.allocate(sponsor2, programId1, PositiveAmount.of(15_000L), currency.id());
        accountingService.grant(programId1, projectId1, PositiveAmount.of(23_000L), currency.id());

        accountingService.allocate(sponsor1, programId2, PositiveAmount.of(1_000L), currency.id());
        accountingService.allocate(sponsor2, programId2, PositiveAmount.of(2_000L), currency.id());
        accountingService.allocate(sponsor2, programId2, PositiveAmount.of(3_000L), currency.id());
        accountingService.allocate(sponsor2, programId2, PositiveAmount.of(5_000L), currency.id());
        accountingService.grant(programId2, projectId2, PositiveAmount.of(11_000L), currency.id());

        // Then
        assertAccount(sponsor1Account1.id(), 1_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1.id(), 0L, 3_000L, 3_000L);
        assertAccount(sponsor2Account2.id(), 15_000L, 50_000L, 0L);

        // When
        accountingService.createReward(projectId2, rewardId1, PositiveAmount.of(3_500L), currency.id());
        accountingService.createReward(projectId2, rewardId2, PositiveAmount.of(4_000L), currency.id());

        // Then
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

        // When (special actions to make reward2 payable)
        accountingService.updateSponsorAccount(sponsor2Account2.id(), null);
        accountingService.fund(sponsor2Account1.id(), fakeTransaction(Network.ETHEREUM, PositiveAmount.of(2_500L)));

        // Then
        assertAccount(sponsor1Account1.id(), 1_000L, 10_000L, 10_000L);
        assertAccount(sponsor2Account1.id(), 0L, 5_500L, 5_500L);
        assertAccount(sponsor2Account2.id(), 15_000L, 50_000L, 50_000L);
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isTrue();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isTrue();

        // When
        final var payments = accountingService.pay(Set.of(rewardId1, rewardId2));

        // Then
        assertThat(payments).hasSize(2);
        final var payment1 = payments.stream().filter(p -> p.network().equals(Network.ETHEREUM)).findFirst().orElseThrow();
        final var payment2 = payments.stream().filter(p -> p.network().equals(Network.STARKNET)).findFirst().orElseThrow();
        assertThat(accountingService.isPayable(rewardId1, currency.id())).isFalse();
        assertThat(accountingService.isPayable(rewardId2, currency.id())).isFalse();

        // When
        final var ethPaymentReference = fakePaymentReference(Network.ETHEREUM);
        accountingService.confirm(payment1
                .confirmedAt(ethPaymentReference.timestamp())
                .transactionHash(ethPaymentReference.reference())
        );

        // Then
        final var capturedReceipts = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptStoragePort, times(2)).save(capturedReceipts.capture());
        final var savedReceipts = capturedReceipts.getAllValues();
        assertThat(savedReceipts.stream().map(Receipt::rewardId)).containsExactlyInAnyOrder(rewardId1, rewardId2);
        assertThat(savedReceipts.stream().map(Receipt::reference).distinct()).containsExactly(ethPaymentReference.reference());
        assertThat(savedReceipts.stream().map(Receipt::network).distinct()).containsExactly(ethPaymentReference.network());
        verify(accountingObserver).onRewardPaid(rewardId1);
        verify(accountingObserver, never()).onRewardPaid(rewardId2);

        assertAccount(sponsor1Account1.id(), 1_000L, 9_000L, 9_000L);
        assertAccount(sponsor2Account1.id(), 0L, 500L, 500L);
        assertAccount(sponsor2Account2.id(), 15_000L, 50_000L, 50_000L);

        // When
        reset(accountingObserver);
        reset(receiptStoragePort);
        final var starknetPaymentReference = fakePaymentReference(Network.STARKNET);
        accountingService.confirm(payment2
                .confirmedAt(starknetPaymentReference.timestamp())
                .transactionHash(starknetPaymentReference.reference()));

        // Then
        final var capturedReceipt = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptStoragePort).save(capturedReceipt.capture());
        final var savedReceipt = capturedReceipt.getValue();
        assertThat(savedReceipt.rewardId()).isEqualTo(rewardId2);
        assertThat(savedReceipt.reference()).isEqualTo(starknetPaymentReference.reference());
        assertThat(savedReceipt.network()).isEqualTo(starknetPaymentReference.network());
        verify(accountingObserver, never()).onRewardPaid(rewardId1);
        verify(accountingObserver).onRewardPaid(rewardId2);

        assertAccount(sponsor1Account1.id(), 1_000L, 9_000L, 9_000L);
        assertAccount(sponsor2Account1.id(), 0L, 500L, 500L);
        assertAccount(sponsor2Account2.id(), 15_000L, 48_500L, 48_500L);

        assertThat(accountBookEventStorage.events.get(currency).stream().map(IdentifiedAccountBookEvent::data)).containsExactlyInAnyOrder(
                new MintEvent(AccountId.of(sponsor1Account1.id()), PositiveAmount.of(10_000L)),
                new MintEvent(AccountId.of(sponsor2Account1.id()), PositiveAmount.of(3_000L)),
                new MintEvent(AccountId.of(sponsor2Account1.id()), PositiveAmount.of(17_000L)),
                new MintEvent(AccountId.of(sponsor2Account2.id()), PositiveAmount.of(50_000L)),
                new RefundEvent(AccountId.of(sponsor2Account2.id()), AccountId.ROOT, PositiveAmount.of(30_000L)),
                new TransferEvent(AccountId.of(sponsor1Account1.id()), AccountId.of(programId1), PositiveAmount.of(8_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1.id()), AccountId.of(programId1), PositiveAmount.of(15_000L)),
                new TransferEvent(AccountId.of(programId1), AccountId.of(projectId1), PositiveAmount.of(23_000L)),
                new TransferEvent(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2), PositiveAmount.of(1_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), PositiveAmount.of(2_000L)),
                new TransferEvent(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), PositiveAmount.of(3_000L)),
                new TransferEvent(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2), PositiveAmount.of(5_000L)),
                new TransferEvent(AccountId.of(programId2), AccountId.of(projectId2), PositiveAmount.of(11_000L)),
                new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId1), PositiveAmount.of(3_500L)),
                new TransferEvent(AccountId.of(projectId2), AccountId.of(rewardId2), PositiveAmount.of(4_000L)),
                new TransferEvent(AccountId.of(rewardId1), AccountId.of(payment1.id()), PositiveAmount.of(3_500L)),
                new TransferEvent(AccountId.of(rewardId2), AccountId.of(payment1.id()), PositiveAmount.of(2_500L)),
                new TransferEvent(AccountId.of(rewardId2), AccountId.of(payment2.id()), PositiveAmount.of(1_500L)),
                new BurnEvent(AccountId.of(payment1.id()), PositiveAmount.of(6_000L)),
                new BurnEvent(AccountId.of(payment2.id()), PositiveAmount.of(1_500L))
        );

        final var transactionsCaptor = ArgumentCaptor.forClass(AccountBook.Transaction.class);
        verify(accountBookObserver, atLeastOnce()).on(any(), any(), transactionsCaptor.capture());
        final var transactions = transactionsCaptor.getAllValues();
        // @formatter:off
        assertThat(transactions).containsExactlyInAnyOrder(
                new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsor1Account1.id())), PositiveAmount.of(10_000L)),
                new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsor2Account1.id())), PositiveAmount.of(3_000L)),
                new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsor2Account1.id())), PositiveAmount.of(17_000L)),
                new AccountBook.Transaction(MINT, List.of(AccountId.of(sponsor2Account2.id())), PositiveAmount.of(50_000L)),
                new AccountBook.Transaction(REFUND, List.of(AccountId.of(sponsor2Account2.id())), PositiveAmount.of(30_000L)),

                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId1)), PositiveAmount.of(8_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId1)), PositiveAmount.of(3_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId1)), PositiveAmount.of(12_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2)), PositiveAmount.of(1_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2)), PositiveAmount.of(2_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2)), PositiveAmount.of(3_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2)), PositiveAmount.of(5_000L)),

                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId1), AccountId.of(projectId1)), PositiveAmount.of(8_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId1), AccountId.of(projectId1)), PositiveAmount.of(3_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId1), AccountId.of(projectId1)), PositiveAmount.of(12_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2), AccountId.of(projectId2)), PositiveAmount.of(1_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2)), PositiveAmount.of(2_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2)), PositiveAmount.of(3_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2), AccountId.of(projectId2)), PositiveAmount.of(5_000L)),

                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1)), PositiveAmount.of(1_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1)), PositiveAmount.of(2_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1)), PositiveAmount.of(500L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2)), PositiveAmount.of(2_500L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2)), PositiveAmount.of(1_500L)),

                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(1_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(2_000L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(500L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2), AccountId.of(payment1.id())), PositiveAmount.of(2_500L)),
                new AccountBook.Transaction(TRANSFER, List.of(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2), AccountId.of(payment2.id())), PositiveAmount.of(1_500L)),

                new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsor1Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(1_000L)),
                new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(2_000L)),
                new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId1), AccountId.of(payment1.id())), PositiveAmount.of(500L)),
                new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsor2Account1.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2), AccountId.of(payment1.id())), PositiveAmount.of(2_500L)),
                new AccountBook.Transaction(BURN, List.of(AccountId.of(sponsor2Account2.id()), AccountId.of(programId2), AccountId.of(projectId2), AccountId.of(rewardId2), AccountId.of(payment2.id())), PositiveAmount.of(1_500L))
        );
        // @formatter:on
    }

    private void assertAccount(SponsorAccount.Id sponsorAccountId, Long expectedAllowance, Long expectedBalance, Long expectedUnlockedBalance) {
        final var sponsorAccountStatement = accountingService.getSponsorAccountStatement(sponsorAccountId).orElseThrow();
        assertThat(sponsorAccountStatement.allowance()).isEqualTo(PositiveAmount.of(expectedAllowance));
        assertThat(sponsorAccountStatement.account().balance()).isEqualTo(Amount.of(expectedBalance));
        assertThat(sponsorAccountStatement.account().unlockedBalance()).isEqualTo(Amount.of(expectedUnlockedBalance));
    }
}
