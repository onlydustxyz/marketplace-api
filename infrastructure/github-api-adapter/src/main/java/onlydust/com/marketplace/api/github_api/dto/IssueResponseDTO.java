package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import onlydust.com.marketplace.api.domain.view.CreatedAndClosedIssueView;

import java.util.Date;

@Data
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
    Long comments;
    Long number;
    @JsonProperty("state_reason")
    StateReason stateReason;
    @JsonProperty("html_url")
    String htmlUrl;


    public enum State {
        open, close
    }

    public enum StateReason {
        completed, not_planned, reopened
    }

    public CreatedAndClosedIssueView toView(final String repoName) {
        return CreatedAndClosedIssueView.builder()
                .updatedAt(this.updatedAt)
                .createdAt(this.createdAt)
                .closedAt(this.closedAt)
                .id(this.id)
                .commentsCount(this.comments)
                .repoName(repoName)
                .htmlUrl(this.htmlUrl)
                .status(this.toStatus())
                .build();
    }

    private CreatedAndClosedIssueView.Status toStatus() {
        return switch (this.state) {
            case open -> CreatedAndClosedIssueView.Status.OPEN;
            case close -> switch (this.stateReason) {
                case not_planned -> CreatedAndClosedIssueView.Status.CANCELLED;
                default -> CreatedAndClosedIssueView.Status.CLOSED;
            };
        };
    }
}
