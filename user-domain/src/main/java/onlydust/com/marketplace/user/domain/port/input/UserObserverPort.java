package onlydust.com.marketplace.user.domain.port.input;


import onlydust.com.marketplace.kernel.model.AuthenticatedUser;

public interface UserObserverPort {
    void onUserSignedUp(AuthenticatedUser user);
}
