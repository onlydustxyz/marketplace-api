package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

public interface SponsorStoragePort {
    Page<SponsorView> findSponsors(int pageIndex, int pageSize);
}
