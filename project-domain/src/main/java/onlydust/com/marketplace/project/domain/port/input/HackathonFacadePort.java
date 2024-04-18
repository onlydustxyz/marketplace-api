package onlydust.com.marketplace.project.domain.port.input;

import lombok.NonNull;

import java.time.ZonedDateTime;

public interface HackathonFacadePort {
    void createHackathon(@NonNull String title, @NonNull String subtitle, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate);
}
