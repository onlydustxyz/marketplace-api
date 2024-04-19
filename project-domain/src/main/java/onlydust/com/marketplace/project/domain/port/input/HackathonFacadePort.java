package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.HackathonShortView;

import java.time.ZonedDateTime;
import java.util.Set;

public interface HackathonFacadePort {
    HackathonDetailsView createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate);

    void updateHackathon(@NonNull Hackathon hackathon);

    HackathonDetailsView getHackathonById(@NonNull Hackathon.Id hackathonId);

    Page<HackathonShortView> getHackathons(int sanitizedPageIndex, int sanitizedPageSize, Set<Hackathon.Status> statuses);
}
