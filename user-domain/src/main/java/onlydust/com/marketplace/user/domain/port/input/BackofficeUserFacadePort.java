package onlydust.com.marketplace.user.domain.port.input;

import onlydust.com.marketplace.user.domain.model.BackofficeUser;

public interface BackofficeUserFacadePort {
    BackofficeUser getUserByIdentity(BackofficeUser.Identity identity);
}
