package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.net.URL;

public interface SponsorFacadePort {
    // TODO: move to read-api
    SponsorView getSponsor(UserId userId, SponsorId sponsorId);

    // TODO: move to read-api
    Page<SponsorView> listSponsors(String search, int sanitizedPageIndex, int sanitizedPageSize);

    URL uploadLogo(InputStream imageInputStream);

    void addLeadToSponsor(UserId leadId, SponsorId sponsorId);
}
