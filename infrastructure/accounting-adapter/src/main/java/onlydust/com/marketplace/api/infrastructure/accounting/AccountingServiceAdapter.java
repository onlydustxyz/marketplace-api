package onlydust.com.marketplace.api.infrastructure.accounting;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.api.domain.port.output.AccountingServicePort;

@AllArgsConstructor
public class AccountingServiceAdapter implements AccountingServicePort {
    private final AccountingFacadePort accountingFacadePort;
}
