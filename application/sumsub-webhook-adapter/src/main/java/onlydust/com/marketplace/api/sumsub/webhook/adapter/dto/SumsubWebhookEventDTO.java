package onlydust.com.marketplace.api.sumsub.webhook.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.model.EventType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EventType("SumsubWebhookEventDTO")
public class SumsubWebhookEventDTO extends Event {
    @JsonProperty("applicantId")
    String applicantId;
    @JsonProperty("inspectionId")
    String inspectionId;
    @JsonProperty("correlationId")
    String correlationId;
    @JsonProperty("levelName")
    String levelName;
    @JsonProperty("previousLevelName")
    String previousLevelName;
    @JsonProperty("externalUserId")
    String externalUserId;
    @JsonProperty("type")
    String type;
    @JsonProperty("sandboxMode")
    Boolean sandboxMode;
    @JsonProperty("reviewStatus")
    String reviewStatus;
    @JsonProperty("createdAtMs")
    String createdAtMs;
    @JsonProperty("applicantType")
    String applicantType;
    @JsonProperty("videoIdentReviewStatus")
    String videoIdentReviewStatus;
    @JsonProperty("applicantActionId")
    String applicantActionId;
    @JsonProperty("externalApplicantActionId")
    String externalApplicantActionId;
    @JsonProperty("clientId")
    String clientId;
    @JsonProperty("applicantMemberOf")
    List<ApplicantMemberOfDTO> applicantMemberOf;
    @JsonProperty("reviewResult")
    ReviewResultDTO reviewResult;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ReviewResultDTO {
        @JsonProperty("moderationComment")
        String moderationComment;
        @JsonProperty("clientComment")
        String clientComment;
        @JsonProperty("reviewAnswer")
        String reviewAnswer;
        @JsonProperty("rejectLabels")
        List<String> rejectLabels;
        @JsonProperty("reviewRejectType")
        String reviewRejectType;
        @JsonProperty("buttonIds")
        List<String> buttonIds;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ApplicantMemberOfDTO {
        @JsonProperty("applicantId")
        String applicationId;
    }
}
