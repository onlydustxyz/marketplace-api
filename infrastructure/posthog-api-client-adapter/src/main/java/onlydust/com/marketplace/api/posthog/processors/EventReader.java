package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.kernel.model.Event;

public interface EventReader<E extends Event> {
    void addProperties(E event, ObjectNode properties);

    String eventType(E event);

    Object distinctId(E event);

    Object timestamp(E event);
}
