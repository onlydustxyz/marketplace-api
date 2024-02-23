package onlydust.com.marketplace.kernel.model;

import lombok.EqualsAndHashCode;

import java.util.Optional;

@EqualsAndHashCode
public abstract class Event {
    public Optional<String> group() {
        return Optional.empty();
    }
}
