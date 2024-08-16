package onlydust.com.marketplace.api.it.api;

import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.api.contract.model.NotificationPageResponse;
import onlydust.com.marketplace.api.contract.model.NotificationPatchRequest;
import onlydust.com.marketplace.api.contract.model.NotificationStatus;
import onlydust.com.marketplace.api.contract.model.NotificationsPatchRequest;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationAccepted;
import onlydust.com.marketplace.project.domain.model.notification.ApplicationToReview;
import onlydust.com.marketplace.project.domain.model.notification.CommitteeApplicationCreated;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationUser;
import onlydust.com.marketplace.user.domain.service.NotificationService;
import onlydust.com.marketplace.user.domain.service.NotificationSettingsService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@TagMe
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeNotificationsIT extends AbstractMarketplaceApiIT {

    @Autowired
    NotificationService notificationService;
    @Autowired
    NotificationSettingsService notificationSettingsService;
    private final UUID bretzel = UUID.fromString("7d04163c-4187-4313-8066-61504d34fc56");

    @Test
    @Order(1)
    void should_read_all_notification_by_statuses() {
        // Given
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        notificationService.push(hayden.user().getId(), CommitteeApplicationCreated.builder()
                .applicationEndDate(ZonedDateTime.now())
                .committeeId(UUID.fromString("0d95a6d2-13c7-45a5-817e-2fd83111dd6a"))
                .projectId(UUID.randomUUID())
                .projectName("committeeProject1")
                .committeeName("committee1")
                .build());
        notificationService.push(hayden.user().getId(), ApplicationToReview.builder()
                .issue(new NotificationIssue(1L, faker.internet().url(), "issue1", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "aaa", "AaA"))
                .user(new NotificationUser(hayden.user().getId(), hayden.user().getGithubUserId(), hayden.user().getGithubLogin()))
                .build());
        notificationService.push(hayden.user().getId(), InvoiceRejected.builder()
                .billingProfileId(UUID.fromString("9c003b18-81f4-40ee-827b-b2046c07d056"))
                .invoiceName("invoice1")
                .rejectionReason("rejectionReason1")
                .rewards(List.of(shortRewardStub(23, "USD"), shortRewardStub(56, "STRK")))
                .build());
        notificationService.push(hayden.user().getId(), RewardCanceled.builder()
                .shortReward(shortRewardStub(11.1, "USD"))
                .build());
        notificationService.push(hayden.user().getId(), RewardReceived.builder()
                .contributionCount(3)
                .sentByGithubLogin("projectLead1")
                .shortReward(shortRewardStub(22.2, "USDC"))
                .build());
        notificationService.push(hayden.user().getId(), RewardsPaid.builder()
                .shortRewards(List.of(shortRewardStub(24, "USD"), shortRewardStub(25, "STRK")))
                .build());
        notificationService.push(hayden.user().getId(), ApplicationAccepted.builder()
                .issue(new NotificationIssue(1L, faker.internet().url(), "title1", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "bbb", "bBb"))
                .build());
        notificationService.push(hayden.user().getId(), BillingProfileVerificationClosed.builder()
                .billingProfileId(BillingProfile.Id.of(UUID.fromString("a805e770-104b-4010-b849-cbf90b93ccf4")))
                .billingProfileName("bpHaydenClosed")
                .build());
        notificationService.push(pierre.user().getId(), BillingProfileVerificationRejected.builder()
                .billingProfileId(BillingProfile.Id.of(UUID.fromString("9222c39d-7c85-4cc4-8089-c1830bc457b0")))
                .billingProfileName("bpPierreRejected")
                .rejectionReason("rejectionReason1")
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
                          "totalItemNumber": 8,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_VERIFICATION_CLOSED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": {
                                  "billingProfileId": "a805e770-104b-4010-b849-cbf90b93ccf4",
                                  "billingProfileName": "bpHaydenClosed"
                                }
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_PROJECT_APPLICATION_ACCEPTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": {
                                  "projectName": "Bretzel",
                                  "projectSlug": "bretzel",
                                  "issueId": 1,
                                  "issueName": "title1"
                                },
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARDS_PAID",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": {
                                  "numberOfRewardPaid": 2,
                                  "totalAmountDollarsEquivalent": 49.0
                                },
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARD_RECEIVED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": {
                                  "rewardId": "f216c7ad-1875-49a2-a8a8-c65b2d6d0675",
                                  "projectName": "project-22.2-USDC",
                                  "amount": 22.2,
                                  "currencyCode": "USDC",
                                  "sentByGithubLogin": "projectLead1",
                                  "contributionCount": 3
                                },
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARD_CANCELED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": {
                                  "rewardId": "f216c7ad-1875-49a2-a8a8-c65b2d6d0675",
                                  "projectName": "project-11.1-USD",
                                  "amount": 11.1,
                                  "currencyCode": "USD"
                                },
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_INVOICE_REJECTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": {
                                  "invoiceName": "invoice1",
                                  "rejectionReason": "rejectionReason1",
                                  "billingProfileId": "9c003b18-81f4-40ee-827b-b2046c07d056"
                                },
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "MAINTAINER_APPLICATION_TO_REVIEW",
                              "data": {
                                "maintainerApplicationToReview": {
                                  "projectSlug": "bretzel",
                                  "projectName": "Bretzel",
                                  "applicantId": 5160414,
                                  "issueId": 1,
                                  "issueName": "issue1",
                                  "applicationLogin": "haydencleary"
                                },
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "MAINTAINER_COMMITTEE_APPLICATION_CREATED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": {
                                  "committeeName": "committee1",
                                  "committeeId": "0d95a6d2-13c7-45a5-817e-2fd83111dd6a"
                                },
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
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
                          "totalItemNumber": 8,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_VERIFICATION_CLOSED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": {
                                  "billingProfileId": "a805e770-104b-4010-b849-cbf90b93ccf4",
                                  "billingProfileName": "bpHaydenClosed"
                                }
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_PROJECT_APPLICATION_ACCEPTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": {
                                  "projectName": "Bretzel",
                                  "projectSlug": "bretzel",
                                  "issueId": 1,
                                  "issueName": "title1"
                                },
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARDS_PAID",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": {
                                  "numberOfRewardPaid": 2,
                                  "totalAmountDollarsEquivalent": 49.0
                                },
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARD_RECEIVED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": {
                                  "rewardId": "f216c7ad-1875-49a2-a8a8-c65b2d6d0675",
                                  "projectName": "project-22.2-USDC",
                                  "amount": 22.2,
                                  "currencyCode": "USDC",
                                  "sentByGithubLogin": "projectLead1",
                                  "contributionCount": 3
                                },
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_REWARD_CANCELED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": {
                                  "rewardId": "f216c7ad-1875-49a2-a8a8-c65b2d6d0675",
                                  "projectName": "project-11.1-USD",
                                  "amount": 11.1,
                                  "currencyCode": "USD"
                                },
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_INVOICE_REJECTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": {
                                  "invoiceName": "invoice1",
                                  "rejectionReason": "rejectionReason1",
                                  "billingProfileId": "9c003b18-81f4-40ee-827b-b2046c07d056"
                                },
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "MAINTAINER_APPLICATION_TO_REVIEW",
                              "data": {
                                "maintainerApplicationToReview": {
                                  "projectSlug": "bretzel",
                                  "projectName": "Bretzel",
                                  "applicantId": 5160414,
                                  "issueId": 1,
                                  "issueName": "issue1",
                                  "applicationLogin": "haydencleary"
                                },
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "MAINTAINER_COMMITTEE_APPLICATION_CREATED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": {
                                  "committeeName": "committee1",
                                  "committeeId": "0d95a6d2-13c7-45a5-817e-2fd83111dd6a"
                                },
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null
                              }
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
                              "count": 8
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
                              "count": 8
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

    private static ShortReward shortRewardStub(double val, String currencyCode) {
        return ShortReward.builder()
                .id(RewardId.of(UUID.fromString("f216c7ad-1875-49a2-a8a8-c65b2d6d0675")))
                .amount(BigDecimal.valueOf(val))
                .currencyCode(currencyCode)
                .dollarsEquivalent(BigDecimal.valueOf(val))
                .projectName("project-%s-%s".formatted(val, currencyCode))
                .build();
    }

    @Test
    @Order(2)
    void should_mark_notifications_as_read() {
        // Given
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();

        final NotificationPageResponse notificationPageResponse = client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10", "status", "UNREAD")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(NotificationPageResponse.class)
                .returnResult()
                .getResponseBody();

        final NotificationsPatchRequest notificationsPatchRequest = new NotificationsPatchRequest();
        final UUID notificationId1 = notificationPageResponse.getNotifications().get(0).getId();
        notificationsPatchRequest.setNotifications(List.of(new NotificationPatchRequest(notificationId1,
                NotificationStatus.READ)));
        // When
        client.patch()
                .uri(getApiURI(ME_NOTIFICATIONS))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(notificationsPatchRequest)
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.notifications[?(@.id == '%s')].status".formatted(notificationId1)).isEqualTo("READ");

        // When
        client.patch()
                .uri(getApiURI(ME_NOTIFICATIONS_ALL))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .isNoContent();

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
                          "totalPageNumber": 0,
                          "totalItemNumber": 0,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                          ]
                        }
                        """);
    }
}
