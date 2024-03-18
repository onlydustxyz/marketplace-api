package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
public class AccountBookFacade {

    private final SponsorAccountStorage sponsorAccountStorage;
    private final AccountBookAggregate accountBook;

    public <T> PositiveAmount balanceOf(T ownerId) {
        return accountBook.state().balanceOf(AccountBook.AccountId.of(ownerId));
    }

    public <T> PositiveAmount initialBalanceOf(T ownerId) {
        return accountBook.state().amountReceivedBy(AccountBook.AccountId.of(ownerId));
    }

    public Map<RewardId, PositiveAmount> unpaidRewards(SponsorAccount.Id sponsorAccountId) {
        return accountBook.state().unspentChildren(AccountBook.AccountId.of(sponsorAccountId)).entrySet().stream()
                .filter(e -> e.getKey().isReward())
                .collect(Collectors.toUnmodifiableMap(e -> e.getKey().rewardId(), Map.Entry::getValue));
    }


    public boolean isFunded(RewardId rewardId) {
        return accountBook.state().transferredAmountPerOrigin(AccountBook.AccountId.of(rewardId)).entrySet().stream()
                .allMatch(entry -> {
                    final var sponsorAccount = sponsorAccountStorage.get(entry.getKey().sponsorAccountId()).orElseThrow();
                    return sponsorAccount.balance().isGreaterThanOrEqual(entry.getValue());
                });
    }

    public Set<Network> networksOf(RewardId rewardId) {
        return accountBook.state().transferredAmountPerOrigin(AccountBook.AccountId.of(rewardId)).keySet().stream()
                .map(accountId -> sponsorAccountStorage.get(accountId.sponsorAccountId()).orElseThrow().network())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableSet());
    }

    public Optional<Instant> unlockDateOf(RewardId rewardId) {
        return accountBook.state().transferredAmountPerOrigin(AccountBook.AccountId.of(rewardId)).keySet().stream()
                .map(accountId -> sponsorAccountStorage.get(accountId.sponsorAccountId()).orElseThrow().lockedUntil())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Instant::compareTo);
    }

    public PositiveAmount transferredAmount(SponsorAccount.Id sponsorAccountId, ProjectId projectId) {
        return accountBook.state().transferredAmount(AccountBook.AccountId.of(sponsorAccountId), AccountBook.AccountId.of(projectId));
    }
}
