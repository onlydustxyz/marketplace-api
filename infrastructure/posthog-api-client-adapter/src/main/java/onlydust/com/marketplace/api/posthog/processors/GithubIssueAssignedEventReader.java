package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.event.OnGithubIssueAssignedTrackingEvent;

public class GithubIssueAssignedEventReader implements EventReader<OnGithubIssueAssignedTrackingEvent> {
    @Override
    public void addProperties(final OnGithubIssueAssignedTrackingEvent event, final ObjectNode properties) {
        properties.put("issue_id", event.issueId());
        properties.put("created_at", event.createdAt().toString());
        properties.put("assignee_github_id", event.assigneeGithubId());
        properties.put("assignee_user_id", event.assigneeUserId().toString());
        properties.put("is_good_first_issue", event.isGoodFirstIssue());
    }

    @Override
    public String eventType(OnGithubIssueAssignedTrackingEvent event) {
        return "github_issue_assigned";
    }

    @Override
    public Object distinctId(OnGithubIssueAssignedTrackingEvent event) {
        return event.assigneeUserId();
    }

    @Override
    public Object timestamp(OnGithubIssueAssignedTrackingEvent event) {
        return event.assignedAt();
    }
}