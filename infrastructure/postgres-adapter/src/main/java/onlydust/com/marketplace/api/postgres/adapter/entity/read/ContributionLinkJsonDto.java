package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import onlydust.com.marketplace.project.domain.model.ContributionType;
import onlydust.com.marketplace.project.domain.model.GithubRepo;
import onlydust.com.marketplace.project.domain.view.ContributionLinkView;
import onlydust.com.marketplace.project.domain.view.ContributorLinkView;

@EqualsAndHashCode
public class ContributionLinkJsonDto {
    @JsonProperty("type")
    ContributionType type;
    @JsonProperty("github_number")
    Long githubNumber;
    @JsonProperty("github_status")
    String githubStatus;
    @JsonProperty("github_title")
    String githubTitle;
    @JsonProperty("github_html_url")
    String githubHtmlUrl;
    @JsonProperty("github_body")
    String githubBody;
    @JsonProperty("github_author_id")
    Long githubAuthorId;
    @JsonProperty("github_author_login")
    String githubAuthorLogin;
    @JsonProperty("github_author_html_url")
    String githubAuthorHtmlUrl;
    @JsonProperty("github_author_avatar_url")
    String githubAuthorAvatarUrl;
    @JsonProperty("is_mine")
    Boolean isMine;
    @JsonProperty("repo_id")
    Long repoId;
    @JsonProperty("repo_owner")
    String repoOwner;
    @JsonProperty("repo_name")
    String repoName;
    @JsonProperty("repo_html_url")
    String repoHtmlUrl;

    public ContributionLinkView toView() {
        final var repo = GithubRepo.builder()
                .id(repoId)
                .owner(repoOwner)
                .name(repoName)
                .htmlUrl(repoHtmlUrl)
                .build();

        final var author = ContributorLinkView.builder()
                .githubUserId(githubAuthorId)
                .login(githubAuthorLogin)
                .avatarUrl(githubAuthorAvatarUrl)
                .build();

        return ContributionLinkView.builder()
                .type(type)
                .githubNumber(githubNumber)
                .githubStatus(githubStatus)
                .githubTitle(githubTitle)
                .githubHtmlUrl(githubHtmlUrl)
                .githubBody(githubBody)
                .githubAuthor(author)
                .githubRepo(repo)
                .isMine(isMine)
                .build();
    }
}
