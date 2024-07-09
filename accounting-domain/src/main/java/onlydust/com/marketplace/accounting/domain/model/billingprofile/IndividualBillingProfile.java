package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;

import java.time.ZonedDateTime;

@Getter
@SuperBuilder
@Accessors(fluent = true)
public class IndividualBillingProfile extends BillingProfile {

    @NonNull
    private final User owner;
    @NonNull
    private final Kyc kyc;

    public IndividualBillingProfile(@NonNull String name, @NonNull UserId ownerId) {
        super(name);
        this.owner = new User(ownerId, User.Role.ADMIN, ZonedDateTime.now());
        this.kyc = Kyc.initForUserAndBillingProfile(ownerId, this.id());
    }

    @Override
    public String subject() {
        return kyc().getLastName() == null ? kyc().getFirstName() : kyc().getFirstName() + " " + kyc().getLastName();
    }

    @Override
    public Type type() {
        return Type.INDIVIDUAL;
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
    public boolean isSwitchableToSelfEmployed() {
        return false;
    }

    @Override
    public boolean isSwitchableToCompany() {
        return false;
    }
}
