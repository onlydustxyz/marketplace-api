package onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type;

import onlydust.com.marketplace.project.domain.model.ProjectVisibility;

public enum ProjectVisibilityEnumEntity {
    PUBLIC, PRIVATE;

    public ProjectVisibility toDomain() {
        return switch (this) {
            case PUBLIC -> ProjectVisibility.PUBLIC;
            case PRIVATE -> ProjectVisibility.PRIVATE;
        };
    }
}
