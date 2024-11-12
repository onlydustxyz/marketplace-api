package onlydust.com.marketplace.api.read.entities.project;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.billing_profile.BillingProfileReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Entity
@NoArgsConstructor
@Immutable
@Accessors(fluent = true)
@Getter
public class ProjectAsContributorReadEntity {
    @Id
    UUID id;
    String slug;
    String name;
    String shortDescription;
    String logoUrl;
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(columnDefinition = "project_visibility")
    ProjectVisibility visibility;

    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<RegisteredUserResponse> leads;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ShortGithubRepoResponse> repos;

    @JdbcTypeCode(SqlTypes.ARRAY)
    UUID[] goodFirstIssueIds;
    Integer contributorCount;
    Integer contributionCount;
    BigDecimal rewardedUsdAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;

    @ManyToOne
    @JoinColumn(name = "billingProfileId", insertable = false, updatable = false)
    BillingProfileReadEntity billingProfile;

    public MyProjectsAsContributorPageItemResponse toDto(Long callerGithubUserId) {
        return new MyProjectsAsContributorPageItemResponse()
                .id(id)
                .slug(slug)
                .name(name)
                .shortDescription(shortDescription)
                .logoUrl(logoUrl)
                .visibility(visibility)
                .languages(languages)
                .leads(leads)
                .repos(repos)
                .goodFirstIssueIds(goodFirstIssueIds == null ? List.of() : List.of(goodFirstIssueIds))
                .contributorCount(contributorCount)
                .contributionCount(contributionCount)
                .rewardedUsdAmount(rewardedUsdAmount)
                .billingProfile(billingProfile == null ? null : billingProfile.toShortResponse(callerGithubUserId));
    }
}
