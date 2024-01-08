package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.CommitteeId;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;

public interface SponsorAccountingFacadePort {

    void registerTransfer(SponsorId sponsorId, Amount amount);

    void fundCommittee(SponsorId sponsorId, CommitteeId committeeId, PositiveAmount amount);

    void refundFromCommittee(CommitteeId committeeId, SponsorId sponsorId, PositiveAmount amount);
}
