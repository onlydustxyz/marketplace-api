package onlydust.com.marketplace.api.sumsub.webhook.adapter.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SumsubWebhookDTO {
    @JsonProperty("applicantId")
    String applicantId;
    @JsonProperty("inspectionId")
    String inspectionId;
    @JsonProperty("externalUserId")
    String externalUserId;
}
