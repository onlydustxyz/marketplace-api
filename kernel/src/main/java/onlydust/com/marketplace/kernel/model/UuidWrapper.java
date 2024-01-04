package onlydust.com.marketplace.kernel.model;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

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
