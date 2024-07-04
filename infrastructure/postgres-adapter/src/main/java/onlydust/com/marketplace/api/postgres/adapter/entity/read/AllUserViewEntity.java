package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountViewEntity;
import onlydust.com.marketplace.kernel.model.github.GithubUserIdentity;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Table(name = "all_users", schema = "iam")
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Immutable
public class AllUserViewEntity {
    @Id
    @NonNull
    @EqualsAndHashCode.Include
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    String email;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserViewEntity registered;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull
    GithubAccountViewEntity github;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserProfileInfoViewEntity profile;

    public Boolean isRegistered() {
        return userId != null;
    }

    public ShortContributorView toDomain() {
        return new ShortContributorView(GithubUserId.of(githubUserId), login, avatarUrl, isNull(userId) ? null : UserId.of(userId), email);
    }

    public GithubUserIdentity toGithubIdentity() {
        return GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .githubLogin(login)
                .githubAvatarUrl(avatarUrl)
                .email(email)
                .build();
    }
}
