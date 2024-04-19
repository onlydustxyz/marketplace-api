package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;

import java.util.Optional;
import java.util.Set;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);

    Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id);

    boolean exists(Hackathon.Id id);

    Page<HackathonShortView> findByStatuses(int pageIndex, int pageSize, Set<Hackathon.Status> statuses);
}
