package onlydust.com.marketplace.api.domain.model;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
public abstract class OldEvent {

  final public String aggregateName;
  final public UUID aggregateId;
  final public String payload;
}
