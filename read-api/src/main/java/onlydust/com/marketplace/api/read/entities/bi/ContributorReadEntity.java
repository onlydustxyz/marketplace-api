package onlydust.com.marketplace.api.read.entities.bi;

import static java.util.Comparator.comparing;
import static onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity.pretty;
import static onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity.toDecimalNumberKpi;
import static onlydust.com.marketplace.api.read.entities.bi.ContributorKpisReadEntity.toNumberKpi;
import static onlydust.com.marketplace.kernel.mapper.AmountMapper.prettyUsd;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
public class ContributorReadEntity {
    @Id
    @NonNull
    Long contributorId;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorOverviewResponse contributor;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> projects;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageWithPercentageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectContributorLabelResponse> projectContributorLabels;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> maintainedProjects;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ContributorGithubRepoResponse> repos;
    String contributorCountry;

    BigDecimal totalRewardedUsdAmount;
    Integer rewardCount;
    Integer completedContributionCount;
    Integer completedIssueCount;
    Integer completedPrCount;
    Integer completedCodeReviewCount;
    Integer inProgressIssueCount;
    Integer pendingApplicationCount;

    private Optional<Country> contributorCountry() {
        return Optional.ofNullable(contributorCountry).map(Country::fromIso3);
    }

    public BiContributorResponse toDto() {
        return new BiContributorResponse()
                .contributor(pretty(contributor))
                .projects(projects == null ? null : projects.stream().sorted(comparing(ProjectLinkResponse::getName)).toList())
                .categories(categories == null ? null : categories.stream().sorted(comparing(ProjectCategoryResponse::getName)).toList())
                .languages(languages == null ? null : languages.stream().sorted(comparing(LanguageWithPercentageResponse::getName)).toList())
                .ecosystems(ecosystems == null ? null : ecosystems.stream().sorted(comparing(EcosystemLinkResponse::getName)).toList())
                .projectContributorLabels(projectContributorLabels == null ? null :
                        projectContributorLabels.stream().sorted(comparing(ProjectContributorLabelResponse::getName)).toList())
                .country(contributorCountry().map(country -> new CountryResponse()
                                .code(country.iso2Code())
                                .name(country.display().orElse(null)))
                        .orElse(null))
                .totalRewardedUsdAmount(toDecimalNumberKpi(prettyUsd(totalRewardedUsdAmount), prettyUsd(totalRewardedUsdAmount)))
                .rewardCount(toNumberKpi(rewardCount, rewardCount))
                .contributionCount(toNumberKpi(completedContributionCount, completedContributionCount))
                .issueCount(toNumberKpi(completedIssueCount, completedIssueCount))
                .prCount(toNumberKpi(completedPrCount, completedPrCount))
                .codeReviewCount(toNumberKpi(completedCodeReviewCount, completedCodeReviewCount))
                .inProgressIssueCount(inProgressIssueCount)
                .pendingApplicationCount(pendingApplicationCount)
                .maintainedProjectCount(maintainedProjects == null ? 0 : maintainedProjects.size())
                .repos(repos == null ? List.of() :
                        repos.stream().sorted(comparing(ContributorGithubRepoResponse::getContributorContributionCount).reversed()).toList());
    }
}
