package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
@Immutable
public class ProjectLeadQueryEntity {
    @Id
    @Column(name = "github_user_id", nullable = false)
    Long githubId;
    @Column(name = "id")
    UUID id;
    @Column(name = "login")
    String login;
    @Column(name = "avatar_url")
    String avatarUrl;
    @Column(name = "html_url")
    String htmlUrl;
    @Column(name = "has_accepted_invitation")
    Boolean hasAcceptedInvitation;
}
