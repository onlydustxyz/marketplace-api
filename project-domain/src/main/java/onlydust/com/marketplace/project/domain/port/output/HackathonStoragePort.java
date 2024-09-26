package onlydust.com.marketplace.project.domain.port.output;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.util.Optional;

public interface HackathonStoragePort {
    void save(@NonNull Hackathon hackathon);

    void saveStatus(@NonNull Hackathon.Id hackathonId, @NonNull Hackathon.Status status);

    Optional<Hackathon> findById(@NonNull Hackathon.Id id);

    boolean exists(Hackathon.Id id);

    boolean hasUserRegistered(Hackathon.Id hackathonId);

    void delete(Hackathon.Id hackathonId);

    void registerUser(UserId userId, Hackathon.Id hackathonId);
}
