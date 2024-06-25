package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.helper.UserAuthHelper;
import org.junit.jupiter.api.Test;

public class IssuesApiIT extends AbstractMarketplaceApiIT {

    @Test
    void should_return_issue_by_id() {
        final UserAuthHelper.AuthenticatedUser olivier = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(getApiURI(String.format(ISSUES_BY_ID, "1736474921")))
                .header("Authorization", "Bearer " + olivier.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": 1736474921,
                          "number": 1111,
                          "title": "Documentation by AnthonyBuisset",
                          "status": "OPEN",
                          "htmlUrl": "https://github.com/od-mocks/cool.repo.B/issues/1111"
                        }
                        """);
    }
}
