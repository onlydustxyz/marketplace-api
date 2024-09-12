package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.ProjectId;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

public record ProjectInfosView(@NonNull ProjectId projectId, @NonNull String name, @NonNull String slug, URI logoUri,
                               @NonNull String shortDescription, @NonNull String longDescription, @NonNull List<ProjectLeaderLinkView> projectLeads,
                               @NonNull Integer activeContributors, @NonNull BigDecimal amountSentInUsd, @NonNull Integer contributorsRewarded,
                               @NonNull Integer contributionsCompleted, @NonNull Integer newContributors, @NonNull Integer openIssue) {
}
