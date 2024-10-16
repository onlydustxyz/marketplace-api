package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.service.AccountBookFacade;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.Map;

@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SponsorAccountStatement {
    @EqualsAndHashCode.Include
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

    public PositiveAmount unspentBalanceSentTo(ProjectId projectId) {
        return accountBookFacade.unspentBalanceReceivedFrom(sponsorAccount.id(), projectId);
    }
}
