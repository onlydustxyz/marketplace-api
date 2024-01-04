package onlydust.com.marketplace.api.domain.view.backoffice;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectLeadInvitationView {

  UUID id;
  UUID projectId;
  Long githubUserId;
}
