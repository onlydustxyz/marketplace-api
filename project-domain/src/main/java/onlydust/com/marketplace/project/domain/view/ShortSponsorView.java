package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.NonNull;

import java.util.UUID;


@Builder
public record ShortSponsorView(@NonNull UUID id, @NonNull String name, @NonNull String url, @NonNull String logoUrl) {
}
