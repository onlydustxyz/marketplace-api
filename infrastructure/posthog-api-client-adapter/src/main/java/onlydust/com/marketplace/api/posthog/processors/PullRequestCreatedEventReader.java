package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestCreatedTrackingEvent;

public class PullRequestCreatedEventReader implements EventReader<OnPullRequestCreatedTrackingEvent> {
    @Override
    public void addProperties(final OnPullRequestCreatedTrackingEvent event, final ObjectNode properties) {
        properties.put("pull_request_id", event.pullRequestId());
        properties.put("author_github_id", event.authorGithubId());
        properties.put("author_user_id", event.authorUserId().toString());
        properties.put("pr_created_at", event.createdAt().toString());
    }

    @Override
    public String eventType(OnPullRequestCreatedTrackingEvent event) {
        return "pull_request_created";
    }

    @Override
    public Object distinctId(OnPullRequestCreatedTrackingEvent event) {
        return event.authorUserId();
    }

    @Override
    public Object timestamp(OnPullRequestCreatedTrackingEvent event) {
        return event.createdAt();
    }
}
