package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;

@Builder
public record ProjectShortView(@NonNull UUID id, @NonNull String name, String logoUrl, @NonNull String shortDescription, @NonNull String slug) {
}
