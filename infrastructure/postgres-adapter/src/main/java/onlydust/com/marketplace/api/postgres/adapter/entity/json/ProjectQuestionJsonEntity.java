package onlydust.com.marketplace.api.postgres.adapter.entity.json;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record ProjectQuestionJsonEntity(@NonNull String question, @NonNull Boolean required) {
}
