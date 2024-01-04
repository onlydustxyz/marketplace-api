package onlydust.com.marketplace.accounting.domain.model;

import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

@NoArgsConstructor(staticName = "random")
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ProjectId extends UuidWrapper {

  public static ProjectId of(@NonNull final UUID uuid) {
    return ProjectId.builder().uuid(uuid).build();
  }

  public static ProjectId of(@NonNull final String uuid) {
    return ProjectId.of(UUID.fromString(uuid));
  }
}