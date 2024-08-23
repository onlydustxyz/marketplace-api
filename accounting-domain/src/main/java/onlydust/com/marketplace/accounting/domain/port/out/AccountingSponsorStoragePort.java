package onlydust.com.marketplace.accounting.domain.port.out;

import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;

import java.util.Optional;

public interface AccountingSponsorStoragePort {

    Optional<SponsorView> get(SponsorId sponsorId);
}
