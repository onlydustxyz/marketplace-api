package onlydust.com.marketplace.bff.read.mapper;

import onlydust.com.backoffice.api.contract.model.UserLinkResponse;
import onlydust.com.backoffice.api.contract.model.UserPage;
import onlydust.com.backoffice.api.contract.model.UserPageItemResponse;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.AllUserViewEntity;
import onlydust.com.marketplace.bff.read.entities.user.UserShortEntity;
import org.springframework.data.domain.Page;

import java.time.ZoneId;

import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface UserMapper {

    static UserPage pageToResponse(final Page<UserShortEntity> userShortEntityPage, final int pageIndex) {
        return new UserPage()
                .users(userShortEntityPage.getContent().stream().map(user -> new UserPageItemResponse()
                        .id(user.getId())
                        .githubUserId(user.getGithubUserId())
                        .login(user.getLogin())
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .lastSeenAt(user.getLastSeenAt().toInstant().atZone(ZoneId.systemDefault()))
                        .signedUpAt(user.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()))
                ).toList())
                .totalPageNumber(userShortEntityPage.getTotalPages())
                .totalItemNumber((int) userShortEntityPage.getTotalElements())
                .hasMore(hasMore(pageIndex, userShortEntityPage.getTotalPages()))
                .nextPageIndex(nextPageIndex(pageIndex, userShortEntityPage.getTotalPages()));
    }

    static UserLinkResponse map(final UserShortEntity user) {
        return new UserLinkResponse()
                .userId(user.getId())
                .githubUserId(user.getGithubUserId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl());
    }

    static UserLinkResponse map(final AllUserViewEntity user) {
        return new UserLinkResponse()
                .userId(user.userId())
                .githubUserId(user.githubUserId())
                .login(user.login())
                .avatarUrl(user.avatarUrl());
    }
}