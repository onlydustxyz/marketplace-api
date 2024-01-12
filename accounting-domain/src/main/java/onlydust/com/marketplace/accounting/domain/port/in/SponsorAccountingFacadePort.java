package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.PositiveMoney;

public interface SponsorAccountingFacadePort {

    void registerTransfer(SponsorId sponsorId, Money money);

    void fundCommittee(SponsorId sponsorId, CommitteeId committeeId, PositiveMoney amount);

    void refundFromCommittee(CommitteeId committeeId, SponsorId sponsorId, PositiveMoney amount);
}
