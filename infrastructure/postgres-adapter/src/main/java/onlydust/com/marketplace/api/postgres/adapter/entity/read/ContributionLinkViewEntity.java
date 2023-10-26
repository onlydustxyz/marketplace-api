package onlydust.com.marketplace.api.postgres.adapter.entity.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import onlydust.com.marketplace.api.domain.view.ContributionLinkView;

import java.util.Date;

public class ContributionLinkViewEntity {
    String id;
    @JsonProperty("created_at")
    Date createdAt;
    @JsonProperty("completed_at")
    Date completedAt;
    ContributionViewEntity.Type type;
    ContributionViewEntity.Status status;
    @JsonProperty("github_number")
    Long githubNumber;
    @JsonProperty("github_title")
    String githubTitle;
    @JsonProperty("github_html_url")
    String githubHtmlUrl;
    @JsonProperty("github_body")
    String githubBody;
    @JsonProperty("is_mine")
    Boolean isMine;

    public ContributionLinkView toView() {
        return ContributionLinkView.builder()
                .id(id)
                .createdAt(createdAt)
                .completedAt(completedAt)
                .type(type.toView())
                .status(status.toView())
                .githubNumber(githubNumber)
                .githubTitle(githubTitle)
                .githubHtmlUrl(githubHtmlUrl)
                .githubBody(githubBody)
                .isMine(isMine)
                .build();
    }
}
