package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import onlydust.com.marketplace.accounting.domain.model.Account;

import java.util.UUID;

public record Vertex(UUID id, Account.Id accountId) {
}
