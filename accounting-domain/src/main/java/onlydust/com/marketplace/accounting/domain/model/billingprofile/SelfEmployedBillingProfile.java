package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.UserId;

@Getter
@Accessors(fluent = true)
public class SelfEmployedBillingProfile extends BillingProfile {
    @NonNull
    private final User owner;
    @NonNull
    private final Kyb kyb;

    public SelfEmployedBillingProfile(@NonNull String name, @NonNull UserId ownerId) {
        super(name);
        this.owner = new User(ownerId, User.Role.ADMIN);
        this.kyb = Kyb.initForUser(ownerId);
    }

    @Override
    public Type type() {
        return Type.COMPANY;
    }

    public boolean isSwitchableToCompany() {
        return true;
    }
}
