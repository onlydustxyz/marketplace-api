package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.events.dto.ShortReward;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.notification.RewardCanceled;
import onlydust.com.marketplace.accounting.domain.notification.RewardReceived;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
import onlydust.com.marketplace.user.domain.service.NotificationService;
import onlydust.com.marketplace.user.domain.service.NotificationSettingsService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static onlydust.com.marketplace.kernel.model.notification.NotificationCategory.*;
import static onlydust.com.marketplace.kernel.model.notification.NotificationChannel.DAILY_EMAIL;
import static onlydust.com.marketplace.kernel.model.notification.NotificationChannel.IN_APP;

@TagMe
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeNotificationsIT extends AbstractMarketplaceApiIT {

    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationSettingsService notificationSettingsService;

    @Test
    void shouldReadAllNotificationByStatuses() {
        // Given
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();
        final NotificationSettings notificationSettings = NotificationSettings.builder()
                .channelsPerCategory(Map.of(
                        CONTRIBUTOR_REWARD, List.of(DAILY_EMAIL, IN_APP),
                        KYC_KYB_BILLING_PROFILE, List.of(IN_APP),
                        MAINTAINER_PROJECT_CONTRIBUTOR, List.of(DAILY_EMAIL)))
                .build();
        notificationSettingsService.updateNotificationSettings(NotificationRecipient.Id.of(hayden.user().getId()), notificationSettings);
        notificationService.push(hayden.user().getId(), CommitteeApplicationCreated.builder()
                .applicationEndDate(ZonedDateTime.now())
                .committeeId(UUID.randomUUID())
                .projectId(UUID.randomUUID())
                .projectName(faker.rickAndMorty().character())
                .committeeName(faker.lordOfTheRings().character())
                .build());
        notificationService.push(hayden.user().getId(), RewardCanceled.builder()
                .shortReward(ShortReward.builder()
                        .id(RewardId.random())
                        .amount(BigDecimal.valueOf(11.1))
                        .currencyCode("USD")
                        .dollarsEquivalent(BigDecimal.valueOf(11.1))
                        .projectName(faker.rickAndMorty().character())
                        .build())
                .build());
        notificationService.push(hayden.user().getId(), RewardReceived.builder()
                .contributionCount(3)
                .sentByGithubLogin(faker.rickAndMorty().character())
                .shortReward(ShortReward.builder()
                        .id(RewardId.random())
                        .amount(BigDecimal.valueOf(22.2))
                        .currencyCode("USDC")
                        .dollarsEquivalent(BigDecimal.valueOf(22.2))
                        .projectName(faker.rickAndMorty().character())
                        .build())
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": null,
                              "data": {"maintainerApplicationToReview":null,"maintainerCommitteeApplicationCreated":null}
                            },
                            {
                              "status": "UNREAD",
                              "type": null,
                              "data": {"maintainerApplicationToReview":null,"maintainerCommitteeApplicationCreated":null}
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10", "status", "UNREAD")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 2,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": null,
                              "data": {"maintainerApplicationToReview":null,"maintainerCommitteeApplicationCreated":null}
                            },
                            {
                              "status": "UNREAD",
                              "type": null,
                              "data": {"maintainerApplicationToReview":null,"maintainerCommitteeApplicationCreated":null}
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10", "status", "READ")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                          ]
                        }
                        """);



        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS_COUNT))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                            {
                              "count": 2
                            }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS_COUNT, Map.of("status", "UNREAD")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                            {
                              "count": 2
                            }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS_COUNT, Map.of("status", "READ")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                            {
                              "count": 0
                            }
                        """);




    }
}
