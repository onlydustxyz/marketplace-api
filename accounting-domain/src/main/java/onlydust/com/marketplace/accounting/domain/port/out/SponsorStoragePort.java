package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Optional;

public interface SponsorStoragePort {
    Page<SponsorView> findSponsors(int pageIndex, int pageSize);

    Optional<SponsorView> get(SponsorId sponsorId);
}
