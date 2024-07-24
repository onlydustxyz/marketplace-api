package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.api.suites.tags.TagMe;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.MediaType;

import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.authentication.AuthenticationFilter.BEARER_PREFIX;

@TagMe
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeNotificationSettingsIT extends AbstractMarketplaceApiIT {

    final UUID projectId = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    @Test
    @Order(1)
    void should_return_default_notification_settings_for_project() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(ME_NOTIFICATION_SETTINGS_BY_PROJECT_ID.formatted(projectId))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "7d04163c-4187-4313-8066-61504d34fc56",
                          "slug": "bretzel",
                          "name": "Bretzel",
                          "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                          "onGoodFirstIssueAdded": false
                        }
                        """);
    }

    @Test
    @Order(10)
    void should_patch_notification_settings_for_project() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();

        // When
        client.patch()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS_BY_PROJECT_ID.formatted(projectId)))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "onGoodFirstIssueAdded": true
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
    }

    @Test
    @Order(20)
    void should_return_notification_settings_for_project() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();

        // When
        client.get()
                .uri(ME_NOTIFICATION_SETTINGS_BY_PROJECT_ID.formatted(projectId))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "id": "7d04163c-4187-4313-8066-61504d34fc56",
                          "slug": "bretzel",
                          "name": "Bretzel",
                          "logoUrl": "https://onlydust-app-images.s3.eu-west-1.amazonaws.com/5003677688814069549.png",
                          "onGoodFirstIssueAdded": true
                        }
                        """);
    }
}
