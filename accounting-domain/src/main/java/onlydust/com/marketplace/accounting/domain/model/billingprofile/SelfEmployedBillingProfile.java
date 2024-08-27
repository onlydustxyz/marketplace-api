package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.kernel.model.UserId;

import java.time.ZonedDateTime;

@Getter
@Accessors(fluent = true)
@SuperBuilder
public class SelfEmployedBillingProfile extends BillingProfile {
    @NonNull
    private final User owner;

    @NonNull
    private final Kyb kyb;

    public SelfEmployedBillingProfile(@NonNull String name, @NonNull UserId ownerId) {
        super(name);
        this.owner = new User(ownerId, User.Role.ADMIN, ZonedDateTime.now());
        this.kyb = Kyb.initForUserAndBillingProfile(ownerId, this.id());
    }

    @Override
    public boolean isAdmin(UserId userId) {
        return owner.id().equals(userId);
    }

    @Override
    public boolean isMember(UserId userId) {
        return isAdmin(userId);
    }

    @Override
    public String subject() {
        return kyb().getName();
    }

    @Override
    public Type type() {
        return Type.SELF_EMPLOYED;
    }

    @Override
    public boolean isSwitchableToSelfEmployed() {
        return false;
    }

    public boolean isSwitchableToCompany() {
        return true;
    }
}
