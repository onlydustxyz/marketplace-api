package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.project.domain.model.ProjectVisibility;

import java.util.UUID;

@Builder
public record ProjectShortView(@NonNull UUID id,
                               @NonNull String slug,
                               @NonNull String name,
                               String logoUrl,
                               @NonNull String shortDescription,
                               @NonNull ProjectVisibility visibility) {
}
