package onlydust.com.marketplace.accounting.domain.model.accountbook;

import onlydust.com.marketplace.accounting.domain.AccountBookState;
import onlydust.com.marketplace.kernel.visitor.Visitor;

public interface AccountBookEvent extends Visitor<AccountBookState> {
}
