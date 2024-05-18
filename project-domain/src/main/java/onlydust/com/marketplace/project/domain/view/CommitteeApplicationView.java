package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.math.BigDecimal;
import java.util.List;

public record CommitteeApplicationView(@NonNull Committee.Status status, @NonNull List<Committee.ProjectAnswer> answers,
                                       ProjectInfosView projectInfosView) {

    public record ProjectInfosView(@NonNull String shortDescription, @NonNull String longDescription, @NonNull List<ProjectLeaderLinkView> projectLeads,
                                   @NonNull Integer activeContributors, @NonNull BigDecimal amountSentInUsd, @NonNull Integer contributorsRewarded,
                                   @NonNull Integer contributionsCompleted, @NonNull Integer newContributors, @NonNull Integer openIssue) {
    }
}
