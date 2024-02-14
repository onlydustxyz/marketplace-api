package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;
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
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantCreated", "init", null, null)), expectedStatus(VerificationStatus.STARTED));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantCreated", "pending", null, null)), expectedStatus(VerificationStatus.STARTED));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantCreated", "completed", null, null)), expectedStatus(VerificationStatus.STARTED));

        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantPending", "init", null, null)), expectedStatus(VerificationStatus.UNDER_REVIEW));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantPending", "pending", null, null)), expectedStatus(VerificationStatus.UNDER_REVIEW));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantPending", "completed", null, null)), expectedStatus(VerificationStatus.UNDER_REVIEW));


        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantReviewed", "init", null, null)), expectedStatus(VerificationStatus.UNDER_REVIEW));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantReviewed", "pending", null, null)), expectedStatus(VerificationStatus.UNDER_REVIEW));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantReviewed", "completed", "GREEN", null)), expectedStatus(VerificationStatus.VERIFIED));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantReviewed", "completed", "RED", "RETRY")), expectedStatus(VerificationStatus.REJECTED));
        assertEquals(sumsubMapper.apply(stubSumsubEvent("applicantReviewed", "completed", "RED", "FINAL")), expectedStatus(VerificationStatus.CLOSED));
    }


    private SumsubWebhookEventDTO stubSumsubEvent(final String type, final String reviewStatus, final String reviewResult, final String decision) {
        final SumsubWebhookEventDTO sumsubWebhookEventDTO = new SumsubWebhookEventDTO();
        sumsubWebhookEventDTO.setApplicantType("individual");
        sumsubWebhookEventDTO.setExternalUserId(externalId.toString());
        sumsubWebhookEventDTO.setType(type);
        sumsubWebhookEventDTO.setReviewStatus(reviewStatus);
        final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO = new SumsubWebhookEventDTO.ReviewResultDTO();
        reviewResultDTO.setReviewAnswer(reviewResult);
        if (nonNull(decision)){
            reviewResultDTO.setReviewRejectType(decision);
        }
        sumsubWebhookEventDTO.setReviewResult(reviewResultDTO);
        return sumsubWebhookEventDTO;
    }

    private BillingProfileUpdated expectedStatus(final VerificationStatus verificationStatus) {
        return BillingProfileUpdated.builder()
                .billingProfileId(externalId).type(BillingProfileType.INDIVIDUAL).verificationStatus(verificationStatus).build();
    }
}
