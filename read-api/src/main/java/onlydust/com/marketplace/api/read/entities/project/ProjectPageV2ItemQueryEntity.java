package onlydust.com.marketplace.api.read.entities.project;

import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.api.contract.model.LanguageWithPercentageResponse;
import onlydust.com.marketplace.api.contract.model.ProjectCategoryResponse;
import onlydust.com.marketplace.api.contract.model.ProjectShortResponseV2;
import onlydust.com.marketplace.api.contract.model.ProjectTag;

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
        return totalLines == 0L ? List.of() : languages.stream()
                .map(language -> new LanguageWithPercentageResponse()
                        .name(language.name)
                        .slug(language.slug)
                        .id(language.id)
                        .logoUrl(language.logoUrl)
                        .transparentLogoUrl(language.transparentLogoUrl)
                        .bannerUrl(language.bannerUrl)
                        .color(language.color)
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
    public static class LanguageWithLineCount {
        @EqualsAndHashCode.Include
        UUID id;
        String name;
        String slug;
        String logoUrl;
        String bannerUrl;
        String transparentLogoUrl;
        String color;
        Integer lineCount;
    }
}
