package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.kernel.model.UuidWrapper;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
@Accessors(fluent = true)
public abstract class BillingProfile {
    @NonNull
    protected final Id id;
    @NonNull
    protected final String name;
    @NonNull
    protected final VerificationStatus status;
    @NonNull
    protected final Boolean enabled;

    protected ZonedDateTime invoiceMandateAcceptedAt;
    protected ZonedDateTime invoiceMandateLatestVersionDate;

    public void acceptMandate() {
        invoiceMandateAcceptedAt = ZonedDateTime.now();
    }

    public abstract String subject();

    protected BillingProfile(@NonNull String name) {
        this.id = Id.random();
        this.name = name;
        this.status = VerificationStatus.NOT_STARTED;
        this.enabled = true;
    }

    public abstract Type type();

    public boolean isVerified() {
        return status == VerificationStatus.VERIFIED;
    }

    public abstract boolean isInvoiceMandateAccepted();

    public abstract boolean isAdmin(UserId userId);

    public abstract boolean isMember(UserId userId);

    public abstract boolean isSwitchableToSelfEmployed();

    public abstract boolean isSwitchableToCompany();

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

    public record User(@NonNull UserId id, @NonNull Role role, @NonNull ZonedDateTime joinedAt) {
        public enum Role {
            ADMIN, MEMBER;

            public static Set<Role> all() {
                return EnumSet.allOf(Role.class);
            }
        }
    }
}
