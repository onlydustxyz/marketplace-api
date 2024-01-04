package onlydust.com.marketplace.accounting.domain.port.in;

import java.math.BigDecimal;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

public interface AccountingFacadePort {

  void registerTransferFromSponsor(SponsorId sponsorId, CommitteeId committeeId, BigDecimal amount, Currency currency,
      Network network);

  void allocateFundsToProject(CommitteeId committeeId, ProjectId projectId, BigDecimal amount, Currency currency);

  void registerRefundToSponsor(SponsorId sponsorId, BigDecimal one, Currency currency);
}
