package onlydust.com.marketplace.api.it.api;

import com.onlydust.customer.io.adapter.properties.CustomerIOProperties;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.notification.*;
import onlydust.com.marketplace.accounting.domain.notification.dto.ShortReward;
import onlydust.com.marketplace.api.contract.model.NotificationPageResponse;
import onlydust.com.marketplace.api.contract.model.NotificationPatchRequest;
import onlydust.com.marketplace.api.contract.model.NotificationStatus;
import onlydust.com.marketplace.api.contract.model.NotificationsPatchRequest;
import onlydust.com.marketplace.api.helper.CurrencyHelper;
import onlydust.com.marketplace.api.helper.UserAuthHelper;
import onlydust.com.marketplace.api.suites.tags.TagMe;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.kernel.model.notification.NotificationCategory;
import onlydust.com.marketplace.kernel.model.notification.NotificationChannel;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;
import onlydust.com.marketplace.project.domain.model.notification.*;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationDetailedIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationIssue;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationProject;
import onlydust.com.marketplace.project.domain.model.notification.dto.NotificationUser;
import onlydust.com.marketplace.user.domain.job.NotificationSummaryEmailJob;
import onlydust.com.marketplace.user.domain.model.NotificationRecipient;
import onlydust.com.marketplace.user.domain.model.NotificationSettings;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.time.ZoneOffset.UTC;

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
        projectHelper.updateVisibility(ProjectId.of(bretzel), ProjectVisibility.PRIVATE);
        final var sponsor = sponsorHelper.create();
        final var program = programHelper.create(sponsor.id());

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
        notificationService.push(hayden.user().getId(), BillingProfileVerificationRejected.builder()
                .billingProfileId(BillingProfile.Id.of(UUID.fromString("9222c39d-7c85-4cc4-8089-c1830bc457b0")))
                .billingProfileName("bpPierreRejected")
                .rejectionReason("rejectionReason1")
                .build());
        notificationService.push(hayden.user().getId(), ApplicationRefused.builder()
                .issue(new NotificationIssue(2L, faker.internet().url(), "title2", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "bbb", "bBb"))
                .build());
        notificationService.push(hayden.user().getId(), GoodFirstIssueCreated.builder()
                .project(new NotificationProject(bretzel, "bbb", "bBb"))
                .issue(new NotificationDetailedIssue(1111L, faker.internet().url(), "gfi-1", faker.rickAndMorty().character(), null, faker.pokemon().name(),
                        faker.internet().url(), List.of()))
                .build());
        notificationService.push(hayden.user().getId(), FundsAllocatedToProgram.builder()
                .amount(BigDecimal.valueOf(100))
                .currencyId(CurrencyHelper.USDC.value())
                .programId(program.id())
                .sponsorId(sponsor.id())
                .build());
        notificationService.push(hayden.user().getId(), FundsUnallocatedFromProgram.builder()
                .amount(BigDecimal.valueOf(99))
                .currencyId(CurrencyHelper.STRK.value())
                .programId(program.id())
                .sponsorId(sponsor.id())
                .build());
        notificationService.push(hayden.user().getId(), DepositRejected.builder()
                .amount(BigDecimal.valueOf(30000))
                .currencyId(CurrencyHelper.STRK.value())
                .sponsorId(sponsor.id())
                .timestamp(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, UTC))
                .depositId(UUID.fromString("f216c7ad-1875-49a2-a8a8-c65b2d6d0675"))
                .build());
        notificationService.push(hayden.user().getId(), DepositApproved.builder()
                .amount(BigDecimal.valueOf(40000))
                .currencyId(CurrencyHelper.ETH.value())
                .sponsorId(sponsor.id())
                .timestamp(ZonedDateTime.of(2023, 3, 2, 0, 0, 0, 0, UTC))
                .depositId(UUID.fromString("a216c7ad-1875-49a2-a8a8-c65b2d6d06aa"))
                .build());

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "1000")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.notifications[?(@.type == 'SPONSOR_LEAD_DEPOSIT_APPROVED')].data.sponsorLeadDepositApproved.sponsor.id").isEqualTo(sponsor.id().toString())
                .jsonPath("$.notifications[?(@.type == 'SPONSOR_LEAD_DEPOSIT_REJECTED')].data.sponsorLeadDepositRejected.sponsor.id").isEqualTo(sponsor.id().toString())
                .jsonPath("$.notifications[?(@.type == 'SPONSOR_LEAD_FUNDS_UNALLOCATED_FROM_PROGRAM')].data.sponsorLeadFundsUnallocatedFromProgram.program" +
                          ".id").isEqualTo(program.id().toString())
                .jsonPath("$.notifications[?(@.type == 'SPONSOR_LEAD_FUNDS_UNALLOCATED_FROM_PROGRAM')].data.sponsorLeadFundsUnallocatedFromProgram.sponsor" +
                          ".id").isEqualTo(sponsor.id().toString())
                .jsonPath("$.notifications[?(@.type == 'PROGRAM_LEAD_FUNDS_ALLOCATED_TO_PROGRAM')].data.programLeadFundsAllocatedToProgram.program.id").isEqualTo(program.id().toString())
                .jsonPath("$.notifications[?(@.type == 'PROGRAM_LEAD_FUNDS_ALLOCATED_TO_PROGRAM')].data.programLeadFundsAllocatedToProgram.sponsor.id").isEqualTo(sponsor.id().toString())
                .json("""
                        {
                          "totalPageNumber": 1,
                          "totalItemNumber": 15,
                          "hasMore": false,
                          "nextPageIndex": 0,
                          "notifications": [
                            {
                              "status": "UNREAD",
                              "type": "SPONSOR_LEAD_DEPOSIT_APPROVED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": {
                                  "amount": 40000,
                                  "currencyCode": "ETH",
                                  "timestamp": "2023-03-02T00:00:00Z"
                                },
                                "sponsorLeadDepositRejected": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "SPONSOR_LEAD_DEPOSIT_REJECTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": {
                                  "amount": 30000,
                                  "currencyCode": "STRK",
                                  "timestamp": "2021-01-01T00:00:00Z"
                                }
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "SPONSOR_LEAD_FUNDS_UNALLOCATED_FROM_PROGRAM",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": {
                                  "amount": 99,
                                  "currencyCode": "STRK"
                                },
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "PROGRAM_LEAD_FUNDS_ALLOCATED_TO_PROGRAM",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": {
                                  "amount": 100,
                                  "currencyCode": "USDC"
                                },
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_PROJECT_GOOD_FIRST_ISSUE_CREATED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": {
                                  "projectName": "Bretzel",
                                  "projectSlug": "bretzel",
                                  "issueId": 1111,
                                  "issueName": "gfi-1"
                                },
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "CONTRIBUTOR_PROJECT_APPLICATION_REFUSED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": {
                                  "projectName": "Bretzel",
                                  "projectSlug": "bretzel",
                                  "issueId": 2,
                                  "issueName": "title2"
                                },
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            },
                            {
                              "status": "UNREAD",
                              "type": "GLOBAL_BILLING_PROFILE_VERIFICATION_REJECTED",
                              "data": {
                                "maintainerApplicationToReview": null,
                                "maintainerCommitteeApplicationCreated": null,
                                "contributorInvoiceRejected": null,
                                "contributorRewardCanceled": null,
                                "contributorRewardReceived": null,
                                "contributorRewardsPaid": null,
                                "contributorProjectApplicationAccepted": null,
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": {
                                  "billingProfileId": "9222c39d-7c85-4cc4-8089-c1830bc457b0",
                                  "billingProfileName": "bpPierreRejected",
                                  "reason": "rejectionReason1"
                                },
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            },
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": {
                                  "billingProfileId": "a805e770-104b-4010-b849-cbf90b93ccf4",
                                  "billingProfileName": "bpHaydenClosed"
                                },
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
                              }
                            }
                          ]
                        }
                        """);

        // When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "1", "pageSize", "10", "status", "UNREAD")))
                .header("Authorization", "Bearer " + hayden.jwt())
                // Then
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .json("""
                        {
                          "totalPageNumber": 2,
                          "totalItemNumber": 5,
                          "hasMore": false,
                          "nextPageIndex": 1,
                          "notifications": [
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                                "contributorProjectApplicationRefused": null,
                                "contributorProjectGoodFirstIssueCreated": null,
                                "globalBillingProfileReminder": null,
                                "globalBillingProfileVerificationRejected": null,
                                "globalBillingProfileVerificationClosed": null,
                                "programLeadFundsAllocatedToProgram": null,
                                "sponsorLeadFundsUnallocatedFromProgram": null,
                                "sponsorLeadDepositApproved": null,
                                "sponsorLeadDepositRejected": null
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
                              "count": 15
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
                              "count": 15
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

// When
        client.get()
                .uri(getApiURI(ME_NOTIFICATIONS, Map.of("pageIndex", "0", "pageSize", "10")))
                .header("Authorization", "Bearer " + pierre.jwt())
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

    private static ShortReward shortRewardStub(double val, String currencyCode) {
        return ShortReward.builder()
                .id(RewardId.of(UUID.fromString("f216c7ad-1875-49a2-a8a8-c65b2d6d0675")))
                .amount(BigDecimal.valueOf(val))
                .currencyCode(currencyCode)
                .dollarsEquivalent(BigDecimal.valueOf(val))
                .projectName("project-%s-%s".formatted(val, currencyCode))
                .sentByGithubLogin("sender-%s-%s".formatted(val, currencyCode))
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

    @Autowired
    NotificationSummaryEmailJob notificationSummaryEmailJob;
    @Autowired
    CustomerIOProperties customerIOProperties;

    @Test
    @Order(3)
    void should_send_summary_emails() throws InterruptedException {
        // Given
        final UserAuthHelper.AuthenticatedUser hayden = userAuthHelper.authenticateHayden();
        final UserAuthHelper.AuthenticatedUser pierre = userAuthHelper.authenticatePierre();

        notificationSettingsService.updateNotificationSettings(NotificationRecipient.Id.of(pierre.user().getId()), inAppAndSummaryEmailSettings());
        notificationSettingsService.updateNotificationSettings(NotificationRecipient.Id.of(hayden.user().getId()), inAppAndSummaryEmailSettings());

        notificationService.push(hayden.user().getId(), CommitteeApplicationCreated.builder()
                .applicationEndDate(ZonedDateTime.now())
                .committeeId(UUID.fromString("8be639e3-86e1-4a8f-a790-e8fef6a78f74"))
                .projectId(UUID.fromString("dd227344-b2ab-471f-88be-ad9c3a4dd72b"))
                .projectName("committeeProject2")
                .committeeName("committee2")
                .build());
        notificationService.push(hayden.user().getId(), ApplicationToReview.builder()
                .issue(new NotificationIssue(1L, faker.internet().url(), "issue2", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "ccc", "CCC"))
                .user(new NotificationUser(hayden.user().getId(), hayden.user().getGithubUserId(), hayden.user().getGithubLogin()))
                .build());
        notificationService.push(hayden.user().getId(), InvoiceRejected.builder()
                .billingProfileId(UUID.fromString("3161998c-fd33-4e5d-9c67-0e7c7da0b942"))
                .invoiceName("invoice2")
                .rejectionReason("rejectionReason2")
                .rewards(List.of(shortRewardStub(45, "ETH"), shortRewardStub(765, "OP")))
                .build());
        notificationService.push(hayden.user().getId(), RewardCanceled.builder()
                .shortReward(shortRewardStub(33.44, "WLD"))
                .build());
        notificationService.push(pierre.user().getId(), RewardReceived.builder()
                .contributionCount(4)
                .sentByGithubLogin("projectLead2")
                .shortReward(shortRewardStub(22.2, "BTC"))
                .build());
        notificationService.push(pierre.user().getId(), RewardsPaid.builder()
                .shortRewards(List.of(shortRewardStub(11123.3, "USD"), shortRewardStub(45.3, "STRK")))
                .build());
        notificationService.push(hayden.user().getId(), ApplicationAccepted.builder()
                .issue(new NotificationIssue(2L, faker.internet().url(), "title2", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "ddd", "DDD"))
                .build());
        notificationService.push(hayden.user().getId(), ApplicationRefused.builder()
                .issue(new NotificationIssue(3L, faker.internet().url(), "title3", faker.rickAndMorty().location(),
                        faker.lorem().characters()))
                .project(new NotificationProject(bretzel, "abc", "ABC"))
                .build());
        notificationService.push(hayden.user().getId(), BillingProfileVerificationClosed.builder()
                .billingProfileId(BillingProfile.Id.of(UUID.fromString("6230df7a-f6c9-4cc4-930c-b310d83c0703")))
                .billingProfileName("bpHaydenClosed2")
                .build());
        notificationService.push(pierre.user().getId(), BillingProfileVerificationRejected.builder()
                .billingProfileId(BillingProfile.Id.of(UUID.fromString("069632d9-6fa2-46d9-80e8-608661309e64")))
                .billingProfileName("bpPierreRejected2")
                .rejectionReason("rejectionReason3")
                .build());
        notificationService.push(pierre.user().getId(), GoodFirstIssueCreated.builder()
                .project(new NotificationProject(bretzel, "ddd", "DDD"))
                .issue(new NotificationDetailedIssue(22L, faker.rickAndMorty().character(), "gfi-2", faker.rickAndMorty().character(), null,
                        faker.pokemon().name(), faker.internet().url(), List.of()))
                .build());

        // When
        notificationSummaryEmailJob.run();
        Thread.sleep(2000L);

        // Then
        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(equalToJson("""
                                {
                                   "transactional_message_id": "70",
                                   "identifiers": {
                                     "id": "fc92397c-3431-4a84-8054-845376b630a0",
                                     "email": "pierre.oucif@gadz.org"
                                   },
                                   "to": "pierre.oucif@gadz.org",
                                   "subject": "Weekly notifications",
                                   "message_data": {
                                     "title": "Weekly notifications",
                                     "username": "PierreOucif",
                                     "description": "Here is a summary of your notifications from last week. Please review them at your convenience.",
                                     "notifications": [
                                       {
                                         "title": "You have received a new reward",
                                         "description": "sender-22.2-BTC sent you a new reward of 22.200 BTC on project project-22.2-BTC",
                                         "button": {
                                           "text": "See details",
                                           "link": "https://develop-app.onlydust.com/rewards"
                                         }
                                       },
                                       {
                                         "title": "Your rewards has been paid",
                                         "description": "2 reward(s) has been paid for a total of 11168.600 USD",
                                         "button": {
                                           "text": "See details",
                                           "link": "https://develop-app.onlydust.com/rewards"
                                         }
                                       },
                                       {
                                         "title": "Your billing profile has been rejected",
                                         "description": "Your billing profile bpPierreRejected2 has been rejected because of : rejectionReason3",
                                         "button": {
                                           "text": "Resume verification",
                                           "link": "https://develop-app.onlydust.com/settings/billing/069632d9-6fa2-46d9-80e8-608661309e64/general-information"
                                         }
                                       },
                                       {
                                         "title" : "New good first issue",
                                         "description" : "New good first issue gfi-2 on project DDD",
                                         "button" : {
                                           "text" : "View issue",
                                           "link" : "https://develop-app.onlydust.com/p/ddd"
                                         }
                                       }
                                     ]
                                   }
                                 }
                                """, true, false)));

        customerIOWireMockServer.verify(1,
                postRequestedFor(urlEqualTo("/send/email"))
                        .withHeader("Content-Type", equalTo("application/json"))
                        .withHeader("Authorization", equalTo("Bearer %s".formatted(customerIOProperties.getApiKey())))
                        .withRequestBody(equalToJson("""
                                {
                                    "transactional_message_id": "70",
                                    "identifiers": {
                                      "id": "eaa1ddf3-fea5-4cef-825b-336f8e775e05",
                                      "email": "haydenclearymusic@gmail.com"
                                    },
                                    "to": "haydenclearymusic@gmail.com",
                                    "subject": "Weekly notifications",
                                    "message_data": {
                                      "title": "Weekly notifications",
                                      "username": "haydencleary",
                                      "description": "Here is a summary of your notifications from last week. Please review them at your convenience.",
                                      "notifications": [
                                        {
                                          "title": "New committee application",
                                          "description": "You have applied to committee2 committee.",
                                          "button": {
                                            "text": "Review my answer",
                                            "link": "https://develop-app.onlydust.com/c/8be639e3-86e1-4a8f-a790-e8fef6a78f74/applicant?p=dd227344-b2ab-471f-88be-ad9c3a4dd72b"
                                          }
                                        },
                                        {
                                          "title": "New contributor application",
                                          "description": "We wanted to inform you that a contributor named haydencleary has applied to work on your issue issue2 on project CCC",
                                          "button": {
                                            "text": "Review",
                                            "link": "https://develop-app.onlydust.com/p/ccc/applications"
                                          }
                                        },
                                        {
                                          "title": "Your invoice has been rejected",
                                          "description": "Your invoice invoice2 has been rejected because of : rejectionReason2",
                                          "button": {
                                            "text": "Upload another invoice",
                                            "link": "https://develop-app.onlydust.com/rewards"
                                          }
                                        },
                                        {
                                          "title": "Your reward has been canceled",
                                          "description": "Your reward of 33.440 WLD has been canceled for the project project-33.44-WLD",
                                          "button": null
                                        },
                                        {
                                          "title": "Your application has been accepted",
                                          "description": "Your application for title2 has been accepted",
                                          "button": {
                                            "text": "See my applications",
                                            "link": "https://develop-app.onlydust.com/applications"
                                          }
                                        },
                                        {
                                          "title": "Your application has been refused",
                                          "description": "Your application for title3 has been refused",
                                          "button": {
                                            "text": "See my applications",
                                            "link": "https://develop-app.onlydust.com/applications"
                                          }
                                        },
                                        {
                                          "title": "Your billing profile has been closed",
                                          "description": "Your billing profile bpHaydenClosed2 has been closed, please contact support for more information",
                                          "button": {
                                            "text": "Contact us",
                                            "link": "https://develop-app.onlydust.com/settings/billing/6230df7a-f6c9-4cc4-930c-b310d83c0703/general-information"
                                          }
                                        }
                                      ]
                                    }
                                  }
                                """, true, false)));
    }

    public static NotificationSettings inAppAndSummaryEmailSettings() {
        final Map<NotificationCategory, List<NotificationChannel>> channelsPerCategory = new HashMap<>();
        for (var notificationCategory : NotificationCategory.values()) {
            channelsPerCategory.put(notificationCategory, List.of(NotificationChannel.IN_APP, NotificationChannel.SUMMARY_EMAIL));
        }
        return NotificationSettings.builder()
                .channelsPerCategory(channelsPerCategory)
                .build();
    }
}
