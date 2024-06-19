package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Builder
@Table(name = "pending_project_leader_invitations", schema = "public")
public class ProjectLeaderInvitationEntity {

    @Id
    private UUID id;
    UUID projectId;
    Long githubUserId;
}
