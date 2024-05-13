package onlydust.com.marketplace.bff.read.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Table(name = "all_users", schema = "iam")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class BffUserViewEntity {
    @Id
    @EqualsAndHashCode.Include
    @NonNull Long githubUserId;

    UUID userId;
    @NonNull String login;
    @NonNull String avatarUrl;
    String email;

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    RegisteredUserViewEntity registered;

    @OneToOne
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull GithubAccountViewEntity github;

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserProfileViewEntity profile;
}
