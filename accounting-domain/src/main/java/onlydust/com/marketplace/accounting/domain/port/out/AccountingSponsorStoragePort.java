package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;
import java.util.Optional;

public interface AccountingSponsorStoragePort {

    Optional<SponsorView> getView(SponsorId sponsorId);

    List<UserId> findSponsorLeads(SponsorId sponsorId);
}
