package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.model.SponsorId;

import java.util.Optional;

public interface AccountingSponsorStoragePort {

    Optional<SponsorView> getView(SponsorId sponsorId);
}
