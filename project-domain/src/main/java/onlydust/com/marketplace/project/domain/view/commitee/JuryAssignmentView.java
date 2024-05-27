package onlydust.com.marketplace.project.domain.view.commitee;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.view.ProjectShortView;
import onlydust.com.marketplace.project.domain.view.RegisteredContributorLinkView;

import java.util.List;

public record JuryAssignmentView(@NonNull Integer totalAssigned, @NonNull Integer completedAssigment, @NonNull RegisteredContributorLinkView user,
                                 @NonNull List<ProjectShortView> projectsAssigned) {
}
