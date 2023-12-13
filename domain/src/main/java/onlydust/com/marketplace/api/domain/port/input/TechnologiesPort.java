package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.User;

import java.util.List;

public interface TechnologiesPort {
    void suggest(String name, User requester);

    List<String> getAllUsedTechnologies();
}
