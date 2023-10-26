package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;

public interface ProjectContributorsMapper {

    static ProjectContributorsLinkView.SortBy mapSortBy(String sort) {
        final ProjectContributorsLinkView.SortBy sortBy = switch (isNull(sort) ? "" : sort) {
            case "CONTRIBUTION_COUNT" -> ProjectContributorsLinkView.SortBy.contributionCount;
            case "EARNED" -> ProjectContributorsLinkView.SortBy.earned;
            case "REWARD_COUNT" -> ProjectContributorsLinkView.SortBy.rewardCount;
            case "TO_REWARD_COUNT" -> ProjectContributorsLinkView.SortBy.toRewardCount;
            default -> ProjectContributorsLinkView.SortBy.login;
        };
        return sortBy;
    }

    static ContributorsPageResponse mapProjectContributorsLinkViewPageToResponse(final Page<ProjectContributorsLinkView> page,
                                                                                 final int pageIndex) {
        final ContributorsPageResponse contributorPageResponse = new ContributorsPageResponse();
        contributorPageResponse.setTotalPageNumber(page.getTotalPageNumber());
        contributorPageResponse.setTotalItemNumber(page.getTotalItemNumber());
        contributorPageResponse.setContributors(page.getContent().stream()
                .map(ProjectContributorsMapper::mapProjectContributorsLinkViewToResponse).toList());
        contributorPageResponse.setHasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()));
        contributorPageResponse.setNextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
        return contributorPageResponse;
    }

    Faker FAKER = new Faker();

    static ContributorPageItemResponse mapProjectContributorsLinkViewToResponse(final ProjectContributorsLinkView projectContributorsLinkView) {
        final ContributorPageItemResponse response = new ContributorPageItemResponse();
        response.setAvatarUrl(projectContributorsLinkView.getAvatarUrl());
        response.setGithubUserId(projectContributorsLinkView.getGithubUserId());
        response.setLogin(projectContributorsLinkView.getLogin());
        response.setEarned(new RewardTotalAmountsResponse()
                .totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                .details(
                        List.of(
                                new MyRewardAmountResponse().totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .currency(CurrencyContract.USD),
                                new MyRewardAmountResponse().totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .totalDollarsEquivalent(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .currency(CurrencyContract.ETH),
                                new MyRewardAmountResponse().totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .totalDollarsEquivalent(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .currency(CurrencyContract.OP),
                                new MyRewardAmountResponse().totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .currency(CurrencyContract.STARK),
                                new MyRewardAmountResponse().totalAmount(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .totalDollarsEquivalent(BigDecimal.valueOf(FAKER.number().numberBetween(10, 1000)))
                                        .currency(CurrencyContract.APT)
                        )
                )
        );
        response.setContributionCount(projectContributorsLinkView.getContributionCount());
        response.setRewardCount(projectContributorsLinkView.getRewards());
        response.setContributionToRewardCount(projectContributorsLinkView.getTotalToReward());
        response.setPullRequestToReward(projectContributorsLinkView.getPullRequestsToRewardCount());
        response.setCodeReviewToReward(projectContributorsLinkView.getCodeReviewToRewardCount());
        response.setIssueToReward(projectContributorsLinkView.getIssuesToRewardCount());
        response.setIsRegistered(isNull(projectContributorsLinkView.getIsRegistered()) ? false :
                projectContributorsLinkView.getIsRegistered());
        return response;
    }
}
