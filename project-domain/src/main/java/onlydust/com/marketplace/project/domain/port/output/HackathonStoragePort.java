package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

import java.util.Optional;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);

    Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id);
}
