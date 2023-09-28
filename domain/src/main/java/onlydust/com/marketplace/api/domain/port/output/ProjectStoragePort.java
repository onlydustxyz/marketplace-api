package onlydust.com.marketplace.api.domain.port.output;

import onlydust.com.marketplace.api.domain.model.Project;

import java.util.UUID;

public interface ProjectStoragePort {
    Project getById(UUID projectId);

    Project getBySlug(String slug);
}
