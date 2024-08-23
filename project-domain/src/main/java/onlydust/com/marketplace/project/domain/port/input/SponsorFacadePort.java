package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

public interface SponsorFacadePort {
    Sponsor createSponsor(@NonNull String name, URI url, @NonNull URI logoUrl);

    void updateSponsor(@NonNull Sponsor.Id sponsorId, @NonNull String name, URI url, @NonNull URI logoUrl);

    URL uploadLogo(InputStream imageInputStream);

    void addLeadToSponsor(UUID leadId, Sponsor.Id sponsorId);

    Optional<Sponsor> findById(UUID leadId, Sponsor.Id sponsorId);
}
