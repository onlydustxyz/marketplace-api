package onlydust.com.marketplace.kernel.model;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@ToString
public class AuthenticatedUser {
    UUID id;
    @Builder.Default
    List<Role> roles = new ArrayList<>();
    Long githubUserId;
    @Builder.Default
    List<UUID> projectsLed = new ArrayList<>();
    @Builder.Default
    List<UUID> administratedBillingProfiles = new ArrayList<>();

    public enum Role {
        ADMIN, USER, INTERNAL_SERVICE, UNSAFE_INTERNAL_SERVICE
    }
}
