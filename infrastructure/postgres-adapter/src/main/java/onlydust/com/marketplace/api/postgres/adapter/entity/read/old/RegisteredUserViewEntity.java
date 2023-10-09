package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "registered_users", schema = "public")
public class RegisteredUserViewEntity {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "github_user_id", nullable = false)
    Long githubId;
    @Column(name = "login", nullable = false)
    String login;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "html_url", nullable = false)
    String htmlUrl;
    @Column(name = "email")
    String bio;
    @Column(name = "last_seen")
    LocalDateTime lastSeen;
    @Column(name = "admin")
    Boolean admin;
}
