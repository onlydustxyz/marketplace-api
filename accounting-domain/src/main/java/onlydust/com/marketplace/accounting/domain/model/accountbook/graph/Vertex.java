package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;

import java.util.UUID;

public record Vertex(UUID id, AccountBook.AccountId accountId) {
}
