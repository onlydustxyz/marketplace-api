package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookObserver;
import onlydust.com.marketplace.accounting.domain.model.accountbook.ReadOnlyAccountBookState;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;
import static onlydust.com.marketplace.accounting.domain.model.Amount.*;
import static onlydust.com.marketplace.accounting.domain.model.SponsorAccount.Transaction.Type.SPEND;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final CachedAccountBookProvider accountBookProvider;
    private final SponsorAccountStorage sponsorAccountStorage;
    private final CurrencyStorage currencyStorage;
    private final AccountingObserverPort accountingObserver;
    private final ProjectAccountingObserver projectAccountingObserver;
    private final InvoiceStoragePort invoiceStoragePort;
    private final AccountBookObserver accountBookObserver;

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
    public void allocate(SponsorAccount.Id from, ProjectId to, PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = transfer(from, to, amount, currencyId);

        onAllowanceUpdated(to, currencyId, accountBookState);
        projectAccountingObserver.onBudgetAllocatedToProject(sponsorAccountStorage.get(from).orElseThrow(() -> notFound("Sponsor account %s not found".formatted(from))).sponsorId(), to);
    }

    @Override
    @Transactional
    public void unallocate(ProjectId from, SponsorAccount.Id to, PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = refund(from, to, amount, currencyId);
        onAllowanceUpdated(from, currencyId, accountBookState);
    }

    @Override
    @Transactional
    public SponsorAccountStatement fund(@NonNull SponsorAccount.Id sponsorAccountId, @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBookState = getAccountBook(sponsorAccount.currency()).state();
        final var accountStatement = registerSponsorAccountTransaction(accountBookState, sponsorAccount, transaction);

        final var allowanceToAdd = min(transaction.amount(), max(accountStatement.debt().negate(), ZERO));
        if (allowanceToAdd.isStrictlyPositive()) increaseAllowance(sponsorAccountId, allowanceToAdd);

        return accountStatement;
    }

    @Override
    @Transactional
    public void createReward(ProjectId from, RewardId to, PositiveAmount amount, Currency.Id currencyId) {
        final var accountBookState = transfer(from, to, amount, currencyId);
        accountingObserver.onRewardCreated(to, new AccountBookFacade(sponsorAccountStorage, accountBookState));
        onAllowanceUpdated(from, currencyId, accountBookState);
    }

    @Override
    @Transactional
    public void pay(final @NonNull RewardId rewardId, final @NonNull Payment.Reference paymentReference) {
        final var network = paymentReference.network();
        final var payableRewards = filterPayableRewards(network, Set.of(rewardId));

        if (payableRewards.isEmpty()) throw badRequest("Reward %s is not payable on %s".formatted(rewardId, network));

        final var payment = pay(network, payableRewards);
        payment.referenceFor(rewardId, paymentReference);
        confirm(payment);
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

        final var refundedAccounts = accountBook.refund(AccountId.of(rewardId));
        saveAccountBook(currency, accountBook);
        accountingObserver.onRewardCancelled(rewardId);
        refundedAccounts.stream().filter(AccountId::isProject).map(AccountId::projectId).forEach(refundedProjectId -> onAllowanceUpdated(refundedProjectId,
                currencyId, accountBook.state()));
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

        rewards.forEach(r -> confirm(accountBook.state(), r, payment.referenceFor(r.id())));
    }

    private void confirm(ReadOnlyAccountBookState accountBookState, PayableReward reward, Payment.Reference paymentReference) {
        accountBookState.transferredAmountPerOrigin(AccountId.of(reward.id()))
                .entrySet()
                .stream().map(e -> Map.entry(
                        mustGetSponsorAccount(e.getKey().sponsorAccountId()),
                        new SponsorAccount.Transaction(SPEND, paymentReference, e.getValue().negate())
                ))
                .filter(e -> paymentReference.network().equals(e.getKey().network().orElse(null)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
                .forEach((account, transaction) -> registerSponsorAccountTransaction(accountBookState, account, transaction));

        accountingObserver.onPaymentReceived(reward.id(), paymentReference);
        if (isPaid(accountBookState, reward.id()))
            accountingObserver.onRewardPaid(reward.id());
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
    public Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId).map(sponsorAccount -> sponsorAccountStatement(sponsorAccount,
                getAccountBook(sponsorAccount.currency()).state()));
    }

    @Override
    public Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId);
    }

    @Override
    public List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountStorage.getSponsorAccounts(sponsorId).stream().map(sponsorAccount -> sponsorAccountStatement(sponsorAccount,
                getAccountBook(sponsorAccount.currency()).state())).toList();
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
                .filter(r -> invoiceStoragePort.invoiceOf(r.id()).map(i -> i.status() == Invoice.Status.APPROVED).orElse(false))
                .toList();
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

        accountBook.transfer(AccountId.of(from), AccountId.of(to), amount);
        saveAccountBook(currency, accountBook);
        return accountBook.state();
    }

    private <From, To> ReadOnlyAccountBookState refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

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
        final var statement = sponsorAccountStatement(sponsorAccount, accountBookState);
        accountingObserver.onSponsorAccountBalanceChanged(statement);
        return statement;
    }

    private SponsorAccount mustGetSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId).orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return accountBookProvider.get(currency);
    }

    private void saveAccountBook(Currency currency, AccountBookAggregate accountBook) {
        final var events = accountBookProvider.save(currency, accountBook);
        events.forEach(accountBookObserver::on);
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
            final var distinctPayableRewards =
                    accountBookState.unspentChildren().keySet().stream()
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
                    .peek(e -> spend(e.getKey().sponsorAccountId(), rewardAccountId.rewardId(), e.getValue()))
                    .map(e -> createPayableReward(e.getKey().sponsorAccountId(), rewardAccountId.rewardId(), e.getValue()));
        }

        private void spend(SponsorAccount.Id sponsorAccountId, RewardId rewardId, PositiveAmount amount) {
            final var sponsorAccount = sponsorAccount(sponsorAccountId);

            final var sponsorAccountNetwork = sponsorAccount.network().orElseThrow();
            sponsorAccount.add(new SponsorAccount.Transaction(SPEND, sponsorAccountNetwork, rewardId.toString(),
                    amount.negate(), "", ""));
        }

        private PayableReward createPayableReward(SponsorAccount.Id sponsorAccountId, RewardId rewardId, PositiveAmount amount) {
            final var sponsorAccountNetwork = sponsorAccount(sponsorAccountId).network().orElseThrow();
            return new PayableReward(rewardId, currency.forNetwork(sponsorAccountNetwork), amount);
        }

        private boolean stillEnoughBalance(SponsorAccount.Id sponsorAccountId, PositiveAmount amount) {
            return sponsorAccount(sponsorAccountId).unlockedBalance().isGreaterThanOrEqual(amount);
        }

        private SponsorAccount sponsorAccount(SponsorAccount.Id sponsorAccountId) {
            return sponsorAccountProvider.get(sponsorAccountId).orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
        }

        private boolean isPayable(RewardId rewardId) {
            if (accountBookState.balanceOf(AccountId.of(rewardId)).isZero()) return false;

            return accountBookState.balancePerOrigin(AccountId.of(rewardId)).entrySet().stream().allMatch(entry -> {
                final var sponsorAccount = sponsorAccountProvider.get(entry.getKey().sponsorAccountId())
                        .orElseThrow(() -> notFound(("Sponsor account %s not found").formatted(entry.getKey().sponsorAccountId())));
                return sponsorAccount.unlockedBalance().isGreaterThanOrEqual(entry.getValue());
            });
        }
    }

    @Override
    public Page<HistoricalTransaction> transactionHistory(SponsorId sponsorId, Integer pageIndex, Integer pageSize) {
        return sponsorAccountStorage.transactionsOf(sponsorId, pageIndex, pageSize);
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
}
