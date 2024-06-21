package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ContributorQueryEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.ProjectLeadQueryEntity;
import onlydust.com.marketplace.api.read.entities.user.AllUserReadEntity;
import org.springframework.data.domain.Page;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface UserMapper {

    static UserPage pageToResponse(final Page<AllUserReadEntity> page, final int pageIndex) {
        return new UserPage()
                .users(page.getContent().stream().map(AllUserReadEntity::toBoPageItemResponse).toList())
                .totalPageNumber(page.getTotalPages())
                .totalItemNumber((int) page.getTotalElements())
                .hasMore(hasMore(pageIndex, page.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, page.getTotalPages()));
    }

    static onlydust.com.marketplace.api.contract.model.GithubUserResponse map(final ContributorQueryEntity user) {
        return new onlydust.com.marketplace.api.contract.model.GithubUserResponse()
                .githubUserId(user.getGithubUserId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl());
    }

    static onlydust.com.marketplace.api.contract.model.RegisteredUserResponse map(final ProjectLeadQueryEntity user) {
        return new onlydust.com.marketplace.api.contract.model.RegisteredUserResponse()
                .id(user.getId())
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl());
    }
}
