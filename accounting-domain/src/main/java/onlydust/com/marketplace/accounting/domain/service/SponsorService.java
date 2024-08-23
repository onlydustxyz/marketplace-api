package onlydust.com.marketplace.accounting.domain.service;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.in.SponsorFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorStoragePort;
import onlydust.com.marketplace.accounting.domain.view.Sponsor;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;

@AllArgsConstructor
public class SponsorService implements SponsorFacadePort {
    private final SponsorStoragePort sponsorStoragePort;
    private final ImageStoragePort imageStoragePort;

    @Override
    public URL uploadLogo(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public void addLeadToSponsor(UserId leadId, SponsorId sponsorId) {
        sponsorStoragePort.addLeadToSponsor(leadId, sponsorId);
    }

    @Override
    public Optional<Sponsor> findById(UserId leadId, SponsorId sponsorId) {
        if (!sponsorStoragePort.isAdmin(leadId, sponsorId))
            throw forbidden("User %s is not allowed to access sponsor %s".formatted(leadId, sponsorId));

        return sponsorStoragePort.get(sponsorId);
    }
}
