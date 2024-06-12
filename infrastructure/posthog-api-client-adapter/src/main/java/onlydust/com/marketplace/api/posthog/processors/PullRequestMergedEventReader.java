package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.event.OnPullRequestMergedTrackingEvent;

import java.util.UUID;

public class PullRequestMergedEventReader implements EventReader<OnPullRequestMergedTrackingEvent> {
    @Override
    public void addProperties(final OnPullRequestMergedTrackingEvent event, final ObjectNode properties) {
        properties.put("pull_request_id", event.pullRequestId());
        properties.put("author_github_id", event.authorGithubId());
        properties.put("author_user_id", event.authorUserId().toString());
        properties.put("created_at", event.createdAt().toString());
    }

    @Override
    public String eventType(OnPullRequestMergedTrackingEvent event) {
        return "pull_request_merged";
    }

    @Override
    public Object distinctId(OnPullRequestMergedTrackingEvent event) {
        return UUID.randomUUID();
    }

    @Override
    public Object timestamp(OnPullRequestMergedTrackingEvent event) {
        return event.mergedAt();
    }
}
