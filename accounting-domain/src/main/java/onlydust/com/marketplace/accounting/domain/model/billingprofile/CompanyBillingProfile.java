package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@SuperBuilder(toBuilder = true)
public class CompanyBillingProfile extends BillingProfile {
    @Override
    public boolean isAdmin(UserId userId) {
        return members.stream().anyMatch(user -> user.id().equals(userId) && user.role() == User.Role.ADMIN);
    }

    @NonNull
    private final Set<User> members;
    @NonNull
    private final Kyb kyb;

    public CompanyBillingProfile(@NonNull String name, @NonNull UserId firstAdmin) {
        super(name);
        this.members = new HashSet<>(Set.of(new User(firstAdmin, User.Role.ADMIN, ZonedDateTime.now())));
        this.kyb = Kyb.initForUserAndBillingProfile(firstAdmin, this.id());
    }

    @Override
    public boolean isMember(UserId userId) {
        return members.stream().anyMatch(user -> user.id().equals(userId));
    }

    @Override
    public String subject() {
        return kyb().getName();
    }

    @Override
    public Type type() {
        return Type.COMPANY;
    }

    public boolean isSwitchableToSelfEmployed() {
        return members.size() == 1;
    }

    @Override
    public boolean isSwitchableToCompany() {
        return false;
    }

    public void addMember(UserId userId, User.Role role) {
        members.add(new User(userId, role, ZonedDateTime.now()));
    }

    public void removeMember(UserId userId) {
        if (members.stream().filter(user -> user.role() == User.Role.ADMIN).count() == 1
            && members.stream().anyMatch(user -> user.id().equals(userId) && user.role() == User.Role.ADMIN))
            throw OnlyDustException.badRequest("Cannot remove last admin %s from company billing profile".formatted(userId));
        members.removeIf(user -> user.id().equals(userId));
    }
}
