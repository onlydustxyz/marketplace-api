package onlydust.com.marketplace.api.postgres.adapter.entity.write.old;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Builder
@Table(name = "auth_users", schema = "public")
public class AuthUserEntity {

    @Id
    @Column(name = "id", nullable = false)
    UUID id;
    @Column(name = "github_user_id")
    Integer githubUserId;
    @Column(name = "email")
    String email;
    @Column(name = "last_seen")
    Date lastSeen;
    @Column(name = "login_at_signup", nullable = false)
    String loginAtSignup;
    @Column(name = "avatar_url_at_signup")
    String avatarUrlAtSignup;
    @Column(name = "created_at", nullable = false)
    Date createdAt;
    @Column(name = "admin", nullable = false)
    Boolean isAdmin;
}
