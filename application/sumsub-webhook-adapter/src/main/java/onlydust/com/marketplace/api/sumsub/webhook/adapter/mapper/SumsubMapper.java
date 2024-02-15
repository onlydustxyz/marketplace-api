package onlydust.com.marketplace.api.sumsub.webhook.adapter.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.job.OutboxSkippingException;
import onlydust.com.marketplace.api.domain.model.BillingProfileType;
import onlydust.com.marketplace.api.domain.model.VerificationStatus;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.model.notification.Event;
import onlydust.com.marketplace.api.sumsub.webhook.adapter.dto.SumsubWebhookEventDTO;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
public class SumsubMapper implements Function<Event, BillingProfileUpdated> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /*
    *   Unstarted - user hasn't started process
        Started - user has start process
        Under review - user has submit for review and is waiting results
        Verified - user got approved
        Rejected - user got rejected and can fix problems
        Invalidated - user got invalidated over time and need to take action (ID card is overdue)
        Closed - user is a terrorist babay
    * */

    @Override
    public BillingProfileUpdated apply(Event event) {
        try {
            if (event instanceof SumsubWebhookEventDTO sumsubWebhookEventDTO) {
                final UUID billingProfileId = UUID.fromString(sumsubWebhookEventDTO.getExternalUserId());
                return BillingProfileUpdated.builder()
                        .billingProfileId(billingProfileId)
                        .type(applicationTypeToDomain(sumsubWebhookEventDTO.getApplicantType()))
                        .verificationStatus(typeAndReviewResultToDomain(sumsubWebhookEventDTO.getType(), sumsubWebhookEventDTO.getReviewStatus(),
                                sumsubWebhookEventDTO.getReviewResult()))
                        .reviewMessageForApplicant(reviewMessageForApplicantToDomain(sumsubWebhookEventDTO.getReviewResult()))
                        .rawReviewDetails(rawReviewToString(sumsubWebhookEventDTO.getReviewResult()))
                        .externalVerificationId(sumsubWebhookEventDTO.getApplicantId())
                        .build();
            }
        } catch (Exception ignored) {

        }
        throw new OutboxSkippingException(String.format("Invalid sumsub event format %s", event));
    }

    private BillingProfileType applicationTypeToDomain(final String applicationType) {
        return switch (applicationType) {
            case "individual" -> BillingProfileType.INDIVIDUAL;
            case "company" -> BillingProfileType.COMPANY;
            default -> throw OnlyDustException.internalServerError(String.format("Invalid application type from sumsub : %s",
                    applicationType));
        };
    }

    private String reviewMessageForApplicantToDomain(final SumsubWebhookEventDTO.ReviewResultDTO reviewResult) {
        if (nonNull(reviewResult) && nonNull(reviewResult.getModerationComment())) {
            return reviewResult.getModerationComment();
        }
        return null;
    }

    private VerificationStatus typeAndReviewResultToDomain(final String type, final String reviewStatus,
                                                           final SumsubWebhookEventDTO.ReviewResultDTO reviewResult) {
        return switch (type) {
            case "applicantCreated" -> VerificationStatus.STARTED;
            case "applicantPending" -> VerificationStatus.UNDER_REVIEW;
            case "applicantOnHold" -> VerificationStatus.UNDER_REVIEW;
            case "applicantReviewed" -> switch (reviewStatus) {
                case "completed":
                    final Optional<Answer> answer = reviewResultToAnswer(reviewResult);
                    if (answer.isPresent()) {
                        yield switch (answer.get()) {
                            case RED -> {
                                if (isAFinalRejection(reviewResult)) {
                                    yield VerificationStatus.CLOSED;
                                } else {
                                    yield VerificationStatus.REJECTED;
                                }
                            }
                            case GREEN -> VerificationStatus.VERIFIED;
                        };
                    }
                default:
                    yield VerificationStatus.UNDER_REVIEW;
            };
            default -> VerificationStatus.UNDER_REVIEW;
        };
    }

    private Optional<Answer> reviewResultToAnswer(final SumsubWebhookEventDTO.ReviewResultDTO reviewResult) {
        if (nonNull(reviewResult) && nonNull(reviewResult.getReviewAnswer())) {
            return switch (reviewResult.getReviewAnswer()) {
                case "RED" -> Optional.of(Answer.RED);
                case "GREEN" -> Optional.of(Answer.GREEN);
                default -> throw OnlyDustException.internalServerError(String.format("Invalid Sumsub answer %s", reviewResult.getReviewAnswer()));
            };
        }
        return Optional.empty();
    }

    private Boolean isAFinalRejection(final SumsubWebhookEventDTO.ReviewResultDTO reviewResult) {
        return nonNull(reviewResult) && nonNull(reviewResult.getReviewRejectType()) && reviewResult.getReviewRejectType().equals("FINAL");
    }

    private enum Answer {
        GREEN, RED;
    }

    private String rawReviewToString(final SumsubWebhookEventDTO.ReviewResultDTO reviewResultDTO) {
        if (isNull(reviewResultDTO)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(reviewResultDTO);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Failed to serialize reviewResultDTO %s into string".formatted(reviewResultDTO), e);
            return null;
        }
    }
}
