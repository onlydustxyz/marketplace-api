package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountingFacadePort {

    void registerTransferFromSponsor(UUID sponsorId, UUID committeeId, BigDecimal amount, Currency currency,
                                     Network network);

    void allocateFundsToProject(UUID committeeId, UUID projectId, BigDecimal amount, Currency currency);

    void registerRefundToSponsor(UUID sponsorId, BigDecimal one, Currency currency);
}
