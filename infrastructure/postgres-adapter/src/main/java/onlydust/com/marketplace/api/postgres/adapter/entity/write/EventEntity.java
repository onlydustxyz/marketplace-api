package onlydust.com.marketplace.api.postgres.adapter.entity.write;

import onlydust.com.marketplace.api.domain.model.notification.Event;

public interface EventEntity {
    Event getEvent();

    void setStatus(Status status);

    void setError(String message);

    enum Status {
        PENDING, PROCESSED, FAILED
    }
}
