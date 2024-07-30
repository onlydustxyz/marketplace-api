package onlydust.com.marketplace.api.slack;

public interface SlackApiClient {
    void sendNotification(final String slackChannel, final String slackDefaultMessage, final String slackBlock);
}
