package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

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
