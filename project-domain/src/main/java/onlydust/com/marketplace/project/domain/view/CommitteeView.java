package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record CommitteeView(@NonNull Committee.Id id, @NonNull String name, @NonNull ZonedDateTime startDate, @NonNull ZonedDateTime endDate,
                            @NonNull Committee.Status status, ShortSponsorView sponsor, List<Committee.ProjectQuestion> projectQuestions) {
}
