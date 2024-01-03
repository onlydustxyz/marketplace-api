package onlydust.com.marketplace.kernel.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class UuidWrapper {
    @NonNull
    @Builder.Default
    private final UUID uuid = UUID.randomUUID();

    public String toString() {
        return uuid.toString();
    }
}
