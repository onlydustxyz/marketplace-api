package onlydust.com.marketplace.accounting.domain.model.accountbook;

import onlydust.com.marketplace.kernel.visitor.Visitor;

public interface AccountBookEvent<R> extends Visitor<AccountBookState, R> {
}
