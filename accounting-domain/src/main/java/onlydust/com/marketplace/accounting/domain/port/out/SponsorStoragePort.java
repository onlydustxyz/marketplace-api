package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.Sponsor;

import java.util.Optional;

public interface SponsorStoragePort {
    boolean isAdmin(UserId userId, SponsorId sponsorId);

    void addLeadToSponsor(UserId leadId, SponsorId sponsorId);

    Optional<Sponsor> get(SponsorId sponsorId);
}
