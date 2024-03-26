package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.io.InputStream;
import java.net.URL;

public interface SponsorFacadePort {
    Page<SponsorView> listSponsors(int sanitizedPageIndex, int sanitizedPageSize);

    URL uploadLogo(InputStream imageInputStream);
}
