package onlydust.com.marketplace.api.domain.model;

import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
public class ProjectCreatedEvent extends Event {
    public ProjectCreatedEvent(UUID id) {
        super("PROJECT", id, "{\"Created\": {\"id\": \"%s\"}}".formatted(id));
    }
}
