package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.SponsorView;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

import java.io.InputStream;
import java.net.URL;

@AllArgsConstructor
public class SponsorService implements SponsorFacadePort {
    private final SponsorStoragePort sponsorStoragePort;
    private final ImageStoragePort imageStoragePort;

    @Override
    public SponsorView getSponsor(UserId userId, SponsorId sponsorId) {
        if (!sponsorStoragePort.isAdmin(userId, sponsorId))
            throw OnlyDustException.forbidden("User %s is not admin of sponsor %s".formatted(userId, sponsorId));

        return sponsorStoragePort.get(sponsorId)
                .orElseThrow(() -> OnlyDustException.notFound("Sponsor %s not found".formatted(sponsorId)));
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
