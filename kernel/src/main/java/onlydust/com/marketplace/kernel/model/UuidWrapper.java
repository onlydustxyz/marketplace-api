package onlydust.com.marketplace.kernel.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public abstract class UuidWrapper {
    @NonNull
    @Builder.Default
    private final UUID uuid = UUID.randomUUID();

    public String toString() {
        return uuid.toString();
    }

    public UUID value() {
        return uuid;
    }
}
