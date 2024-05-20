package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;


@Builder
public record ShortSponsorView(@NonNull String name, @NonNull String logoUrl) {
}
