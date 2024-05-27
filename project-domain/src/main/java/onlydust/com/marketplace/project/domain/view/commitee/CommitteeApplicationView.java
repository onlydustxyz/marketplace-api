package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.ProjectInfosView;

import java.time.ZonedDateTime;
import java.util.List;

public record CommitteeApplicationView(@NonNull Committee.Status status, @NonNull List<ProjectAnswerView> answers,
                                       ProjectInfosView projectInfosView, @NonNull Boolean hasStartedApplication, @NonNull ZonedDateTime applicationStartDate,
                                       @NonNull ZonedDateTime applicationEndDate) {
}
