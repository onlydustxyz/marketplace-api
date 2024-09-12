package onlydust.com.marketplace.user.domain.model;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.UserId;

import java.util.List;
import java.util.Set;

public record BackofficeUser(@NonNull UserId id, @NonNull String email, @NonNull String name, @NonNull Set<Role> roles, String avatarUrl) {
    public enum Role {
        BO_FINANCIAL_ADMIN, BO_MARKETING_ADMIN, BO_READER
    }

    @Builder
    public record Identity(@NonNull String email, @NonNull String name, String avatarUrl) {
    }

    public AuthenticatedUser asAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .id(id)
                .roles(List.of(AuthenticatedUser.Role.INTERNAL_SERVICE))
                .build();
    }
}
