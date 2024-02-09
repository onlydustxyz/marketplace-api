package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookState;

import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SponsorAccountStatement {
    private final @NonNull SponsorAccount sponsorAccount;
    private final @NonNull AccountBookState accountBookState;

    public SponsorAccount account() {
        return sponsorAccount;
    }

    public PositiveAmount allowance() {
        return accountBookState.balanceOf(AccountBook.AccountId.of(sponsorAccount.id()));
    }

    public PositiveAmount awaitingPaymentAmount() {
        return awaitingPayments().values().stream().reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    public Map<RewardId, PositiveAmount> awaitingPayments() {
        return accountBookState.unspentChildren(AccountBook.AccountId.of(sponsorAccount.id()))
                .entrySet().stream()
                .filter(e -> e.getKey().isReward())
                .map(e -> Map.entry(e.getKey().rewardId(), e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public AccountBookState accountBookState() {
        return accountBookState;
    }
}
