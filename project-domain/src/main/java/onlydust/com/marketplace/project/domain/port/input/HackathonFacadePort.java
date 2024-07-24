package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;

public interface HackathonFacadePort {
    void createHackathon(@NonNull String title, @NonNull Collection<String> githubLabels, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate);

    void updateHackathon(@NonNull Hackathon hackathon);

    void updateHackathonStatus(@NonNull Hackathon.Id hackathonId, @NonNull Hackathon.Status status);

    void deleteHackathon(Hackathon.Id hackathonId);

    void registerToHackathon(UUID userId, Hackathon.Id hackathonId);

}
