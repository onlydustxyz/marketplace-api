package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;

public class AccountBook {

    private final AccountBookState state = new AccountBookState();


    public void mint(Account.Id account, PositiveAmount amount) {
        state.mint(account, amount);
    }

    public void burn(Account.Id account, PositiveAmount amount) {
        state.burn(account, amount);
    }

    public Amount balanceOf(Account.Id account) {
        return state.balanceOf(account);
    }

    public void transfer(Account.Id from, Account.Id to, PositiveAmount amount) {
        state.transfer(from, to, amount);
    }

    public void refund(Account.Id from, Account.Id to, PositiveAmount amount) {
        state.refund(from, to, amount);
    }
}
