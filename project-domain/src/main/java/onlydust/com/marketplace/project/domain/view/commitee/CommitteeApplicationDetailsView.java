package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.ProjectAnswerView;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;

import java.util.List;

public record CommitteeApplicationDetailsView(@NonNull List<ProjectAnswerView> answers,
                                              @NonNull ProjectShortView projectShortView, @NonNull Boolean hasStartedApplication,
                                              List<ProjectJuryVoteView> projectJuryVoteViews
) {
}
