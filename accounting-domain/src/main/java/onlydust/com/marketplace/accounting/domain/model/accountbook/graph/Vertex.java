package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;

public record Vertex(long id, AccountBook.AccountId accountId) {
    public boolean equals(Vertex other) {
        return id == other.id;
    }
}
