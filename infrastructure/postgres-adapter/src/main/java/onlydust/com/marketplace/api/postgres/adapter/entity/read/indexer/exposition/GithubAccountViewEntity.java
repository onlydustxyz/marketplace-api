package onlydust.com.marketplace.api.postgres.adapter.entity.read.indexer.exposition;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserViewEntity;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import org.hibernate.annotations.Immutable;

import java.time.ZonedDateTime;
import java.util.Set;

@Entity
@Table(name = "github_accounts", schema = "indexer_exp")
@Value
@ToString
@Immutable
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(force = true)
@Accessors(fluent = true)
public class GithubAccountViewEntity {
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
    @NonNull ZonedDateTime createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    Set<GithubRepoViewEntity> repos;
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    GithubAppInstallationViewEntity installation;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "githubUser")
    UserViewEntity user;

    public ContributorLinkView toContributorLinkView() {
        return ContributorLinkView.builder()
                .githubUserId(id)
                .login(login)
                .avatarUrl(avatarUrl)
                .isRegistered(user != null)
                .build();
    }
}
