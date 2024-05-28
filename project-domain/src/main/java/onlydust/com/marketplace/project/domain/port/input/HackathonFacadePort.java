package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface HackathonFacadePort {
    HackathonDetailsView createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate);

    void updateHackathon(@NonNull Hackathon hackathon);

    void updateHackathonStatus(@NonNull UUID hackathonId, @NonNull Hackathon.Status status);

    HackathonDetailsView getHackathonById(@NonNull Hackathon.Id hackathonId);

    HackathonDetailsView getHackathonBySlug(String hackathonSlug);

    Page<HackathonShortView> getHackathons(int sanitizedPageIndex, int sanitizedPageSize, Set<Hackathon.Status> statuses);

    List<HackathonShortView> getAllPublishedHackathons();

    void deleteHackathon(Hackathon.Id hackathonId);

    void registerToHackathon(UUID userId, Hackathon.Id hackathonId);

    boolean isRegisteredToHackathon(UUID userId, Hackathon.Id hackathonId);

}
