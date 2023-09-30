package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Entity
public class UserProfileEntity {
    @Id
    @Column(name = "row_number", nullable = false)
    Integer rowNumber;
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "github_user_id", nullable = false)
    Integer githubId;
    @Column(name = "bio", nullable = false)
    String bio;
    @Column(name = "avatar_url", nullable = false)
    String avatarUrl;
    @Column(name = "login", nullable = false)
    String login;
}
