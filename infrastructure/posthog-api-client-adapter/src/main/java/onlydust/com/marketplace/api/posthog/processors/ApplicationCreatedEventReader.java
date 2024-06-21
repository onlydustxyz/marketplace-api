package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.event.OnApplicationCreatedTrackingEvent;

public class ApplicationCreatedEventReader implements EventReader<OnApplicationCreatedTrackingEvent> {
    @Override
    public void addProperties(final OnApplicationCreatedTrackingEvent event, final ObjectNode properties) {
        properties.put("application_id", event.applicationId().toString());
        properties.put("project_id", event.projectId().toString());
        properties.put("issue_id", event.issueId().toString());
        properties.put("applicant_github_id", event.applicantGithubId());
        properties.put("origin", event.origin().toString());
        properties.put("availability_score", event.availabilityScore());
        properties.put("best_projects_similarity_score", event.bestProjectsSimilarityScore());
        properties.put("main_repo_language_user_score", event.mainRepoLanguageUserScore());
        properties.put("project_fidelity_score", event.projectFidelityScore());
        properties.put("recommendation_score", event.recommendationScore());
    }

    @Override
    public String eventType(OnApplicationCreatedTrackingEvent event) {
        return "application_created";
    }

    @Override
    public Object distinctId(OnApplicationCreatedTrackingEvent event) {
        return event.applicantGithubId();
    }

    @Override
    public Object timestamp(OnApplicationCreatedTrackingEvent event) {
        return event.appliedAt();
    }
}
