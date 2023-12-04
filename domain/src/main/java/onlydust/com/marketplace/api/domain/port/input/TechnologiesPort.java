package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.User;

public interface TechnologiesPort {
    void suggest(String name, User requester);
}
