package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.Optional;
import java.util.UUID;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);

    void saveStatus(@NonNull Hackathon.Id hackathonId, @NonNull Hackathon.Status status);

    Optional<Hackathon> findById(@NonNull Hackathon.Id id);

    boolean exists(Hackathon.Id id);

    void delete(Hackathon.Id hackathonId);

    void registerUser(UUID userId, Hackathon.Id hackathonId);

}
