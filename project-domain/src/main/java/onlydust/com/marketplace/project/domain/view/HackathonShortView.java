package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Hackathon;

import java.time.ZonedDateTime;

public record HackathonShortView(@NonNull Hackathon.Id id,
                                 @NonNull String slug,
                                 @NonNull Hackathon.Status status,
                                 @NonNull String title,
                                 String location,
                                 @NonNull ZonedDateTime startDate,
                                 @NonNull ZonedDateTime endDate) {

}
