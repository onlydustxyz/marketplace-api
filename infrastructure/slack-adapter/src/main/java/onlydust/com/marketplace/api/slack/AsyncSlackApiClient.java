package onlydust.com.marketplace.api.slack;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import static java.util.Objects.nonNull;

@Slf4j
@AllArgsConstructor
public class AsyncSlackApiClient implements SlackApiClient {
    private static final String OD_LOGO_URL = "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp";

    private final MethodsClient slackClient;
    private final SlackProperties slackProperties;

    @Async
    @Override
    public void sendNotification(final String slackChannel, final String slackDefaultMessage, final String slackBlock) {
        try {
            final ChatPostMessageResponse chatPostMessageResponse = slackClient.chatPostMessage(req -> req
                    .channel(slackChannel)
                    .iconUrl(OD_LOGO_URL)
                    .username("[%s] - OnlyDust API".formatted(slackProperties.getEnvironment().toUpperCase()))
                    .text(slackDefaultMessage)
                    .blocksAsString(slackBlock)
            );
            if (nonNull(chatPostMessageResponse.getError()) || nonNull(chatPostMessageResponse.getWarning())) {
                LOGGER.warn("Error or warning when sending notification to slack : %s".formatted(
                        nonNull(chatPostMessageResponse.getError()) ? chatPostMessageResponse.getError() :
                                chatPostMessageResponse.getWarning())
                );
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to send slack notification %s".formatted(slackDefaultMessage), e);
        }
    }
}
