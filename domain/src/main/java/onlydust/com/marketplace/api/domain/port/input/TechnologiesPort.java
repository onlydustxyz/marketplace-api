package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface TechnologiesPort {
    void suggest(String name, User requester);

    void refreshTechnologies(UUID projectId);

    void refreshTechnologies(List<Long> repoIds);
}
