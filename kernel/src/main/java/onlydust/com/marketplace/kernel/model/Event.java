package onlydust.com.marketplace.kernel.model;

import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

@EqualsAndHashCode
public abstract class Event {
    public @Nullable String group() {
        return null;
    }
}
