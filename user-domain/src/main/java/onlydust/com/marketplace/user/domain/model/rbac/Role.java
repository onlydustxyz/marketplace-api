package onlydust.com.marketplace.user.domain.model.rbac;

import lombok.AllArgsConstructor;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

public class Role {
    private final Map<Resource, Set<Permission>> permissions = new HashMap<>();

    public boolean hasPermission(Resource resource, Permission permission) {
        return permissions.getOrDefault(resource, Collections.emptySet()).contains(permission);
    }

    ResourcePermissionsBuilder on(Resource resource) {
        return new ResourcePermissionsBuilder(this, resource);
    }

    Role inherit(Role... roles) {
        asList(roles).forEach(role -> role.permissions.forEach(this::with));
        return this;
    }

    private Role with(Resource resource, Set<Permission> permissions) {
        this.permissions.computeIfAbsent(resource, r -> new HashSet<>()).addAll(permissions);
        return this;
    }

    @AllArgsConstructor
    static class ResourcePermissionsBuilder {
        private final Role role;
        private final Resource resource;

        Role can(Permission... permissions) {
            return role.with(resource, Arrays.stream(permissions).collect(toSet()));
        }
    }
}
