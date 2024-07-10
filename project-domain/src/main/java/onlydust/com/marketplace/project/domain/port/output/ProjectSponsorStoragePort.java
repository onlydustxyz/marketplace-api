package onlydust.com.marketplace.project.domain.port.output;

import java.util.UUID;

public interface ProjectSponsorStoragePort {
    boolean isUserSponsorAdmin(UUID id, UUID sponsorId);
}
