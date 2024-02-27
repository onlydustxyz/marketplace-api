package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SumsubMapperTest {

    private final SumsubMapper sumsubMapper = new SumsubMapper();
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
}
