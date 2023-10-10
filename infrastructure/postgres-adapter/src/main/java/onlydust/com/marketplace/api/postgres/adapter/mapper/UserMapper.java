package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.view.ContributorLinkView;
import onlydust.com.marketplace.api.domain.view.ProjectLeaderLinkView;
import onlydust.com.marketplace.api.postgres.adapter.entity.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.GithubUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.old.RegisteredUserViewEntity;

public interface UserMapper {

    static ContributorLinkView mapToContributorLinkView(GithubUserViewEntity user) {
        return ContributorLinkView.builder()
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }

    static ProjectLeaderLinkView mapToProjectLeaderLinkView(RegisteredUserViewEntity user) {
        return ProjectLeaderLinkView.builder()
                .id(user.getId())
                .githubUserId(user.getGithubId())
                .login(user.getLogin())
                .avatarUrl(user.getAvatarUrl())
                .url(user.getHtmlUrl())
                .build();
    }

    static User mapUserToDomain(UserEntity user) {
        return User.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .login(user.getGithubLogin())
                .avatarUrl(user.getGithubAvatarUrl())
                .build();
    }

    static UserEntity mapUserToEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .githubUserId(user.getGithubUserId())
                .githubLogin(user.getLogin())
                .githubAvatarUrl(user.getAvatarUrl())
                .build();
    }
}
