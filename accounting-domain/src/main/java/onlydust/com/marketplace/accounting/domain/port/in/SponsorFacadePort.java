package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public interface SponsorFacadePort {
    Optional<SponsorView> getSponsor(UUID sponsorId);

    Page<SponsorView> listSponsors(String search, int sanitizedPageIndex, int sanitizedPageSize);

    URL uploadLogo(InputStream imageInputStream);
}
