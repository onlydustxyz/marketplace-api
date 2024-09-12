package onlydust.com.marketplace.project.domain.port.output;

import onlydust.com.marketplace.kernel.model.ProgramId;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import java.util.List;
import java.util.Optional;

public interface SponsorStoragePort {
    boolean isAdmin(UserId userId, SponsorId sponsorId);

    Optional<Sponsor> get(SponsorId sponsorId);

    List<UserId> findSponsorLeads(SponsorId sponsorId);

    void save(Sponsor sponsor);

    boolean isAdminOfProgramSponsor(UserId userId, ProgramId programId);
}
