package onlydust.com.marketplace.project.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
public abstract class OldEvent {
    final public String aggregateName;
    final public UUID aggregateId;
    final public String payload;
}
