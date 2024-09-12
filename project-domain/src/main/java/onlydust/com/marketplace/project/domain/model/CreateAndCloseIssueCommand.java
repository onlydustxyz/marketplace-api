package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.UserId;

@Data
@Builder(toBuilder = true)
public class CreateAndCloseIssueCommand {
    ProjectId projectId;
    UserId projectLeadId;
    Long githubRepoId;
    String title;
    String description;
}
