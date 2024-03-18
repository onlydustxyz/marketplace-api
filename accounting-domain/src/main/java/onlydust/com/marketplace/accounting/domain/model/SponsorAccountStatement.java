package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;

import java.util.Map;

@AllArgsConstructor
public class SponsorAccountStatement {
    private final @NonNull SponsorAccount sponsorAccount;
    private final @NonNull AccountBookFacade accountBookFacade;

    public SponsorAccount account() {
        return sponsorAccount;
    }

    public PositiveAmount allowance() {
        return accountBookFacade.balanceOf(sponsorAccount.id());
    }

    public PositiveAmount initialAllowance() {
        return accountBookFacade.initialBalanceOf(sponsorAccount.id());
    }

    public PositiveAmount awaitingPaymentAmount() {
        return awaitingPayments().values().stream().reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    public Map<RewardId, PositiveAmount> awaitingPayments() {
        return accountBookFacade.unpaidRewards(sponsorAccount.id());
    }

    public AccountBookFacade accountBookFacade() {
        return accountBookFacade;
    }

    public Amount debt() {
        return initialAllowance().subtract(account().initialBalance());
    }
}
