package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import onlydust.com.marketplace.accounting.domain.model.Ledger;

import java.util.UUID;

public record Vertex(UUID id, Ledger.Id accountId) {
}
