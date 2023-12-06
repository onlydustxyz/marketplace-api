package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

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
