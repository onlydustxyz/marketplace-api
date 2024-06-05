package onlydust.com.marketplace.bff.read.entities.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.EcosystemProjectPageItemResponse;
import onlydust.com.marketplace.api.contract.model.GithubUserResponse;
import onlydust.com.marketplace.bff.read.entities.LanguageReadEntity;
import onlydust.com.marketplace.bff.read.entities.user.GithubUserLinkJson;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Accessors(fluent = true)
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@Immutable
@Table(name = "projects", schema = "public")
public class ProjectEcosystemCardReadEntity {
    @Id
    @EqualsAndHashCode.Include
    UUID id;
    @NonNull
    String name;
    @NonNull
    String slug;
    @NonNull
    @Column(name = "short_description")
    String shortDescription;
    @Column(name = "logo_url")
    String logoUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    List<GithubUserLinkJson> topContributors;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageReadEntity> languages;
    Integer contributorsCount;


    public EcosystemProjectPageItemResponse toContract() {
        return new EcosystemProjectPageItemResponse()
                .id(id)
                .name(name)
                .slug(slug)
                .shortDescription(shortDescription)
                .logoUrl(logoUrl)
                .contributorsCount(contributorsCount)
                .topContributors(isNull(topContributors) ? List.of() : topContributors.stream()
                        .map(githubUserLinkJson -> new GithubUserResponse()
                                .avatarUrl(githubUserLinkJson.getAvatarUrl())
                                .githubUserId(githubUserLinkJson.getGithubUserId())
                                .login(githubUserLinkJson.getLogin())
                        ).toList())
                .languages(languages.stream().map(LanguageReadEntity::toDto).toList());
    }
}
