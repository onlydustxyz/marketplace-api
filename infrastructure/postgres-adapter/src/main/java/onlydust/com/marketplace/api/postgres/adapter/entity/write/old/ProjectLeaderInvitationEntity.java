package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "project_id", nullable = false, updatable = false)
    UUID projectId;
    @Column(name = "github_user_id", nullable = false, updatable = false)
    Long githubUserId;
}
