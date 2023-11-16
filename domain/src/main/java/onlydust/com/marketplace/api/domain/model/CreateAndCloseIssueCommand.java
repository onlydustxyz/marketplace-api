package onlydust.com.marketplace.api.domain.model;

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
