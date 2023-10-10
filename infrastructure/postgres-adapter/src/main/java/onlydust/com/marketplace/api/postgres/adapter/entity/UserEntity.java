package onlydust.com.marketplace.api.postgres.adapter.entity;


import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "users", schema = "iam")
public class UserEntity {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "github_user_id", nullable = false)
    Long githubUserId;
    @Column(name = "github_login", nullable = false)
    String githubLogin;
    @Column(name = "github_avatar_url", nullable = false)
    String githubAvatarUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
