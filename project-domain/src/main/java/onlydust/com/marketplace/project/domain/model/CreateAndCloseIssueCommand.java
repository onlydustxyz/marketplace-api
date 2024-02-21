package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class CreateAndCloseIssueCommand {
    UUID projectId;
    UUID projectLeadId;
    Long githubRepoId;
    String title;
    String description;
}
