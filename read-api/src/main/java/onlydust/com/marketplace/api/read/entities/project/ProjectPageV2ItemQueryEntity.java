package onlydust.com.marketplace.api.read.entities.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

@NoArgsConstructor(force = true)
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Immutable
@Entity
public class ProjectPageV2ItemQueryEntity {
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
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageWithLineCount> languages;
    @JdbcTypeCode(SqlTypes.ARRAY)
    List<ProjectTag> tags;


    private List<ProjectCategoryResponse> categories() {
        return categories == null ? List.of() : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList();
    }

    private List<LanguageWithPercentageResponse> languages() {
        if (isNull(languages)) {
            return List.of();
        }
        final long totalLines = languages.stream()
                .mapToLong(language -> language.lineCount)
                .sum();
        return languages.stream()
                .map(language -> new LanguageWithPercentageResponse()
                        .name(language.name)
                        .slug(language.slug)
                        .id(language.id)
                        .logoUrl(language.logoUrl)
                        .bannerUrl(language.bannerUrl)
                        .percentage(BigDecimal.valueOf(language.lineCount)
                                .multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(totalLines), 2, java.math.RoundingMode.HALF_UP)))
                .sorted(comparing(LanguageWithPercentageResponse::getPercentage))
                .toList();
    }

    public ProjectShortResponseV2 toShortResponse() {
        return new ProjectShortResponseV2()
                .name(name)
                .shortDescription(shortDescription)
                .id(id)
                .logoUrl(logoUrl)
                .categories(categories())
                .languages(languages())
                .availableIssueCount(availableIssueCount)
                .starCount(starCount)
                .goodFirstIssueCount(goodFirstIssueCount)
                .forkCount(forkCount)
                .contributorCount(contributorCount);
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    @Accessors(fluent = true)
    public static class Ecosystem {
        @EqualsAndHashCode.Include
        @JsonProperty("id")
        UUID id;
        @JsonProperty("url")
        String url;
        @JsonProperty("logoUrl")
        String logoUrl;
        @JsonProperty("name")
        String name;
        @JsonProperty("slug")
        String slug;
        @JsonProperty("hidden")
        Boolean hidden;

        public EcosystemLinkResponse toDto() {
            return new EcosystemLinkResponse()
                    .id(id)
                    .name(name)
                    .slug(slug)
                    .logoUrl(logoUrl)
                    .url(url)
                    .hidden(hidden);
        }
    }

    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @Getter
    public static class LanguageWithLineCount {
        @EqualsAndHashCode.Include
        UUID id;
        String name;
        String slug;
        String logoUrl;
        String bannerUrl;
        Integer lineCount;
    }
}
