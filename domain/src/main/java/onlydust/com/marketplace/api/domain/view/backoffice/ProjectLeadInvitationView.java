package onlydust.com.marketplace.api.domain.view.backoffice;

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
