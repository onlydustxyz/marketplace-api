package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.GithubIssue;
import onlydust.com.marketplace.project.domain.view.ContributionDetailsView;

import java.util.List;

public interface ContributionFacadePort {
    ContributionDetailsView getContribution(ProjectId projectId, String contributionId, AuthenticatedUser caller);

    void ignoreContributions(ProjectId projectId, UserId projectLeadId, List<String> contributionIds);

    void unignoreContributions(ProjectId projectId, UserId projectLeadId, List<String> contributionIds);

    void unassign(ProjectId projectId, UserId projectLeadId, String contributionId);
}
