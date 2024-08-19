package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.port.in.BillingProfileVerificationRejectionReasonFacadePort;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SumsubMapperTest {

    private SumsubMapper sumsubMapper;
    BillingProfileVerificationRejectionReasonFacadePort billingProfileVerificationRejectionReasonFacadePort;
    private final Faker faker = new Faker();

    @BeforeEach
    void setUp() {
        billingProfileVerificationRejectionReasonFacadePort = mock(BillingProfileVerificationRejectionReasonFacadePort.class);
        sumsubMapper = new SumsubMapper(billingProfileVerificationRejectionReasonFacadePort);
    }

    private final UUID externalId = UUID.randomUUID();

    // type :
    // - applicantReviewed
    // - applicantPending
    // - applicantCreated
    // - applicantOnHold
    // - applicantPersonalInfoChanged
    // - applicantDeleted
    // - applicantLevelChanged
    // - videoIdentStatusChanged
    // - applicantReset
    // - applicantActionPending
    // - applicantActionOnHold
    // - applicantWorkflowCompleted

    // reviewStatus :
    // - init
    // - pending
    // - completed
    @Test
    void should_map_type_and_review_result_to_domain() {
        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getVerificationStatus(),
                VerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getVerificationStatus(),
                VerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "GREEN", null)).getVerificationStatus(),
                VerificationStatus.VERIFIED);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("prechecked", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("queued", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("onHold", "GREEN", null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getVerificationStatus(),
                VerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getVerificationStatus(),
                VerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "GREEN", null)).getVerificationStatus(),
                VerificationStatus.VERIFIED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "RED", "RETRY")).getVerificationStatus(),
                VerificationStatus.REJECTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "RED", "FINAL")).getVerificationStatus(),
                VerificationStatus.CLOSED);
    }

    @Nested
    class ShouldMapRejectionReasons {
        @Test
        void given_one_set_and_a_group_id() {
            // Given
            final String groupId1 = "groupId1";
            final String buttonId1 = "buttonId1";
            final String label1 = "label1";
            final String rejectionReason1 = faker.rickAndMorty().character();
            final SumsubWebhookEventDTO sumsubWebhookEventDTO = stubSumsubEventWithRejectionLabels(List.of("%s_%s".formatted(groupId1, buttonId1)),
                    List.of(label1));

            // When
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason(groupId1, buttonId1, label1)).thenReturn(Optional.of(rejectionReason1));
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = sumsubMapper.apply(sumsubWebhookEventDTO);

            // Then
            assertEquals(billingProfileVerificationUpdated.getReviewMessageForApplicant(), rejectionReason1);
        }

        @Test
        void given_one_set_and_a_no_group_id() {
            // Given
            final String buttonId1 = "buttonId1";
            final String label1 = "label1";
            final String rejectionReason1 = faker.rickAndMorty().character();
            final SumsubWebhookEventDTO sumsubWebhookEventDTO = stubSumsubEventWithRejectionLabels(List.of(buttonId1),
                    List.of(label1));

            // When
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason(null, buttonId1, label1)).thenReturn(Optional.of(rejectionReason1));
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = sumsubMapper.apply(sumsubWebhookEventDTO);

            // Then
            assertEquals(billingProfileVerificationUpdated.getReviewMessageForApplicant(), rejectionReason1);
        }

        @Test
        void return_default_rejection_reason_given_labels_not_found() {
            // Given
            final String groupId1 = "groupId1";
            final String buttonId1 = "buttonId1";
            final String label1 = "label1";
            final SumsubWebhookEventDTO sumsubWebhookEventDTO = stubSumsubEventWithRejectionLabels(List.of("%s_%s".formatted(groupId1, buttonId1)),
                    List.of(label1));

            // When
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason(groupId1, buttonId1, label1)).thenReturn(Optional.empty());
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = sumsubMapper.apply(sumsubWebhookEventDTO);

            // Then
            assertEquals(billingProfileVerificationUpdated.getReviewMessageForApplicant(),
                    "Something went wrong during the verification process, please contact admin@onlydust.xyz");
        }

        @Test
        void return_default_rejection_reason_given_labels_not_found_and_no_group_id() {
            // Given
            final String buttonId1 = "buttonId1";
            final String label1 = "label1";
            final SumsubWebhookEventDTO sumsubWebhookEventDTO = stubSumsubEventWithRejectionLabels(List.of(buttonId1),
                    List.of(label1));

            // When
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason(null, buttonId1, label1)).thenReturn(Optional.empty());
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = sumsubMapper.apply(sumsubWebhookEventDTO);

            // Then
            assertEquals(billingProfileVerificationUpdated.getReviewMessageForApplicant(),
                    "Something went wrong during the verification process, please contact admin@onlydust.xyz");
        }


        @Test
        void given_multiple_values() {
            // Given
            final SumsubWebhookEventDTO sumsubWebhookEventDTO = stubSumsubEventWithRejectionLabels(
                    List.of("groupId1_buttonId11", "groupId1_buttonId12", "buttonId2", "groupId3_buttonId31"),
                    List.of("rejectionLabel1", "rejectionLabel2"));

            // When
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason("groupId1", "buttonId11", "rejectionLabel1")).thenReturn(Optional.of(
                    "rejectionReason11"));
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason("groupId1", "buttonId12", "rejectionLabel1")).thenReturn(Optional.empty());
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason(null, "buttonId2", "rejectionLabel2")).thenReturn(Optional.of(
                    "rejectionReason2"));
            when(billingProfileVerificationRejectionReasonFacadePort.findExternalRejectionReason("groupId3", "buttonId31", "rejectionLabel2")).thenReturn(Optional.of(
                    "rejectionReason3"));
            final BillingProfileVerificationUpdated billingProfileVerificationUpdated = sumsubMapper.apply(sumsubWebhookEventDTO);

            // Then
            assertEquals("rejectionReason11\nrejectionReason2\nrejectionReason3", billingProfileVerificationUpdated.getReviewMessageForApplicant());
        }
    }


    private SumsubWebhookEventDTO stubSumsubEvent(final String reviewStatus, final String reviewResult, final String decision) {
        final SumsubWebhookEventDTO sumsubWebhookEventDTO = new SumsubWebhookEventDTO();
        sumsubWebhookEventDTO.setApplicantType("individual");
        sumsubWebhookEventDTO.setExternalUserId(externalId.toString());
        sumsubWebhookEventDTO.setReviewStatus(reviewStatus);
        sumsubWebhookEventDTO.setApplicantId("fake-applicant-id");
        final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO = new SumsubWebhookEventDTO.ReviewResultDTO();
        reviewResultDTO.setReviewAnswer(reviewResult);
        if (nonNull(decision)) {
            reviewResultDTO.setReviewRejectType(decision);
        }
        sumsubWebhookEventDTO.setReviewResult(reviewResultDTO);
        return sumsubWebhookEventDTO;
    }

    private SumsubWebhookEventDTO stubSumsubEventWithRejectionLabels(final List<String> buttonIds, final List<String> rejectLabels) {
        final SumsubWebhookEventDTO sumsubWebhookEventDTO = new SumsubWebhookEventDTO();
        sumsubWebhookEventDTO.setApplicantType("individual");
        sumsubWebhookEventDTO.setExternalUserId(externalId.toString());
        sumsubWebhookEventDTO.setReviewStatus("completed");
        sumsubWebhookEventDTO.setApplicantId("fake-applicant-id");
        final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO = new SumsubWebhookEventDTO.ReviewResultDTO();
        reviewResultDTO.setRejectLabels(rejectLabels);
        reviewResultDTO.setButtonIds(buttonIds);
        reviewResultDTO.setReviewRejectType("RETRY");
        reviewResultDTO.setReviewAnswer("RED");
        sumsubWebhookEventDTO.setReviewResult(reviewResultDTO);
        return sumsubWebhookEventDTO;
    }
}
