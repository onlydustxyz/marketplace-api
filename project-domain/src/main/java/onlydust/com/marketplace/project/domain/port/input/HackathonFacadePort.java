package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;

import java.time.ZonedDateTime;

public interface HackathonFacadePort {
    HackathonDetailsView createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate);

    HackathonDetailsView getHackathonById(@NonNull Hackathon.Id hackathonId);
}
