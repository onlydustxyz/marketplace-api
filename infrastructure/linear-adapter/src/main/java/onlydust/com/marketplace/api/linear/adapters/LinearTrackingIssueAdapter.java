package onlydust.com.marketplace.api.linear.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.output.TrackingIssuePort;
import onlydust.com.marketplace.api.linear.LinearGraphqlClient;

import java.util.Map;

@AllArgsConstructor
public class LinearTrackingIssueAdapter implements TrackingIssuePort {
    private final LinearGraphqlClient client;
    private final LinearGraphqlClient.Config config;

    @Override
    public void createIssueForTechTeam(String title, String description) {
        final var engineeringTeam = config.team(LinearGraphqlClient.Team.Keys.Engineering);
        final var backlogState = engineeringTeam.state(LinearGraphqlClient.State.Keys.Backlog);
        final var label = config.label(LinearGraphqlClient.Label.Keys.TechStuff);

        final var response = client.graphql("""
                mutation($title: String!, $description: String!, $teamId: String!, $stateId: String!, $labelId: String!) {
                    issueCreate(input: { title: $title, description: $description, teamId: $teamId, stateId: $stateId, labelIds: [$labelId] }) {
                        success
                    }
                }
                """, Map.of(
                "title", title,
                "description", description,
                "teamId", engineeringTeam.getId(),
                "stateId", backlogState.getId(),
                "labelId", label.getId()
        ), JsonNode.class);

        if (!response.map(r -> r.at("/data/issueCreate/success").asBoolean()).orElse(false)) {
            throw OnlyDustException.internalServerError("Unable to create issue in Linear");
        }
    }
}
