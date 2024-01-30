package onlydust.com.marketplace.accounting.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
public class ContributorId {
    @NonNull
    private final Long value;

    public static ContributorId of(@NonNull final String uuid) {
        return ContributorId.of(Long.parseLong(uuid));
    }

    public Long value() {
        return value;
    }
}