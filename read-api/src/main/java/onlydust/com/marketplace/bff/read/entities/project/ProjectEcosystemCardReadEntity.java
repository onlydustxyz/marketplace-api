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

import static java.util.Objects.nonNull;

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
        EcosystemProjectPageItemResponse contractResponse = new EcosystemProjectPageItemResponse();
        contractResponse.setId(id);
        contractResponse.setName(name);
        contractResponse.setSlug(slug);
        contractResponse.setShortDescription(shortDescription);
        contractResponse.setLogoUrl(logoUrl);
        contractResponse.setContributorsCount(contributorsCount);
        if (nonNull(topContributors)) {
            this.topContributors.stream()
                    .map(githubUserLinkJson -> new GithubUserResponse()
                            .avatarUrl(githubUserLinkJson.getAvatarUrl())
                            .githubUserId(githubUserLinkJson.getGithubUserId())
                            .login(githubUserLinkJson.getLogin())
                    )
                    .forEach(contractResponse::addTopContributorsItem);
        }
        // TODO remove ?
        if (nonNull(languages)) {
            this.languages.stream()
                    .map(LanguageReadEntity::toDto)
                    .forEach(contractResponse::addLanguagesItem);
        }
        return contractResponse;
    }
}
