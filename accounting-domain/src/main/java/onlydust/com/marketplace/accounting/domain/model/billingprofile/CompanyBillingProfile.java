package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import onlydust.com.marketplace.accounting.domain.model.UserId;

import java.util.Set;

@Getter
@Accessors(fluent = true)
public class CompanyBillingProfile extends BillingProfile {
    @NonNull
    private final Set<User> members;
    @NonNull
    private final Kyb kyb;

    public CompanyBillingProfile(@NonNull String name, @NonNull UserId firstAdmin) {
        super(name);
        this.members = Set.of(new User(firstAdmin, User.Role.ADMIN));
        this.kyb = Kyb.initForUser(firstAdmin);
    }

    @Override
    public Type type() {
        return Type.COMPANY;
    }
}
