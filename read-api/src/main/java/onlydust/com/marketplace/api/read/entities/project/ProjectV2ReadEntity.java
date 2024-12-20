package onlydust.com.marketplace.api.read.entities.project;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.read.model.LanguageWithLineCount.toLanguageWithPercentageResponse;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.model.LanguageWithLineCount;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Immutable
@Entity
public class ProjectV2ReadEntity {
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
    Integer mergedPrCount;
    Integer currentWeekAvailableIssueCount;
    Integer currentWeekMergedPrCount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageWithLineCount> languages;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<ProjectTag> tags;
    @JdbcTypeCode(SqlTypes.ARRAY)
    @NonNull List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<GithubUserResponse> leads;
    @JdbcTypeCode(SqlTypes.JSON)
    List<SimpleLink> moreInfos;

    private List<ProjectCategoryResponse> categories() {
        return categories == null ? List.of() : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList();
    }

    private List<EcosystemLinkResponse> ecosystems() {
        return ecosystems == null ? List.of() : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList();
    }

    private List<GithubUserResponse> leads() {
        return leads == null ? List.of() : leads.stream().sorted(comparing(GithubUserResponse::getLogin)).toList();
    }

    public ProjectResponseV2 toResponse() {
        return new ProjectResponseV2()
                .name(name)
                .slug(slug)
                .shortDescription(shortDescription)
                .id(id)
                .logoUrl(logoUrl)
                .categories(categories())
                .languages(toLanguageWithPercentageResponse(languages))
                .availableIssueCount(availableIssueCount)
                .goodFirstIssueCount(goodFirstIssueCount)
                .mergedPrCount(mergedPrCount)
                .starCount(starCount)
                .forkCount(forkCount)
                .contributorCount(contributorCount)
                .ecosystems(ecosystems())
                .leads(leads())
                .moreInfos(moreInfos)
                .currentWeekAvailableIssueCount(currentWeekAvailableIssueCount)
                .currentWeekMergedPrCount(currentWeekMergedPrCount);
    }
}
