package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.Committee;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public record CommitteeApplicationView(@NonNull Committee.Status status, @NonNull List<ProjectAnswerView> answers,
                                       ProjectInfosView projectInfosView, @NonNull Boolean hasStartedApplication) {

    public record ProjectInfosView(@NonNull UUID projectId, @NonNull String name, @NonNull String slug, @NonNull URI logoUri,
                                   @NonNull String shortDescription, @NonNull String longDescription, @NonNull List<ProjectLeaderLinkView> projectLeads,
                                   @NonNull Integer activeContributors, @NonNull BigDecimal amountSentInUsd, @NonNull Integer contributorsRewarded,
                                   @NonNull Integer contributionsCompleted, @NonNull Integer newContributors, @NonNull Integer openIssue) {
    }
}
