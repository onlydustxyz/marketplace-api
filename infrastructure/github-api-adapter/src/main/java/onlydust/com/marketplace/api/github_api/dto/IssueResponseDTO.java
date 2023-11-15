package onlydust.com.marketplace.api.github_api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.Value;
import onlydust.com.marketplace.api.domain.view.CreatedAndClosedIssueView;

import java.util.Date;

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
    Long comments;
    Long number;
    @JsonProperty("state_reason")
    StateReason stateReason;
    @JsonProperty("html_url")
    String htmlUrl;


    public enum State {
        open, closed
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
                .number(this.number)
                .htmlUrl(this.htmlUrl)
                .status(this.toStatus())
                .title(this.title)
                .build();
    }

    private CreatedAndClosedIssueView.Status toStatus() {
        return switch (this.state) {
            case open -> CreatedAndClosedIssueView.Status.OPEN;
            case closed -> switch (this.stateReason) {
                case not_planned -> CreatedAndClosedIssueView.Status.CANCELLED;
                default -> CreatedAndClosedIssueView.Status.CLOSED;
            };
        };
    }
}
