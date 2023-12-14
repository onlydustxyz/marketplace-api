package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.ProjectChurnedContributorsPageItemResponse;
import onlydust.com.marketplace.api.contract.model.ProjectChurnedContributorsPageResponse;
import onlydust.com.marketplace.api.contract.model.UserContributionPageResponse;
import onlydust.com.marketplace.api.domain.model.Contributor;
import onlydust.com.marketplace.api.domain.view.ChurnedContributorView;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectContributorsLinkView;
import onlydust.com.marketplace.api.domain.view.pagination.Page;
import onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper;

import java.net.URI;
import java.net.URL;

import static onlydust.com.marketplace.api.domain.view.pagination.PaginationHelper.hasMore;

public interface ContributorMapper {
    static ContributorResponse of(ContributorLinkView contributorLinkView) {
        return new ContributorResponse()
                .githubUserId(contributorLinkView.getGithubUserId())
                .login(contributorLinkView.getLogin())
                .avatarUrl(contributorLinkView.getAvatarUrl())
                .htmlUrl(URI.create(contributorLinkView.getUrl()))
                .isRegistered(contributorLinkView.getIsRegistered());
    }

    static ProjectChurnedContributorsPageResponse mapProjectChurnedContributorsPageResponse(int pageIndex, Page<ChurnedContributorView> contributors) {
        return new ProjectChurnedContributorsPageResponse()
                .contributors(contributors.getContent().stream().map(ContributorMapper::mapProjectChurnedContributorsPageItemResponse).toList())
                .hasMore(hasMore(pageIndex, contributors.getTotalPageNumber()))
                .totalPageNumber(contributors.getTotalPageNumber())
                .totalItemNumber(contributors.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, contributors.getTotalPageNumber()));
    }

    static ProjectChurnedContributorsPageItemResponse mapProjectChurnedContributorsPageItemResponse(ChurnedContributorView contributor) {
        return new ProjectChurnedContributorsPageItemResponse()
                .githubUserId(contributor.getGithubId())
                .login(contributor.getLogin())
                .htmlUrl(contributor.getHtmlUrl() == null ? null : URI.create(contributor.getHtmlUrl()))
                .avatarUrl(contributor.getAvatarUrl())
                .isRegistered(contributor.getIsRegistered())
                .cover(UserMapper.coverToUserProfileResponse(contributor.getCover()))
                .lastContribution(ContributionMapper.mapChurnedContribution(contributor.getLastContribution()))
                ;
    }
}
