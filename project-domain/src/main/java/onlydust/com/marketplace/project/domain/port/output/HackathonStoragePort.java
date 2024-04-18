package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);
}
