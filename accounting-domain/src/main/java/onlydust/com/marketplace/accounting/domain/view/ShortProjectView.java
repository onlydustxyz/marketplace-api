package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;

@Builder
public record ShortProjectView(@NonNull ProjectId id, @NonNull String name, String logoUrl, @NonNull String shortDescription, @NonNull String slug) {
}
