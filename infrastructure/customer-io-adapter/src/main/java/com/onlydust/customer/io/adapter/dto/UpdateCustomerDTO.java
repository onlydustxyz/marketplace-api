package com.onlydust.customer.io.adapter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

import java.util.Map;

public record UpdateCustomerDTO(@NonNull @JsonProperty("cio_subscription_preferences") CioSubscriptionPreferencesDTO cioSubscriptionPreferences) {

    public static UpdateCustomerDTO fromTopicIdAndSubscription(@NonNull Integer topicId, @NonNull Boolean subscribed) {
        return new UpdateCustomerDTO(new CioSubscriptionPreferencesDTO(Map.of("topic_" + topicId, subscribed)));
    }

    public record CioSubscriptionPreferencesDTO(@NonNull Map<String, Boolean> topics) {
    }
}
