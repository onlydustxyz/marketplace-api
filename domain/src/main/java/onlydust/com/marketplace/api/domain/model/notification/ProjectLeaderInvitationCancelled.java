package onlydust.com.marketplace.api.domain.model.notification;

import java.util.Date;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectLeaderInvitationCancelled extends Event {

  UUID projectId;
  Long githubUserId;
  Date cancelledAt;
}
