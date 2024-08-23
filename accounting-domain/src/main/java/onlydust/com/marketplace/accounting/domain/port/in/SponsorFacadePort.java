package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.Sponsor;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

public interface SponsorFacadePort {
    URL uploadLogo(InputStream imageInputStream);

    void addLeadToSponsor(UserId leadId, SponsorId sponsorId);

    Optional<Sponsor> findById(UserId leadId, SponsorId sponsorId);
}
