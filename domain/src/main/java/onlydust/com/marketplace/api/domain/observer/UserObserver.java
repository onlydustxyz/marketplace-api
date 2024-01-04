package onlydust.com.marketplace.api.domain.observer;

import java.util.Date;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.notification.UserSignedUp;
import onlydust.com.marketplace.api.domain.port.input.UserObserverPort;
import onlydust.com.marketplace.api.domain.port.output.OutboxPort;

@AllArgsConstructor
public class UserObserver implements UserObserverPort {

  private final OutboxPort indexerOutbox;

  @Override
  public void onUserSignedUp(User user) {
    indexerOutbox.push(new UserSignedUp(user.getId(), user.getGithubUserId(), user.getGithubLogin(), new Date()));
  }
}
