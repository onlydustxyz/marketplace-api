package onlydust.com.marketplace.api.it.api;

import com.github.tomakehurst.wiremock.client.WireMock;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
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

    @Test
    @Order(30)
    void should_patch_again_notification_settings_for_project() {
        // Given
        final var user = userAuthHelper.authenticateOlivier();

        // When
        client.patch()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS_BY_PROJECT_ID.formatted(projectId)))
                .header("Authorization", BEARER_PREFIX + user.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "onGoodFirstIssueAdded": false
                        }
                        """)
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

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
    void should_get_and_put_my_notification_settings() {
        // Given
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS))
                .header("Authorization", BEARER_PREFIX + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "notificationSettings": [
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_PROJECT"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "PROGRAM_LEAD"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "SPONSOR_LEAD"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_REWARD"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "GLOBAL_MARKETING"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "MAINTAINER_PROJECT_PROGRAM"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "GLOBAL_BILLING_PROFILE"
                            }
                          ]
                        }
                        """);

        // When
        client.put()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS))
                .header("Authorization", BEARER_PREFIX + hayden.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "notificationSettings": [
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "GLOBAL_BILLING_PROFILE"
                            },
                            {
                              "channels": [
                                "EMAIL", "SUMMARY_EMAIL"
                              ],
                              "category": "CONTRIBUTOR_REWARD"
                            },
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "MAINTAINER_PROJECT_CONTRIBUTOR"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_PROJECT"
                            }
                          ]
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        customerIOTrackingApiWireMockServer.verify(1, WireMock.putRequestedFor(WireMock.urlEqualTo("/customers/%s".formatted(hayden.user().getEmail())))
                .withHeader("Authorization", WireMock.equalTo("Basic JHtDVVNUT01FUl9JT19UUkFDS0lOR19TSVRFX0lEfToke0NVU1RPTUVSX0lPX1RSQUNLSU5HX0FQSV9LRVl9"))
                .withRequestBody(WireMock.equalToJson("""
                        {"cio_subscription_preferences":{"topics":{"topic_3":false}}}
                        """))
        );

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS))
                .header("Authorization", BEARER_PREFIX + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "notificationSettings": [
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "GLOBAL_BILLING_PROFILE"
                            },
                            {
                              "channels": [
                                "EMAIL", "SUMMARY_EMAIL"
                              ],
                              "category": "CONTRIBUTOR_REWARD"
                            },
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "MAINTAINER_PROJECT_CONTRIBUTOR"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_PROJECT"
                            }
                          ]
                        }
                        """);

        // When
        client.put()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS))
                .header("Authorization", BEARER_PREFIX + hayden.jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "notificationSettings": [
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "GLOBAL_BILLING_PROFILE"
                            },
                            {
                              "channels": [
                                "EMAIL", "SUMMARY_EMAIL"
                              ],
                              "category": "CONTRIBUTOR_REWARD"
                            },
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "MAINTAINER_PROJECT_CONTRIBUTOR"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_PROJECT"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "GLOBAL_MARKETING"
                            }
                          ]
                        }
                        """)
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

        customerIOTrackingApiWireMockServer.verify(1, WireMock.putRequestedFor(WireMock.urlEqualTo("/customers/%s".formatted(hayden.user().getEmail())))
                .withHeader("Authorization", WireMock.equalTo("Basic JHtDVVNUT01FUl9JT19UUkFDS0lOR19TSVRFX0lEfToke0NVU1RPTUVSX0lPX1RSQUNLSU5HX0FQSV9LRVl9"))
                .withRequestBody(WireMock.equalToJson("""
                        {"cio_subscription_preferences":{"topics":{"topic_3":true}}}
                        """))
        );

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATION_SETTINGS))
                .header("Authorization", BEARER_PREFIX + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "notificationSettings": [
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "GLOBAL_BILLING_PROFILE"
                            },
                            {
                              "channels": [
                                "EMAIL", "SUMMARY_EMAIL"
                              ],
                              "category": "CONTRIBUTOR_REWARD"
                            },
                            {
                              "channels": [
                                "SUMMARY_EMAIL"
                              ],
                              "category": "MAINTAINER_PROJECT_CONTRIBUTOR"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "CONTRIBUTOR_PROJECT"
                            },
                            {
                              "channels": [
                                "EMAIL"
                              ],
                              "category": "GLOBAL_MARKETING"
                            }
                          ]
                        }
                        """);

    }
}
