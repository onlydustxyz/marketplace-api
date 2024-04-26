package onlydust.com.marketplace.api.postgres.adapter.entity.read;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.view.ShortContributorView;
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

    public Boolean isRegistered() {
        return userId != null;
    }

    public ShortContributorView toDomain() {
        return new ShortContributorView(GithubUserId.of(githubUserId), login, avatarUrl, isNull(userId) ? null : UserId.of(userId), email);
    }
}
