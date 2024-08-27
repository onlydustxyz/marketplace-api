package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.util.Optional;
import java.util.UUID;

public interface SponsorStoragePort {
    boolean isAdmin(UUID userId, SponsorId sponsorId);

    void addLeadToSponsor(UUID leadId, SponsorId sponsorId);

    Optional<Sponsor> get(SponsorId sponsorId);

    void save(Sponsor sponsor);

    boolean isAdminOfAnySponsor(UUID userId);

    boolean isAdminOfProgramSponsor(UUID userId, ProgramId programId);
}
