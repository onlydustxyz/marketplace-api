package onlydust.com.marketplace.api.read.entities.bi;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.read.entities.user.GlobalUsersRanksReadEntity;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

import static onlydust.com.marketplace.api.read.entities.user.PublicUserProfileResponseV2Entity.prettyRankPercentile;

@Entity
@NoArgsConstructor(force = true)
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Immutable
@Accessors(fluent = true)
@Table(name = "p_contributor_global_data", schema = "bi")
public class ContributorReadProjectionEntity {
    @Id
    @NonNull
    Long contributorId;

    String contributorLogin;
    String contributorCountry;

    @JdbcTypeCode(SqlTypes.JSON)
    @NonNull
    ContributorResponse contributor;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectLinkResponse> projects;
    @JdbcTypeCode(SqlTypes.JSON)
    List<ProjectCategoryResponse> categories;
    @JdbcTypeCode(SqlTypes.JSON)
    List<LanguageResponse> languages;
    @JdbcTypeCode(SqlTypes.JSON)
    List<EcosystemLinkResponse> ecosystems;

    BigDecimal totalRewardedUsdAmount;
    Integer rewardCount;
    Integer contributionCount;
    Integer issueCount;
    Integer prCount;
    Integer codeReviewCount;

    @OneToOne
    @MapsId
    GlobalUsersRanksReadEntity globalUserRank;

    public ApplicantResponse toApplicantResponse() {
        return new ApplicantResponse()
                .id(contributor.getId())
                .githubUserId(contributorId)
                .login(contributorLogin)
                .avatarUrl(contributor.getAvatarUrl())
                .isRegistered(contributor.getIsRegistered())
                .countryCode(contributorCountry == null ? null : Country.fromIso3(contributorCountry).iso2Code())
                .languages(languages)
                .ecosystems(ecosystems)
                .totalRewardedUsdAmount(totalRewardedUsdAmount)
                .rewardCount(rewardCount)
                .contributionCount(contributionCount)
                .issueCount(issueCount)
                .prCount(prCount)
                .codeReviewCount(codeReviewCount)
                .globalRank(globalUserRank != null ? Math.toIntExact(globalUserRank.rank()) : null)
                .globalRankPercentile(globalUserRank != null ? prettyRankPercentile(globalUserRank.rankPercentile()) : null)
                .globalRankCategory(globalUserRank != null ? globalUserRank.rankCategory() : null);
    }

    public ContributorResponse toContributorResponse() {
        return contributor;
    }

    public GithubUserResponse toGithubUserResponse() {
        return new GithubUserResponse()
                .githubUserId(contributorId)
                .login(contributorLogin)
                .avatarUrl(contributor.getAvatarUrl());
    }
}
