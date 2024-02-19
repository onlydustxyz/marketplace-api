package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.UserId;

@Getter
@Accessors(fluent = true)
public class IndividualBillingProfile extends BillingProfile {
    @NonNull
    private final User owner;
    @NonNull
    private final Kyc kyc;

    public IndividualBillingProfile(@NonNull String name, @NonNull UserId ownerId) {
        super(name);
        this.owner = new User(ownerId, User.Role.ADMIN);
        this.kyc = Kyc.initForUser(ownerId);
    }

    @Override
    public Type type() {
        return Type.INDIVIDUAL;
    }
}
