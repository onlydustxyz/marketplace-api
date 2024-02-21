package onlydust.com.marketplace.project.domain.view.backoffice;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProjectLeadInvitationView {
    UUID id;
    UUID projectId;
    Long githubUserId;
}
