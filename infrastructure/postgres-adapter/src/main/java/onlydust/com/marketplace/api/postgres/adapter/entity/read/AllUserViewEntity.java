package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
import onlydust.com.marketplace.kernel.model.UserId;
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

    public Boolean isRegistered() {
        return userId != null;
    }

    public ShortContributorView toDomain() {
        return new ShortContributorView(GithubUserId.of(githubUserId), login, avatarUrl, isNull(userId) ? null : UserId.of(userId), email);
    }

    public GithubUserIdentity toGithubIdentity() {
        return GithubUserIdentity.builder()
                .githubUserId(githubUserId)
                .login(login)
                .avatarUrl(avatarUrl)
                .email(email)
                .build();
    }
}
