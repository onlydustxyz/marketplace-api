package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import onlydust.com.marketplace.accounting.domain.model.AccountId;

import java.util.UUID;

public record Vertex(UUID id, AccountId accountId) {
}
