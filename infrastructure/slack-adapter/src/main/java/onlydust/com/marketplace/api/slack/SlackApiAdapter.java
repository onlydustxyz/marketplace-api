package onlydust.com.marketplace.api.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;

import static java.util.Objects.nonNull;

@Slf4j
public class SlackApiAdapter implements NotificationPort {

    private final SlackProperties slackProperties;
    private final MethodsClient slackClient;
    private static final String OD_LOGO_URL = "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp";
    private final UserStoragePort userStoragePort;

    public SlackApiAdapter(final SlackProperties slackProperties, final UserStoragePort userStoragePort) {
        this.slackProperties = slackProperties;
        this.slackClient = Slack.getInstance().methods(slackProperties.getToken());
        this.userStoragePort = userStoragePort;
    }

    @Override
    public void notifyNewEvent(Event event) {
        if (event instanceof BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
            notifyNewVerificationEvent(billingProfileVerificationUpdated);
        } else {
            LOGGER.warn("Unmanaged event class %s".formatted(event.getClass()));
        }
    }

    public void notifyNewVerificationEvent(BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        try {
            final User user = userStoragePort.getUserById(billingProfileVerificationUpdated.getUserId().value())
                    .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(billingProfileVerificationUpdated.getUserId().value())));
            final ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(req -> req
                    .channel(slackProperties.getKycKybChannel())
                    .iconUrl(OD_LOGO_URL)
                    .username("[%s] - OnlyDust API".formatted(slackProperties.getEnvironment().toUpperCase()))
                    .text("New KYC/KYB event")
                    .blocksAsString(BillingProfileVerificationEventMapper.billingProfileUpdatedToSlackNotification(billingProfileVerificationUpdated, user,
                            slackProperties.getTagAllChannel()))
            );
            if (nonNull(chatPostMessageResponse.getError()) || nonNull(chatPostMessageResponse.getWarning())) {
                LOGGER.warn("Error or warning when sending notification to slack : %s".formatted(
                        nonNull(chatPostMessageResponse.getError()) ? chatPostMessageResponse.getError() :
                                chatPostMessageResponse.getWarning())
                );
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send slack notification %s".formatted(billingProfileVerificationUpdated.toString()), e);
        }
    }

}
