package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.util.UUID;

public interface CommitteeObserverPort {
    void onNewApplication(@NonNull Committee.Id committeeId, @NonNull UUID projectId, @NonNull UUID userId);
}
