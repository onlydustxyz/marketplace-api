package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.Currency;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.TotalEarnedPerCurrency;
import onlydust.com.marketplace.api.domain.view.TotalsEarned;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectContributorViewEntity;

import static java.util.Objects.nonNull;

public interface ProjectContributorsMapper {

    static ProjectContributorsLinkView mapToDomainWithProjectLeadData(final ProjectContributorViewEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(mapAmountsEntityToDomain(contributorViewEntity))
                .rewards(contributorViewEntity.getRewards())
                .totalToReward(contributorViewEntity.getTotalToReward())
                .issuesToRewardCount(contributorViewEntity.getIssuesToReward())
                .pullRequestsToRewardCount(contributorViewEntity.getPrsToReward())
                .codeReviewToRewardCount(contributorViewEntity.getCodeReviewsToReward())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .build();
    }

    static ProjectContributorsLinkView mapToDomainWithoutProjectLeadData(final ProjectContributorViewEntity contributorViewEntity) {
        return ProjectContributorsLinkView.builder()
                .avatarUrl(contributorViewEntity.getAvatarUrl())
                .login(contributorViewEntity.getLogin())
                .earned(mapAmountsEntityToDomain(contributorViewEntity))
                .rewards(contributorViewEntity.getRewards())
                .contributionCount(contributorViewEntity.getContributionCount())
                .githubUserId(contributorViewEntity.getGithubUserId())
                .isRegistered(contributorViewEntity.isRegistered())
                .build();
    }

    private static TotalsEarned mapAmountsEntityToDomain(ProjectContributorViewEntity contributorViewEntity) {
        final TotalsEarned totalsEarned = TotalsEarned.builder()
                .totalDollarsEquivalent(contributorViewEntity.getEarned())
                .build();
        if (nonNull(contributorViewEntity.getOpAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Op)
                    .totalAmount(contributorViewEntity.getOpAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getOpDollarsEquivalentAmount()).build());
        }
        if (nonNull(contributorViewEntity.getStarkAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Strk)
                    .totalAmount(contributorViewEntity.getStarkAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getStarkDollarsEquivalentAmount()).build());
        }
        if (nonNull(contributorViewEntity.getEthAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Eth)
                    .totalAmount(contributorViewEntity.getEthAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getEthDollarsEquivalentAmount()).build());
        }
        if (nonNull(contributorViewEntity.getAptAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Apt)
                    .totalAmount(contributorViewEntity.getAptAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getAptDollarsEquivalentAmount()).build());
        }
        if (nonNull(contributorViewEntity.getUsdcAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Usdc)
                    .totalAmount(contributorViewEntity.getUsdcAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getUsdcDollarsEquivalentAmount()).build());
        }
        if (nonNull(contributorViewEntity.getUsdAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Usd)
                    .totalAmount(contributorViewEntity.getUsdAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getUsdAmount()).build());
        }
        if (nonNull(contributorViewEntity.getLordsAmount())) {
            totalsEarned.addDetail(TotalEarnedPerCurrency.builder()
                    .currency(Currency.Lords)
                    .totalAmount(contributorViewEntity.getLordsAmount())
                    .totalDollarsEquivalent(contributorViewEntity.getLordsDollarsEquivalentAmount()).build());
        }
        return totalsEarned;
    }

}
