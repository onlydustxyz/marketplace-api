package onlydust.com.marketplace.api.domain.model;

import java.util.UUID;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ProjectCreatedOldEvent extends OldEvent {

  public ProjectCreatedOldEvent(UUID id) {
    super("PROJECT", id, "{\"Created\": {\"id\": \"%s\"}}".formatted(id));
  }
}
