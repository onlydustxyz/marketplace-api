package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.Project;

import java.util.UUID;

public interface ProjectFacadePort {
    Project getById(UUID projectId);

    Project getBySlug(String slug);
}
