package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;

@AllArgsConstructor(staticName = "of")
@Getter
@Accessors(fluent = true)
public class Vertex {
    private final AccountBook.AccountId accountId;
}
