package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public interface SponsorFacadePort {
    Sponsor createSponsor(@NonNull String name, URI url, @NonNull URI logoUrl);

    void updateSponsor(@NonNull SponsorId sponsorId, @NonNull String name, URI url, @NonNull URI logoUrl);

    URL uploadLogo(InputStream imageInputStream);

    void addLeadToSponsor(UUID leadId, SponsorId sponsorId);

    Optional<Sponsor> findById(UUID leadId, SponsorId sponsorId);
}
