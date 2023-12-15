package onlydust.com.marketplace.api.postgres.adapter.entity.read.old;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OnboardingEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserProfileInfoEntity;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Table(name = "registered_users", schema = "public")
@Immutable
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
    String email;
    @Column(name = "admin")
    Boolean admin;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private OnboardingEntity onboarding;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "id", insertable = false, updatable = false)
    private UserProfileInfoEntity profile;
}
