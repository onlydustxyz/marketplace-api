package onlydust.com.marketplace.api.postgres.adapter.entity.json;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record ApplicationAnswerJsonEntity(@NonNull String question, @NonNull Boolean required, String answer) {
}
