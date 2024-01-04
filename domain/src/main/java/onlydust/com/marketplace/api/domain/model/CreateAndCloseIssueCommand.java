package onlydust.com.marketplace.api.domain.model;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class CreateAndCloseIssueCommand {

  UUID projectId;
  UUID projectLeadId;
  Long githubRepoId;
  String title;
  String description;
}
