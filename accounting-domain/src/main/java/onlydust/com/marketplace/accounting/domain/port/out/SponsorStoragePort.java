package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

import java.util.Optional;

public interface SponsorStoragePort {
    Page<SponsorView> findSponsors(String search, int pageIndex, int pageSize);

    Optional<SponsorView> get(SponsorId sponsorId);

    boolean isAdmin(UserId userId, SponsorId sponsorId);
}
