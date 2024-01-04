package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
public class ProjectLeadInvitationEntity {

  @Id
  UUID id;
  UUID projectId;
  Long githubUserId;
}
