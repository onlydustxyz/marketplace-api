package onlydust.com.marketplace.api.read.entities.project;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.read.model.LanguageWithLineCount.toLanguageWithPercentageResponse;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.EcosystemLinkResponse;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponseV2;
import onlydust.com.marketplace.api.contract.model.ProjectTag;
import onlydust.com.marketplace.api.read.model.LanguageWithLineCount;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Immutable
@MappedSuperclass
public class BaseProjectPageV2ItemQueryEntity {
    @Id
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String logoUrl;
    Integer contributorCount;
    Integer forkCount;
    Integer starCount;
    Integer goodFirstIssueCount;
    Integer availableIssueCount;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageWithLineCount> languages;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<ProjectTag> tags;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @NonNull List<EcosystemLinkResponse> ecosystems;

    private List<ProjectCategoryResponse> categories() {
        return categories == null ? List.of() : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList();
    }

    public ProjectShortResponseV2 toShortResponse() {
        return new ProjectShortResponseV2()
                .name(name)
                .shortDescription(shortDescription)
                .id(id)
                .logoUrl(logoUrl)
                .categories(categories())
                .languages(toLanguageWithPercentageResponse(languages))
                .availableIssueCount(availableIssueCount)
                .starCount(starCount)
                .goodFirstIssueCount(goodFirstIssueCount)
                .forkCount(forkCount)
                .contributorCount(contributorCount)
                .ecosystems(ecosystems);
    }
}
