package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.time.ZonedDateTime;

public interface CommitteeFacadePort {
    Committee createCommittee(@NonNull String name,
                              @NonNull ZonedDateTime startDate,
                              @NonNull ZonedDateTime endDate);
}
