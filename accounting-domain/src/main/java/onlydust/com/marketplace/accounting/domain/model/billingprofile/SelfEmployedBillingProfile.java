package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

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
        this.owner = new User(ownerId, User.Role.ADMIN);
        this.kyb = Kyb.initForUserAndBillingProfile(ownerId, this.id());
    }

    @Override
    public Type type() {
        return Type.SELF_EMPLOYED;
    }

    public boolean isSwitchableToCompany() {
        return true;
    }
}
