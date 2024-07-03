package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.event.OnApplicationCreatedTrackingEvent;

public class ApplicationCreatedEventReader implements EventReader<OnApplicationCreatedTrackingEvent> {
    @Override
    public void addProperties(final OnApplicationCreatedTrackingEvent event, final ObjectNode properties) {
        properties.put("application_id", event.applicationId().toString());
        properties.put("project_id", event.projectId().toString());
        properties.put("issue_id", event.issueId().toString());
        properties.put("applicant_user_id", event.applicantUserId() == null ? null : event.applicantUserId().toString());
        properties.put("applicant_github_id", event.applicantGithubId());
        properties.put("origin", event.origin().toString());
    }

    @Override
    public String eventType(OnApplicationCreatedTrackingEvent event) {
        return "issue_applied";
    }

    @Override
    public Object distinctId(OnApplicationCreatedTrackingEvent event) {
        return event.applicantUserId() == null ? event.applicationId() : event.applicantUserId();
    }

    @Override
    public Object timestamp(OnApplicationCreatedTrackingEvent event) {
        return event.appliedAt();
    }
}
