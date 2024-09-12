package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Committee;

public interface CommitteeObserverPort {
    void onNewApplication(@NonNull Committee.Id committeeId, @NonNull ProjectId projectId, @NonNull UserId userId);
}
