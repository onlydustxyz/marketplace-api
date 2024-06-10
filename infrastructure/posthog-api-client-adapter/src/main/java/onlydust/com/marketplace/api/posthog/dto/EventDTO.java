package onlydust.com.marketplace.api.posthog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventDTO {
    String event;
    @JsonProperty("api_key")
    String apiKey;
    @JsonProperty("distinct_id")
    String distinctId;
    // ISO 8601 format
    String timestamp;
    JsonNode properties;
}
