package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAccountEntity;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

import static java.util.Objects.isNull;

@Entity
@Value
@Table(name = "all_users", schema = "iam")
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
@Immutable
public class AllUserViewEntity {
    @Id
    @NonNull
    Long githubUserId;
    UUID userId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;

    String email;


    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserViewEntity registered;

    @OneToOne
    @JoinColumn(name = "githubUserId", insertable = false, updatable = false)
    @NonNull GithubAccountEntity github;

    @OneToOne
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    UserProfileViewEntity profile;

    public Boolean isRegistered() {
        return userId != null;
    }

    public ShortContributorView toDomain() {
        return new ShortContributorView(GithubUserId.of(githubUserId), login, avatarUrl, isNull(userId) ? null : UserId.of(userId), email);
    }
}
