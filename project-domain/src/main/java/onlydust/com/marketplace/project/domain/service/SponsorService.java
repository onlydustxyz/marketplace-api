package onlydust.com.marketplace.project.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.kernel.port.output.ImageStoragePort;
import onlydust.com.marketplace.project.domain.model.Sponsor;
import onlydust.com.marketplace.project.domain.port.input.SponsorFacadePort;
import onlydust.com.marketplace.project.domain.port.output.SponsorStoragePort;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.forbidden;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class SponsorService implements SponsorFacadePort {
    private final SponsorStoragePort sponsorStoragePort;
    private final ImageStoragePort imageStoragePort;

    @Override
    public Sponsor createSponsor(@NonNull String name, URI url, @NonNull URI logoUrl, @NonNull List<UserId> leads) {
        final var sponsor = Sponsor.create(name, url, logoUrl, leads);
        sponsorStoragePort.save(sponsor);
        return sponsor;
    }

    @Override
    public void updateSponsor(@NonNull SponsorId sponsorId, @NonNull String name, URI url, @NonNull URI logoUrl, @NonNull List<UserId> leads) {
        final var sponsor = sponsorStoragePort.get(sponsorId)
                .orElseThrow(() -> notFound("Sponsor %s not found".formatted(sponsorId)));

        sponsorStoragePort.save(sponsor.toBuilder()
                .name(name)
                .url(url)
                .logoUrl(logoUrl)
                .leads(leads)
                .build());
    }

    @Override
    public URL uploadLogo(InputStream imageInputStream) {
        return imageStoragePort.storeImage(imageInputStream);
    }

    @Override
    public Optional<Sponsor> findById(UserId leadId, SponsorId sponsorId) {
        if (!sponsorStoragePort.isAdmin(leadId, sponsorId))
            throw forbidden("User %s is not allowed to access sponsor %s".formatted(leadId, sponsorId));

        return sponsorStoragePort.get(sponsorId);
    }
}
