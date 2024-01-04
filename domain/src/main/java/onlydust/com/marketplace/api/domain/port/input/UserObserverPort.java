package onlydust.com.marketplace.api.domain.port.input;

import onlydust.com.marketplace.api.domain.model.User;

public interface UserObserverPort {

  void onUserSignedUp(User user);
}
