package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.ReadOnlyAccountBookState;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.BlockchainFacadePort;
import onlydust.com.marketplace.accounting.domain.port.in.RewardStatusFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.model.*;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.kernel.port.output.PermissionPort;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.accounting.domain.model.Amount.*;
import static onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction.Type.SPEND;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.*;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final CachedAccountBookProvider accountBookProvider;
    private final SponsorAccountStorage sponsorAccountStorage;
    private final CurrencyStorage currencyStorage;
    private final AccountingObserverPort accountingObserver;
    private final ProjectAccountingObserver projectAccountingObserver;
    private final InvoiceStoragePort invoiceStoragePort;
    private final RewardStatusFacadePort rewardStatusFacadePort;
    private final ReceiptStoragePort receiptStorage;
    private final BlockchainFacadePort blockchainFacadePort;
    private final DepositStoragePort depositStoragePort;
    private final TransactionStoragePort transactionStoragePort;
    private final PermissionPort permissionPort;
    private final OnlyDustWallets onlyDustWallets;
    private final DepositObserverPort depositObserverPort;
    private final AccountingSponsorStoragePort accountingSponsorStoragePort;

    @Override
    @Transactional
    public SponsorAccountStatement createSponsorAccountWithInitialAllowance(@NonNull SponsorId sponsorId, @NonNull Currency.Id currencyId,
                                                                            ZonedDateTime lockedUntil, @NonNull PositiveAmount allowance) {
        final var sponsorAccount = createSponsorAccount(sponsorId, currencyId, lockedUntil);
        increaseAllowance(sponsorAccount.id(), allowance);
        return sponsorAccountStatement(sponsorAccount, getAccountBook(currencyId).state());
    }

    @Transactional
    public SponsorAccountStatement createSponsorAccountWithInitialBalance(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId,
                                                                          ZonedDateTime lockedUntil,
                                                                          @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = createSponsorAccount(sponsorId, currencyId, lockedUntil);
        return fund(sponsorAccount.id(), transaction);
    }

    @Override
    @Transactional
    public SponsorAccountStatement increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBook = getAccountBook(sponsorAccount.currency());

        if (amount.isPositive()) accountBook.mint(AccountId.of(sponsorAccountId), PositiveAmount.of(amount));
        else accountBook.refund(AccountId.of(sponsorAccountId), AccountId.ROOT, PositiveAmount.of(amount.negate()));

        saveAccountBook(sponsorAccount.currency(), accountBook);
        return sponsorAccountStatement(sponsorAccount, accountBook.state());
    }

    @Override
    @Transactional
    public void allocate(final SponsorId from, final ProgramId to, final PositiveAmount amount, final Currency.Id currencyId) {
        final var sponsorAccountIds = sponsorAccountStorage.find(from, currencyId).stream()
                .map(SponsorAccount::id)
                .toList();

        transfer(sponsorAccountIds, to, amount, currencyId);
        accountingObserver.onFundsAllocatedToProgram(from, to, amount, currencyId);
    }

    @Override
    @Transactional
    public void unallocate(ProgramId from, SponsorId to, PositiveAmount amount, Currency.Id currencyId) {
        final var sponsorAccountIds = reversed(sponsorAccountStorage.find(to, currencyId)).stream().map(SponsorAccount::id).toList();
        refund(from, sponsorAccountIds, amount, currencyId);
        accountingObserver.onFundsRefundedByProgram(from, to, amount, currencyId);
    }

    private static <T> List<T> reversed(List<T> list) {
        List<T> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);
        return reversed;
    }

    @Override
    public void grant(ProgramId from, ProjectId to, PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = transfer(from, to, amount, currencyId);

        onAllowanceUpdated(to, currencyId, accountBookState);
    }

    @Override
    public void ungrant(ProjectId from, ProgramId to, @NonNull PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = refund(from, to, amount, currencyId);
        onAllowanceUpdated(from, currencyId, accountBookState);
    }

    @Override
    @Transactional
    public SponsorAccountStatement fund(@NonNull SponsorAccount.Id sponsorAccountId, @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBookState = getAccountBook(sponsorAccount.currency()).state();
        final var accountStatement = registerSponsorAccountTransaction(accountBookState, sponsorAccount, transaction);
        accountingObserver.onSponsorAccountBalanceChanged(accountStatement);

        final var allowanceToAdd = min(transaction.amount(), max(accountStatement.debt().negate(), ZERO));
        if (allowanceToAdd.isStrictlyPositive()) increaseAllowance(sponsorAccountId, allowanceToAdd);

        return accountStatement;
    }

    @Override
    @Transactional
    public void createReward(ProjectId from, RewardId to, PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = transfer(List.of(from), to, amount, currencyId);
        final var accountBookFacade = new AccountBookFacade(sponsorAccountStorage, accountBookState);
        rewardStatusFacadePort.create(accountBookFacade, to);
        accountingObserver.onRewardCreated(to, accountBookFacade);
        onAllowanceUpdated(from, currencyId, accountBookState);
    }

    @Override
    @Transactional
    public Payment pay(final @NonNull RewardId rewardId,
                       final @NonNull ZonedDateTime confirmedAt,
                       final @NonNull Network network,
                       final @NonNull String transactionHash) {
        final var payableRewards = filterPayableRewards(network, Set.of(rewardId));

        if (payableRewards.isEmpty()) throw badRequest("Reward %s is not payable on %s".formatted(rewardId, network));

        final var payment = pay(network, payableRewards);
        confirm(payment.confirmedAt(confirmedAt).transactionHash(transactionHash));
        return payment;
    }

    @Override
    @Transactional
    public List<Payment> pay(final Set<RewardId> rewardIds) {
        return getPayableRewards(rewardIds).stream()
                .collect(groupingBy(r -> r.currency().network()))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> pay(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Payment pay(final @NonNull Network network, final List<PayableReward> rewards) {
        final var payment = Payment.of(network, rewards);

        payment.rewards().stream()
                .collect(groupingBy(PayableReward::currency))
                .forEach((currency, currencyRewards) -> pay(payment.id(), currency, currencyRewards));

        return payment;
    }

    private void pay(final @NonNull Payment.Id paymentId, final @NonNull PayableCurrency payableCurrency, final List<PayableReward> rewards) {
        final var currency = getCurrency(payableCurrency.id());
        final var accountBook = getAccountBook(currency);

        rewards.forEach(reward -> accountBook.transfer(AccountId.of(reward.id()), AccountId.of(paymentId), reward.amount()));
        saveAccountBook(currency, accountBook);
    }

    @Override
    @Transactional
    public void cancel(@NonNull RewardId rewardId, @NonNull Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        if (invoiceStoragePort.invoiceOf(rewardId).map(i -> i.status().isActive()).orElse(false))
            throw forbidden("Reward %s cannot be cancelled because it is included in an invoice".formatted(rewardId));

        final var refundedAccounts = accountBook.refund(AccountId.of(rewardId)).stream().map(t -> t.path().get(t.path().size() - 2)).collect(toSet());
        saveAccountBook(currency, accountBook);
        accountingObserver.onRewardCancelled(rewardId);
        refundedAccounts.stream().filter(AccountId::isProject).map(AccountId::projectId)
                .forEach(refundedProjectId -> onAllowanceUpdated(refundedProjectId, currencyId, accountBook.state()));
        rewardStatusFacadePort.delete(rewardId);
    }

    @Override
    @Transactional
    public void cancel(@NonNull Payment payment) {
        payment.rewards().stream()
                .map(PayableReward::currency)
                .distinct()
                .forEach(currency -> cancel(payment.id(), currency.id()));
    }

    private void cancel(@NonNull Payment.Id paymentId, @NonNull Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.refund(AccountId.of(paymentId));
        saveAccountBook(currency, accountBook);
    }

    @Override
    @Transactional
    public void confirm(final @NonNull Payment payment) {
        payment.rewards().stream()
                .collect(groupingBy(PayableReward::currency))
                .forEach((currency, rewards) -> confirm(payment, currency.id(), rewards));
    }

    private void confirm(Payment payment, Currency.Id currencyId, List<PayableReward> rewards) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.burn(AccountId.of(payment.id()), rewards.stream().map(PayableReward::amount).reduce(PositiveAmount::add).orElse(PositiveAmount.ZERO));
        saveAccountBook(currency, accountBook);

        final var modifiedSponsorAccounts = rewards.stream()
                .flatMap(r -> confirm(accountBook.state(), r, payment.referenceFor(r.id())).stream())
                .collect(toSet());

        modifiedSponsorAccounts.forEach(accountingObserver::onSponsorAccountBalanceChanged);
    }

    private Set<SponsorAccountStatement> confirm(ReadOnlyAccountBookState accountBookState, PayableReward reward, Payment.Reference paymentReference) {
        final var modifiedSponsorAccounts = new HashSet<SponsorAccountStatement>();

        accountBookState.transferredAmountPerOrigin(AccountId.of(reward.id()))
                .entrySet()
                .stream().map(e -> Map.entry(
                        mustGetSponsorAccount(e.getKey().sponsorAccountId()),
                        new SponsorAccount.Transaction(SPEND, paymentReference, e.getValue())
                ))
                .filter(e -> paymentReference.network().equals(e.getKey().network().orElse(null)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((account, transaction) -> {
                    registerSponsorAccountTransaction(accountBookState, account, transaction);
                    modifiedSponsorAccounts.add(sponsorAccountStatement(account, accountBookState));
                });

        receiptStorage.save(Receipt.of(reward.id(), paymentReference));
        if (isPaid(accountBookState, reward.id()))
            accountingObserver.onRewardPaid(reward.id());

        return modifiedSponsorAccounts;
    }

    private static boolean isPaid(ReadOnlyAccountBookState accountBookState, RewardId rewardId) {
        final var rewardAccountId = AccountId.of(rewardId);
        return accountBookState.balanceOf(rewardAccountId).isZero() && accountBookState.unspentChildren(rewardAccountId).keySet().stream().noneMatch(AccountId::isPayment);
    }

    @Override
    public boolean isPayable(RewardId rewardId, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        return new PayableRewardAggregator(sponsorAccountStorage, currency).isPayable(rewardId);
    }

    @Override
    @Transactional
    public SponsorAccountStatement updateSponsorAccount(SponsorAccount.@NonNull Id sponsorAccountId, ZonedDateTime lockedUntil) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        sponsorAccount.lockUntil(lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);
        final var sponsorAccountStatement = sponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()).state());
        accountingObserver.onSponsorAccountUpdated(sponsorAccountStatement);
        return sponsorAccountStatement;
    }

    public List<PayableReward> getPayableRewards(Set<RewardId> rewardIds) {
        return currencyStorage.all().stream()
                .flatMap(currency -> new PayableRewardAggregator(sponsorAccountStorage, currency).getPayableRewards(rewardIds))
                .toList();
    }

    @Override
    public Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId).map(sponsorAccount -> sponsorAccountStatement(sponsorAccount,
                getAccountBook(sponsorAccount.currency()).state()));
    }

    private List<PayableReward> filterPayableRewards(@NonNull Network network, @NonNull Set<RewardId> rewardIds) {
        return getPayableRewards(rewardIds).stream().filter(payableReward -> network.equals(payableReward.currency().network())).toList();
    }

    private SponsorAccount createSponsorAccount(@NonNull SponsorId sponsorId, Currency.@NonNull Id currencyId, ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);
        final var sponsorAccount = new SponsorAccount(sponsorId, currency, lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);
        return sponsorAccount;
    }

    private <From, To> ReadOnlyAccountBookState transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        if (from instanceof List froms)
            accountBook.transfer(froms.stream().map(AccountId::of).toList(), AccountId.of(to), amount);
        else
            accountBook.transfer(AccountId.of(from), AccountId.of(to), amount);

        saveAccountBook(currency, accountBook);
        return accountBook.state();
    }

    private <From, To> ReadOnlyAccountBookState refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        if (to instanceof List tos)
            accountBook.refund(AccountId.of(from), tos.stream().map(AccountId::of).toList(), amount);
        else
            accountBook.refund(AccountId.of(from), AccountId.of(to), amount);

        saveAccountBook(currency, accountBook);
        return accountBook.state();
    }

    private void onAllowanceUpdated(ProjectId projectId, Currency.Id currencyId, ReadOnlyAccountBookState accountBook) {
        projectAccountingObserver.onAllowanceUpdated(projectId, currencyId, accountBook.balanceOf(AccountId.of(projectId)),
                accountBook.amountReceivedBy(AccountId.of(projectId)));
    }

    private SponsorAccountStatement registerSponsorAccountTransaction(ReadOnlyAccountBookState accountBookState, SponsorAccount sponsorAccount,
                                                                      SponsorAccount.Transaction transaction) {
        sponsorAccount.add(transaction);
        sponsorAccountStorage.save(sponsorAccount);
        return sponsorAccountStatement(sponsorAccount, accountBookState);
    }

    private SponsorAccount mustGetSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId).orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return accountBookProvider.get(currency);
    }

    private void saveAccountBook(Currency currency, AccountBookAggregate accountBook) {
        accountBookProvider.save(currency, accountBook);
    }

    private AccountBookAggregate getAccountBook(Currency.Id currencyId) {
        return getAccountBook(getCurrency(currencyId));
    }

    public SponsorAccountStatement delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId) {
        sponsorAccountStorage.delete(sponsorAccountId, transactionId);
        final var sponsorAccountStatement = getSponsorAccountStatement(sponsorAccountId).orElseThrow();
        accountingObserver.onSponsorAccountBalanceChanged(sponsorAccountStatement);
        return sponsorAccountStatement;
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id).orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }

    private SponsorAccountStatement sponsorAccountStatement(SponsorAccount sponsorAccount, ReadOnlyAccountBookState accountBookState) {
        return new SponsorAccountStatement(sponsorAccount, new AccountBookFacade(sponsorAccountStorage, accountBookState));
    }

    class PayableRewardAggregator {
        private final @NonNull CachedSponsorAccountProvider sponsorAccountProvider;
        private final @NonNull Currency currency;
        private final @NonNull ReadOnlyAccountBookState accountBookState;

        public PayableRewardAggregator(final @NonNull SponsorAccountProvider sponsorAccountProvider, final @NonNull Currency currency) {
            this.sponsorAccountProvider = new CachedSponsorAccountProvider(sponsorAccountProvider);
            this.currency = currency;
            this.accountBookState = getAccountBook(currency).state();
        }

        public Stream<PayableReward> getPayableRewards(Set<RewardId> rewardIds) {
            final var distinctPayableRewards = accountBookState.unspentChildren().keySet().stream()
                    .filter(AccountId::isReward)
                    .filter(rewardAccountId -> rewardIds == null || rewardIds.contains(rewardAccountId.rewardId()))
                    .filter(rewardAccountId -> isPayable(rewardAccountId.rewardId()))
                    .flatMap(this::trySpend)
                    .collect(groupingBy(PayableReward::key, reducing(PayableReward::add)));

            return distinctPayableRewards.values().stream().filter(Optional::isPresent).map(Optional::get);
        }

        private Stream<PayableReward> trySpend(AccountId rewardAccountId) {
            return accountBookState.balancePerOrigin(rewardAccountId).entrySet().stream()
                    .filter(e -> e.getValue().isStrictlyPositive())
                    .filter(e -> stillEnoughBalance(e.getKey().sponsorAccountId(), e.getValue()))
                    .map(e -> {
                        final var sponsorAccountId = e.getKey().sponsorAccountId();
                        final var reward = createPayableReward(sponsorAccountId, rewardAccountId.rewardId(), e.getValue());
                        spend(sponsorAccountId, reward);
                        return reward;
                    });
        }

        private void spend(SponsorAccount.Id sponsorAccountId, PayableReward reward) {
            final var sponsorAccount = sponsorAccount(sponsorAccountId);

            final var sponsorAccountNetwork = sponsorAccount.network().orElseThrow();
            sponsorAccount.add(new SponsorAccount.Transaction(ZonedDateTime.now(), SPEND, sponsorAccountNetwork, reward.id().toString(),
                    reward.amount(), reward.recipientName(), reward.recipientWallet().toString()));
        }

        private PayableReward createPayableReward(SponsorAccount.Id sponsorAccountId, RewardId rewardId, PositiveAmount amount) {
            final var sponsorAccountNetwork = sponsorAccount(sponsorAccountId).network().orElseThrow();
            final var invoice = invoiceStoragePort.invoiceViewOf(rewardId).orElseThrow();
            return PayableReward.of(rewardId, currency.forNetwork(sponsorAccountNetwork), amount, invoice.billingProfileSnapshot());
        }

        private boolean stillEnoughBalance(SponsorAccount.Id sponsorAccountId, PositiveAmount amount) {
            return sponsorAccount(sponsorAccountId).unlockedBalance().isGreaterThanOrEqual(amount);
        }

        private SponsorAccount sponsorAccount(SponsorAccount.Id sponsorAccountId) {
            return sponsorAccountProvider.get(sponsorAccountId).orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
        }

        private boolean isPayable(RewardId rewardId) {
            if (accountBookState.balanceOf(AccountId.of(rewardId)).isZero()) return false;
            if (!invoiceStoragePort.invoiceOf(rewardId).map(i -> i.status().isApproved()).orElse(false))
                return false;

            return accountBookState.balancePerOrigin(AccountId.of(rewardId)).entrySet().stream().allMatch(entry -> {
                final var sponsorAccount = sponsorAccountProvider.get(entry.getKey().sponsorAccountId())
                        .orElseThrow(() -> notFound(("Sponsor account %s not found").formatted(entry.getKey().sponsorAccountId())));
                return sponsorAccount.unlockedBalance().isGreaterThanOrEqual(entry.getValue());
            });
        }
    }

    @Override
    public List<Network> networksOf(Currency.Id currencyId, RewardId rewardId) {
        final var accountBook = getAccountBook(currencyId);
        return accountBook.state().balancePerOrigin(AccountId.of(rewardId)).keySet().stream()
                .map(accountId -> mustGetSponsorAccount(accountId.sponsorAccountId()).network())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Map<SponsorAccount, PositiveAmount> balancesPerOrigin(RewardId id, Currency.Id currencyId) {
        return getAccountBook(currencyId).state().balancePerOrigin(AccountId.of(id)).entrySet().stream()
                .collect(toMap(e -> mustGetSponsorAccount(e.getKey().sponsorAccountId()), Map.Entry::getValue));
    }

    @Override
    public Map<SponsorAccount, PositiveAmount> transferredAmountPerOrigin(RewardId id, Currency.Id currencyId) {
        return getAccountBook(currencyId).state().transferredAmountPerOrigin(AccountId.of(id)).entrySet().stream()
                .collect(toMap(e -> mustGetSponsorAccount(e.getKey().sponsorAccountId()), Map.Entry::getValue));
    }

    @Override
    public Deposit previewDeposit(final @NonNull SponsorId sponsorId, final @NonNull Network network, final @NonNull String transactionReference) {
        final var blockchain = network.blockchain()
                .orElseThrow(() -> badRequest("Network %s is not associated with a blockchain".formatted(network)));

        final var deposit = tryCreateDeposit(sponsorId, blockchain, transactionReference);

        final var latestBillingInformation = depositStoragePort.findLatestBillingInformation(sponsorId);
        return deposit.toBuilder()
                .billingInformation(latestBillingInformation.orElse(null))
                .build();
    }

    private Deposit tryCreateDeposit(final @NonNull SponsorId sponsorId, final @NonNull Blockchain blockchain, final @NonNull String transactionReference) {
        final var sanitizedTransactionReference = blockchainFacadePort.sanitizedTransactionReference(blockchain, transactionReference);

        if (transactionStoragePort.exists(sanitizedTransactionReference)) {
            final var existingDeposit = depositStoragePort.findByTransactionReference(sanitizedTransactionReference);
            if (existingDeposit.isPresent() && existingDeposit.get().status() == Deposit.Status.DRAFT) {
                return existingDeposit.get();
            }
            throw badRequest("Transaction %s already exists".formatted(sanitizedTransactionReference));
        }

        final var transaction = blockchainFacadePort.getTransaction(blockchain, sanitizedTransactionReference)
                .orElseThrow(() -> notFound("Transaction %s not found on blockchain %s".formatted(sanitizedTransactionReference, blockchain.pretty())));

        final var transferTransaction = check(transaction);

        final var currency = transferTransaction.contractAddress()
                .map(address -> currencyStorage.findByErc20(blockchain, address)
                        .orElseThrow(() -> badRequest("Currency %s not supported on blockchain %s".formatted(address, blockchain.pretty()))))
                .orElseGet(() -> currencyStorage.findByCode(Currency.Code.of(blockchain))
                        .orElseThrow(() -> badRequest("Native currency not supported on blockchain %s".formatted(blockchain.pretty()))));

        final var deposit = Deposit.preview(sponsorId, transferTransaction, currency);
        depositStoragePort.save(deposit);
        return deposit;
    }

    private Blockchain.TransferTransaction check(Blockchain.Transaction transaction) {
        if (transaction instanceof Blockchain.TransferTransaction transferTransaction) {
            if (transaction.status() != Blockchain.Transaction.Status.CONFIRMED)
                throw badRequest("Transaction %s is not confirmed on blockchain %s"
                        .formatted(transaction.reference(), transaction.blockchain()));

            final var expectedRecipientAddress = onlyDustWallets.get(transferTransaction.blockchain())
                    .orElseThrow(() -> badRequest("Transaction's (%s) blockchain (%s) is not supported for deposits"
                            .formatted(transaction.reference(), transferTransaction.blockchain())));

            if (!StringUtils.equalsIgnoreCase(transferTransaction.recipientAddress(), expectedRecipientAddress))
                throw badRequest("Transaction's (%s) recipient (%s) is not equal to the OnlyDust wallet (%s) expected on blockchain %s"
                        .formatted(transaction.reference(), transferTransaction.recipientAddress(), expectedRecipientAddress, transaction.blockchain()));

            return transferTransaction;
        }

        throw badRequest("Transaction %s is not a transfer transaction".formatted(transaction.reference()));
    }

    @Override
    public Amount getSponsorBalance(@NonNull SponsorId sponsorId, @NonNull Currency currency) {
        final var accountBookState = getAccountBook(currency.id()).state();

        return sponsorAccountStorage.find(sponsorId, currency.id()).stream()
                .map(sponsorAccount -> accountBookState.balanceOf(AccountId.of(sponsorAccount.id())))
                .reduce(PositiveAmount::add)
                .orElse(PositiveAmount.ZERO);
    }

    @Override
    @Transactional
    public void submitDeposit(UserId userId, Deposit.Id depositId, Deposit.BillingInformation billingInformation) {
        final var deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));

        if (!permissionPort.isUserSponsorLead(userId, deposit.sponsorId())) {
            throw forbidden("User %s is not allowed to update deposit %s".formatted(userId, depositId));
        }

        depositStoragePort.save(deposit.toBuilder()
                .status(Deposit.Status.PENDING)
                .billingInformation(billingInformation)
                .build());

        depositObserverPort.onDepositSubmittedByUser(userId, depositId);
    }

    @Override
    public void rejectDeposit(Deposit.Id depositId) {
        final var deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));

        if (deposit.status() != Deposit.Status.PENDING)
            throw badRequest("Deposit %s is not pending".formatted(depositId));

        depositStoragePort.save(deposit.toBuilder()
                .status(Deposit.Status.REJECTED)
                .build());
    }

    @Override
    @Transactional
    public void approveDeposit(Deposit.Id depositId) {
        final var deposit = depositStoragePort.find(depositId)
                .orElseThrow(() -> notFound("Deposit %s not found".formatted(depositId)));

        if (deposit.status() != Deposit.Status.PENDING)
            throw badRequest("Deposit %s is not pending".formatted(depositId));

        final var sponsor = accountingSponsorStoragePort.getView(deposit.sponsorId())
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(deposit.sponsorId())));

        depositStoragePort.save(deposit.toBuilder()
                .status(Deposit.Status.COMPLETED)
                .build());

        final var accountBook = getAccountBook(deposit.currency());
        final var transaction = SponsorAccount.Transaction.deposit(sponsor, deposit.transaction());

        sponsorAccountStorage.find(deposit.sponsorId(), deposit.currency().id())
                .stream()
                .map(sponsorAccount -> sponsorAccountStatement(sponsorAccount, accountBook.state()))
                .filter(statement -> statement.debt().isStrictlyPositive())
                .findFirst()
                .ifPresentOrElse(
                        statement -> fund(statement.account().id(), transaction),
                        () -> createSponsorAccountWithInitialBalance(deposit.sponsorId(), deposit.currency().id(), null, transaction)
                );
    }
}
