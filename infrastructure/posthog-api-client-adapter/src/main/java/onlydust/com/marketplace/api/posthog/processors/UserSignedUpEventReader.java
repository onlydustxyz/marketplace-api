package onlydust.com.marketplace.api.posthog.processors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import onlydust.com.marketplace.project.domain.model.notification.UserSignedUp;

public class UserSignedUpEventReader implements EventReader<UserSignedUp> {
    @Override
    public void addProperties(final UserSignedUp userSignedUp, final ObjectNode properties) {
    }

    @Override
    public String eventType(UserSignedUp userSignedUp) {
        return "user_signed_up";
    }

    @Override
    public Object distinctId(UserSignedUp userSignedUp) {
        return userSignedUp.getUserId().toString();
    }

    @Override
    public Object timestamp(UserSignedUp userSignedUp) {
        return userSignedUp.getSignedUpAt().toInstant();
    }
}
