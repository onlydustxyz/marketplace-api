package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class SponsorService implements SponsorFacadePort {
    private final SponsorStoragePort sponsorStoragePort;
    private final ImageStoragePort imageStoragePort;

    @Override
    public Optional<SponsorView> getSponsor(UUID sponsorId) {
        return sponsorStoragePort.get(SponsorId.of(sponsorId));
    }

    @Override
    public Page<SponsorView> listSponsors(String search, int pageIndex, int pageSize) {
        return sponsorStoragePort.findSponsors(search, pageIndex, pageSize);
    }

    @Override
    public URL uploadLogo(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }
}
