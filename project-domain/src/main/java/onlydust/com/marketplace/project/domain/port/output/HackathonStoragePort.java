package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);

    void saveStatus(@NonNull Hackathon.Id hackathonId, @NonNull Hackathon.Status status);

    Optional<HackathonDetailsView> findById(@NonNull Hackathon.Id id);

    Optional<HackathonDetailsView> findBySlug(String hackathonSlug);

    boolean exists(Hackathon.Id id);

    Page<HackathonShortView> findByStatuses(int pageIndex, int pageSize, Set<Hackathon.Status> statuses);

    void delete(Hackathon.Id hackathonId);

    void registerUser(UUID userId, Hackathon.Id hackathonId);

    boolean isRegisteredToHackathon(UUID userId, Hackathon.Id hackathonId);

}
