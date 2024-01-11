package onlydust.com.marketplace.accounting.domain.model;

import lombok.ToString;

@ToString
public final class TxVertex {
    private final String accountId;

    public TxVertex(String accountId) {
        this.accountId = accountId;
    }

    public String accountId() {
        return accountId;
    }
}
