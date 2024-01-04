package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.ContributionType;
import onlydust.com.marketplace.api.domain.view.RewardItemStatus;
import onlydust.com.marketplace.api.domain.view.RewardableItemView;


@Value
@NoArgsConstructor(force = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueResponseDTO {

  @JsonProperty("created_at")
  Date createdAt;
  @JsonProperty("updated_at")
  Date updatedAt;
  @JsonProperty("closed_at")
  Date closedAt;
  Long id;
  String title;
  State state;
  Integer comments;
  Long number;
  @JsonProperty("state_reason")
  StateReason stateReason;
  @JsonProperty("html_url")
  String htmlUrl;

  public RewardableItemView toView(final String repoName, final Long repoId) {
    assert this.id != null;
    return RewardableItemView.builder()
        .id(this.id.toString())
        .type(ContributionType.ISSUE)
        .status(this.toStatus())
        .createdAt(this.createdAt)
        .completedAt(State.closed.equals(this.state) ? this.closedAt : this.updatedAt)
        .commentsCount(this.comments)
        .number(this.number)
        .repoName(repoName)
        .repoId(repoId)
        .githubUrl(this.htmlUrl)
        .title(this.title)
        .ignored(false)
        .build();
  }

  private RewardItemStatus toStatus() {
    return this.state == null ? RewardItemStatus.OPEN : switch (this.state) {
      case open -> RewardItemStatus.OPEN;
      case closed -> StateReason.not_planned.equals(this.stateReason) ? RewardItemStatus.CANCELLED :
          RewardItemStatus.CLOSED;
    };
  }

  enum State {
    open, closed
  }

  enum StateReason {
    completed, not_planned, reopened
  }
}
