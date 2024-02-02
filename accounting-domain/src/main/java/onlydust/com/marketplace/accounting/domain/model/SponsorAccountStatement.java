package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookState;

import java.util.Map;

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
        return accountBookState.unspentChildren(AccountBook.AccountId.of(sponsorAccount.id())).entrySet().stream()
                .filter(e -> e.getKey().isReward())
                .map(Map.Entry::getValue)
                .reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }
}
