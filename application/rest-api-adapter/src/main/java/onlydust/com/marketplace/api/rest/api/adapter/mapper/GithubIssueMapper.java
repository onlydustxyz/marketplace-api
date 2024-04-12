package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.ContributorResponse;
import onlydust.com.marketplace.api.contract.model.GithubIssue;
import onlydust.com.marketplace.api.contract.model.GithubIssueStatus;
import onlydust.com.marketplace.api.contract.model.GoodFirstIssuesPageResponse;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;
import onlydust.com.marketplace.project.domain.view.GithubIssueView;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface GithubIssueMapper {
    static GoodFirstIssuesPageResponse map(Page<GithubIssueView> page, Integer pageIndex) {
        return new GoodFirstIssuesPageResponse()
                .issues(page.getContent().stream().map(GithubIssueMapper::map).toList())
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, page.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static GithubIssue map(GithubIssueView issue) {
        return new GithubIssue()
                .id(issue.id())
                .number(issue.number())
                .repository(GithubRepoMapper.mapRepoToShortResponse(issue.repository()))
                .createdAt(issue.createdAt())
                .closedAt(issue.closedAt())
                .title(issue.title())
                .body(issue.body())
                .htmlUrl(issue.htmlUrl())
                .status(map(issue.status()))
                .author(map(issue.author()))
                .commentCount(issue.commentsCount())
                .labels(issue.labels());
    }

    static GithubIssueStatus map(GithubIssueView.Status status) {
        return switch (status) {
            case OPEN -> GithubIssueStatus.OPEN;
            case CANCELLED -> GithubIssueStatus.CANCELLED;
            case COMPLETED -> GithubIssueStatus.COMPLETED;
        };
    }

    static ContributorResponse map(ContributorLinkView user) {
        return new ContributorResponse()
                .githubUserId(user.getGithubUserId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .isRegistered(user.getIsRegistered())
                ;
    }
}
