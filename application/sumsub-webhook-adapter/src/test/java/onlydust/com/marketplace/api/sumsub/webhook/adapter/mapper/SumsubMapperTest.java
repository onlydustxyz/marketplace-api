package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import onlydust.com.marketplace.project.domain.model.OldBillingProfileType;
import onlydust.com.marketplace.project.domain.model.OldVerificationStatus;
import onlydust.com.marketplace.project.domain.model.notification.BillingProfileUpdated;
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
        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "GREEN", null)).getOldVerificationStatus(),
                OldVerificationStatus.VERIFIED);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("prechecked", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("queued", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("onHold", "GREEN", null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);

        assertEquals(sumsubMapper.apply(stubSumsubEvent("init", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.STARTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("pending", null, null)).getOldVerificationStatus(),
                OldVerificationStatus.UNDER_REVIEW);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "GREEN", null)).getOldVerificationStatus(),
                OldVerificationStatus.VERIFIED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "RED", "RETRY")).getOldVerificationStatus(),
                OldVerificationStatus.REJECTED);
        assertEquals(sumsubMapper.apply(stubSumsubEvent("completed", "RED", "FINAL")).getOldVerificationStatus(),
                OldVerificationStatus.CLOSED);
    }


    private SumsubWebhookEventDTO stubSumsubEvent(final String reviewStatus, final String reviewResult, final String decision) {
        final SumsubWebhookEventDTO sumsubWebhookEventDTO = new SumsubWebhookEventDTO();
        sumsubWebhookEventDTO.setApplicantType("individual");
        sumsubWebhookEventDTO.setExternalUserId(externalId.toString());
        sumsubWebhookEventDTO.setReviewStatus(reviewStatus);
        final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO = new SumsubWebhookEventDTO.ReviewResultDTO();
        reviewResultDTO.setReviewAnswer(reviewResult);
        if (nonNull(decision)) {
            reviewResultDTO.setReviewRejectType(decision);
        }
        sumsubWebhookEventDTO.setReviewResult(reviewResultDTO);
        return sumsubWebhookEventDTO;
    }

    private BillingProfileUpdated expectedStatus(final OldVerificationStatus oldVerificationStatus) {
        return BillingProfileUpdated.builder()
                .billingProfileId(externalId).type(OldBillingProfileType.INDIVIDUAL).oldVerificationStatus(oldVerificationStatus).build();
    }
}
