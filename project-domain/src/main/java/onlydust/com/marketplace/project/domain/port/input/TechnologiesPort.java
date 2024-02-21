package onlydust.com.marketplace.project.domain.port.input;

import onlydust.com.marketplace.project.domain.model.User;

import java.util.List;

public interface TechnologiesPort {
    void suggest(String name, User requester);

    List<String> getAllUsedTechnologies();
}
