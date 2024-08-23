package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.util.Optional;
import java.util.UUID;

public interface SponsorStoragePort {
    boolean isAdmin(UUID userId, Sponsor.Id sponsorId);

    void addLeadToSponsor(UUID leadId, Sponsor.Id sponsorId);

    Optional<Sponsor> get(Sponsor.Id sponsorId);

    void save(Sponsor sponsor);
}
