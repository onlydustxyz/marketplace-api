package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.*;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountingFacadePort {

    void registerTransferFromSponsor(SponsorId sponsorId, CommitteeId committeeId, BigDecimal amount, Currency currency,
                                     Network network);

    void allocateFundsToProject(CommitteeId committeeId, ProjectId projectId, BigDecimal amount, Currency currency);

    void registerRefundToSponsor(SponsorId sponsorId, BigDecimal one, Currency currency);
}
