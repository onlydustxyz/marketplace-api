package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public abstract class BillingProfile {

    @NonNull
    protected final Id id;
    @NonNull
    protected final String name;

    protected BillingProfile(@NonNull String name) {
        this.id = Id.random();
        this.name = name;
    }

    public abstract Type type();

    public abstract VerificationStatus status();

    @NoArgsConstructor(staticName = "random")
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    public static class Id extends UuidWrapper {
        public static Id of(@NonNull final UUID uuid) {
            return Id.builder().uuid(uuid).build();
        }

        public static Id of(@NonNull final String uuid) {
            return Id.of(UUID.fromString(uuid));
        }
    }

    public enum Type {
        INDIVIDUAL, COMPANY, SELF_EMPLOYED
    }

    public record User(@NonNull UserId id, @NonNull Role role) {
        public enum Role {
            ADMIN, MEMBER;

            public static Set<Role> all() {
                return EnumSet.allOf(Role.class);
            }
        }
    }
}
