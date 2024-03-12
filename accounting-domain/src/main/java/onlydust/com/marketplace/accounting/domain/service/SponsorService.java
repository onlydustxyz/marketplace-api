package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;

@AllArgsConstructor
public class SponsorService implements SponsorFacadePort {
    private final SponsorStoragePort sponsorStoragePort;

    @Override
    public Page<SponsorView> listSponsors(int pageIndex, int pageSize) {
        return sponsorStoragePort.findSponsors(pageIndex, pageSize);
    }
}
