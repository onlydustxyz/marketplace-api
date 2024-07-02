package onlydust.com.marketplace.api.read.entities.github;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubAppInstallationViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition.GithubRepoViewEntity;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "github_accounts", schema = "indexer_exp")
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class GithubAccountReadEntity {
    @Id
    @EqualsAndHashCode.Include
    Long id;
    String login;
    String type;
    String htmlUrl;
    String avatarUrl;
    String name;
    String bio;
    String location;
    String website;
    String twitter;
    String linkedin;
    String telegram;
    @NonNull
    ZonedDateTime createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    Set<GithubRepoViewEntity> repos;
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @Getter(AccessLevel.NONE)
    @NonNull
    List<GithubAppInstallationViewEntity> installation;

    Optional<GithubAppInstallationViewEntity> installation() {
        return installation.stream().findFirst();
    }

    @Formula("exists(select 1 from iam.users u where u.github_user_id = id)")
    boolean isRegistered;

    public ContributorResponse toContributorResponse() {
        return new ContributorResponse()
                .githubUserId(id)
                .login(login)
                .avatarUrl(avatarUrl)
                .isRegistered(isRegistered);
    }
}
