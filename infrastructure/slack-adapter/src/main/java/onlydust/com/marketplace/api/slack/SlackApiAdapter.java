package onlydust.com.marketplace.api.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.domain.model.notification.BillingProfileUpdated;
import onlydust.com.marketplace.api.domain.port.output.NotificationPort;

import static java.util.Objects.nonNull;

@Slf4j
public class SlackApiAdapter implements NotificationPort {

    private final SlackProperties slackProperties;
    private final MethodsClient slackClient;
    private static final String OD_LOGO_URL = "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp";

    public SlackApiAdapter(final SlackProperties slackProperties) {
        this.slackProperties = slackProperties;
        this.slackClient = Slack.getInstance().methods(slackProperties.getToken());
    }

    @Override
    public void notifyNewVerificationEvent(BillingProfileUpdated billingProfileUpdated) {
        try {
            final ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(req -> req
                    .channel(slackProperties.getKycKybChannel())
                    .iconUrl(OD_LOGO_URL)
                    .username("[%s] - OnlyDust API".formatted(slackProperties.getEnvironment().toUpperCase()))
                    .text("New KYC/KYB event")
                    .blocksAsString(UserVerificationEventMapper.billingProfileUpdatedToSlackNotification(billingProfileUpdated, slackProperties.getTagAllChannel()))
            );
            if (nonNull(chatPostMessageResponse.getError()) || nonNull(chatPostMessageResponse.getWarning())) {
                LOGGER.warn("Error or warning when sending notification to slack : %s".formatted(
                        nonNull(chatPostMessageResponse.getError()) ? chatPostMessageResponse.getError() :
                                chatPostMessageResponse.getWarning())
                );
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send slack notification %s".formatted(billingProfileUpdated.toString()), e);
        }
    }

}
