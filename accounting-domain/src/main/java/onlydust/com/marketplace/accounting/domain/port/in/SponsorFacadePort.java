package onlydust.com.marketplace.accounting.domain.port.in;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

public interface SponsorFacadePort {
    Page<SponsorView> listSponsors(int sanitizedPageIndex, int sanitizedPageSize);
}
