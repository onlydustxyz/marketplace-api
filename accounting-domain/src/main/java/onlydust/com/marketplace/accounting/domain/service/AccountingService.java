package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookState;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class AccountingService implements AccountingFacadePort {
    private final AccountBookEventStorage accountBookEventStorage;
    private final SponsorAccountStorage sponsorAccountStorage;
    private final CurrencyStorage currencyStorage;
    private final AccountingObserver accountingObserver;
    private final ProjectAccountingObserver projectAccountingObserver;

    @Override
    public SponsorAccountStatement createSponsorAccount(@NonNull SponsorId sponsorId,
                                                        Currency.@NonNull Id currencyId,
                                                        @NonNull PositiveAmount allowance,
                                                        ZonedDateTime lockedUntil) {
        final var currency = getCurrency(currencyId);
        final var sponsorAccount = new SponsorAccount(sponsorId, currency, lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);

        increaseAllowance(sponsorAccount.id(), allowance);
        return sponsorAccountStatement(sponsorAccount, getAccountBook(currency));
    }

    @Override
    public SponsorAccountStatement createSponsorAccount(@NonNull SponsorId sponsorId,
                                                        Currency.@NonNull Id currencyId,
                                                        @NonNull PositiveAmount allowance,
                                                        ZonedDateTime lockedUntil,
                                                        @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = createSponsorAccount(sponsorId, currencyId, allowance, lockedUntil);
        return fund(sponsorAccount.account().id(), transaction);
    }

    @Override
    public SponsorAccountStatement increaseAllowance(SponsorAccount.Id sponsorAccountId, Amount amount) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBook = getAccountBook(sponsorAccount.currency());

        if (amount.isPositive())
            accountBook.mint(AccountId.of(sponsorAccountId), PositiveAmount.of(amount));
        else
            accountBook.burn(AccountId.of(sponsorAccountId), PositiveAmount.of(amount.negate()));

        accountBookEventStorage.save(sponsorAccount.currency(), accountBook.pendingEvents());
        onAllowanceUpdated(sponsorAccount.id(), sponsorAccount.currency(), accountBook.state());
        return sponsorAccountStatement(sponsorAccount, accountBook);
    }

    @Override
    public SponsorAccountStatement fund(@NonNull SponsorAccount.Id sponsorAccountId, @NonNull SponsorAccount.Transaction transaction) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        final var accountBook = getAccountBook(sponsorAccount.currency());
        return registerSponsorAccountTransaction(accountBook, sponsorAccount, transaction);
    }


    @Override
    public void pay(final @NonNull RewardId rewardId,
                    final @NonNull Currency.Id currencyId,
                    final @NonNull SponsorAccount.PaymentReference paymentReference) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).forEach((sponsorAccountId, amount) -> {
            final var sponsorAccount = sponsorAccountStorage.get(sponsorAccountId.sponsorAccountId()).orElseThrow();
            final var sponsorAccountNetwork = sponsorAccount.network().orElseThrow(
                    () -> internalServerError("Sponsor account %s is not funded".formatted(sponsorAccountId.sponsorAccountId()))
            );

            if (paymentReference.network().equals(sponsorAccountNetwork)) {
                accountBook.burn(AccountId.of(rewardId), amount);
                registerSponsorAccountTransaction(accountBook, sponsorAccount, new SponsorAccount.Transaction(paymentReference, amount.negate()));
            }
        });

        accountBookEventStorage.save(currency, accountBook.pendingEvents());
        accountingObserver.onRewardPaid(rewardId);
    }

    @Override
    public void cancel(@NonNull RewardId rewardId, @NonNull Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.refund(AccountId.of(rewardId));
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
        accountingObserver.onRewardCancelled(rewardId);
    }

    @Override
    public boolean isPayable(RewardId rewardId, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        return accountBook.state().transferredAmountPerOrigin(AccountId.of(rewardId)).entrySet().stream()
                .allMatch(entry -> {
                    final var sponsorAccount = sponsorAccountStorage.get(entry.getKey().sponsorAccountId()).orElseThrow();
                    return sponsorAccount.unlockedBalance().isGreaterThanOrEqual(entry.getValue());
                });
    }

    @Override
    public <From, To> void transfer(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.transfer(AccountId.of(from), AccountId.of(to), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());

        if (to instanceof RewardId rewardId)
            accountingObserver.onRewardCreated(rewardId, new AccountBookFacade(sponsorAccountStorage, accountBook));
        onAllowanceUpdated(from, currency, accountBook.state());
        onAllowanceUpdated(to, currency, accountBook.state());
    }


    @Override
    public <From, To> void refund(From from, To to, PositiveAmount amount, Currency.Id currencyId) {
        final var currency = getCurrency(currencyId);
        final var accountBook = getAccountBook(currency);

        accountBook.refund(AccountId.of(from), AccountId.of(to), amount);
        accountBookEventStorage.save(currency, accountBook.pendingEvents());
        onAllowanceUpdated(from, currency, accountBook.state());
        onAllowanceUpdated(to, currency, accountBook.state());
    }

    @Override
    public Optional<SponsorAccountStatement> getSponsorAccountStatement(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId)
                .map(sponsorAccount -> sponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency())));
    }

    @Override
    public Optional<SponsorAccount> getSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId);
    }

    @Override
    public List<SponsorAccountStatement> getSponsorAccounts(SponsorId sponsorId) {
        return sponsorAccountStorage.getSponsorAccounts(sponsorId).stream()
                .map(sponsorAccount -> sponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency())))
                .toList();
    }

    @Override
    public SponsorAccountStatement updateSponsorAccount(SponsorAccount.@NonNull Id sponsorAccountId, ZonedDateTime lockedUntil) {
        final var sponsorAccount = mustGetSponsorAccount(sponsorAccountId);
        sponsorAccount.lockUntil(lockedUntil);
        sponsorAccountStorage.save(sponsorAccount);
        final var sponsorAccountStatement = sponsorAccountStatement(sponsorAccount, getAccountBook(sponsorAccount.currency()));
        accountingObserver.onSponsorAccountUpdated(sponsorAccountStatement);
        return sponsorAccountStatement;
    }

    @Override
    public List<PayableReward> getPayableRewards() {
        return currencyStorage.all().stream()
                .flatMap(currency -> new PayableRewardAggregator(sponsorAccountStorage, currency).getPayableRewards())
                .toList();
    }

    private <ID> void onAllowanceUpdated(ID id, Currency currency, AccountBookState accountBook) {
        if (id instanceof ProjectId projectId)
            projectAccountingObserver.onAllowanceUpdated(
                    projectId,
                    currency.id(),
                    accountBook.balanceOf(AccountId.of(projectId)),
                    accountBook.amountReceivedBy(AccountId.of(projectId))
            );
    }

    private SponsorAccountStatement registerSponsorAccountTransaction(AccountBookAggregate accountBook,
                                                                      SponsorAccount sponsorAccount,
                                                                      SponsorAccount.Transaction transaction) {
        sponsorAccount.add(transaction);
        sponsorAccountStorage.save(sponsorAccount);
        final var statement = sponsorAccountStatement(sponsorAccount, accountBook);
        accountingObserver.onSponsorAccountBalanceChanged(statement);
        return statement;
    }

    private SponsorAccount mustGetSponsorAccount(SponsorAccount.Id sponsorAccountId) {
        return sponsorAccountStorage.get(sponsorAccountId)
                .orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
    }

    private AccountBookAggregate getAccountBook(Currency currency) {
        return AccountBookAggregate.fromEvents(accountBookEventStorage.get(currency));
    }

    public SponsorAccountStatement delete(SponsorAccount.Id sponsorAccountId, SponsorAccount.Transaction.Id transactionId) {
        sponsorAccountStorage.delete(sponsorAccountId, transactionId);
        final var sponsorAccountStatement = getSponsorAccountStatement(sponsorAccountId).orElseThrow();
        accountingObserver.onSponsorAccountBalanceChanged(sponsorAccountStatement);
        return sponsorAccountStatement;
    }

    private Currency getCurrency(Currency.Id id) {
        return currencyStorage.get(id)
                .orElseThrow(() -> notFound("Currency %s not found".formatted(id)));
    }

    private SponsorAccountStatement sponsorAccountStatement(SponsorAccount sponsorAccount, AccountBookAggregate accountBook) {
        return new SponsorAccountStatement(sponsorAccount, new AccountBookFacade(sponsorAccountStorage, accountBook));
    }

    class PayableRewardAggregator {
        private final @NonNull CachedSponsorAccountProvider sponsorAccountProvider;
        private final @NonNull Currency currency;
        private final @NonNull AccountBookAggregate accountBook;

        public PayableRewardAggregator(final @NonNull SponsorAccountProvider sponsorAccountProvider, final @NonNull Currency currency) {
            this.sponsorAccountProvider = new CachedSponsorAccountProvider(sponsorAccountProvider);
            this.currency = currency;
            this.accountBook = getAccountBook(currency);
        }

        public Stream<PayableReward> getPayableRewards() {
            final var distinctPayableRewards = accountBook.state().unspentChildren().keySet().stream()
                    .filter(AccountId::isReward)
                    .filter(rewardAccountId -> isPayable(rewardAccountId.rewardId(), currency.id()))
                    .flatMap(this::trySpend)
                    .collect(groupingBy(PayableReward::key, reducing(PayableReward::add)));

            return distinctPayableRewards.values().stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        }

        private Stream<PayableReward> trySpend(AccountId rewardAccountId) {
            return accountBook.state().transferredAmountPerOrigin(rewardAccountId).entrySet().stream()
                    .filter(e -> e.getValue().isStrictlyPositive())
                    .filter(e -> stillEnoughBalance(e.getKey().sponsorAccountId(), e.getValue()))
                    .peek(e -> spend(e.getKey().sponsorAccountId(), rewardAccountId.rewardId(), e.getValue()))
                    .map(e -> createPayableReward(e.getKey().sponsorAccountId(), rewardAccountId.rewardId(), e.getValue()));
        }

        private void spend(SponsorAccount.Id sponsorAccountId, RewardId rewardId, PositiveAmount amount) {
            final var sponsorAccount = sponsorAccount(sponsorAccountId);

            final var sponsorAccountNetwork = sponsorAccount.network().orElseThrow();
            sponsorAccount.add(new SponsorAccount.Transaction(sponsorAccountNetwork, rewardId.toString(), amount.negate(), "", ""));
        }

        private PayableReward createPayableReward(SponsorAccount.Id sponsorAccountId, RewardId rewardId, PositiveAmount amount) {
            final var sponsorAccountNetwork = sponsorAccount(sponsorAccountId).network().orElseThrow();
            return new PayableReward(rewardId, currency.forNetwork(sponsorAccountNetwork), amount);
        }

        private boolean stillEnoughBalance(SponsorAccount.Id sponsorAccountId, PositiveAmount amount) {
            return sponsorAccount(sponsorAccountId).unlockedBalance().isGreaterThanOrEqual(amount);
        }

        private SponsorAccount sponsorAccount(SponsorAccount.Id sponsorAccountId) {
            return sponsorAccountProvider.get(sponsorAccountId)
                    .orElseThrow(() -> notFound("Sponsor account %s not found".formatted(sponsorAccountId)));
        }
    }
}
