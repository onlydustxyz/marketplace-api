package onlydust.com.marketplace.api.domain.model;

import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class ProjectCreatedOldEvent extends OldEvent {
    public ProjectCreatedOldEvent(UUID id) {
        super("PROJECT", id, "{\"Created\": {\"id\": \"%s\"}}".formatted(id));
    }
}
