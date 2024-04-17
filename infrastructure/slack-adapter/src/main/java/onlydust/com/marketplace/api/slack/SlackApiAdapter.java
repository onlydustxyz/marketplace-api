package onlydust.com.marketplace.api.slack;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.events.BillingProfileVerificationUpdated;
import onlydust.com.marketplace.api.slack.mapper.BillingProfileVerificationEventMapper;
import onlydust.com.marketplace.api.slack.mapper.ProjectCreatedEventMapper;
import onlydust.com.marketplace.api.slack.mapper.UserAppliedOnProjectEventMapper;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.Event;
import onlydust.com.marketplace.kernel.port.output.NotificationPort;
import onlydust.com.marketplace.project.domain.model.User;
import onlydust.com.marketplace.project.domain.model.notification.ProjectCreated;
import onlydust.com.marketplace.project.domain.model.notification.UserAppliedOnProject;
import onlydust.com.marketplace.project.domain.port.output.ProjectStoragePort;
import onlydust.com.marketplace.project.domain.port.output.UserStoragePort;
import onlydust.com.marketplace.project.domain.view.ProjectDetailsView;

import static java.util.Objects.nonNull;

@Slf4j
public class SlackApiAdapter implements NotificationPort {

    private final SlackProperties slackProperties;
    private final MethodsClient slackClient;
    private static final String OD_LOGO_URL = "https://app.onlydust.com/_next/static/media/onlydust-logo.68e14357.webp";
    private final UserStoragePort userStoragePort;
    private final ProjectStoragePort projectStoragePort;

    public SlackApiAdapter(final SlackProperties slackProperties, final UserStoragePort userStoragePort, final ProjectStoragePort projectStoragePort) {
        this.slackProperties = slackProperties;
        this.slackClient = Slack.getInstance().methods(slackProperties.getToken());
        this.userStoragePort = userStoragePort;
        this.projectStoragePort = projectStoragePort;
    }

    @Override
    public void notify(Event event) {
        if (event instanceof BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
            sendBillingProfileVerificationNotification(billingProfileVerificationUpdated);
        } else if (event instanceof UserAppliedOnProject userAppliedOnProject) {
            sendUserAppliedOnProjectNotification(userAppliedOnProject);
        } else if (event instanceof ProjectCreated projectCreated) {
            sendProjectCreatedNotification(projectCreated);
        } else {
            LOGGER.warn("Unmanaged event class %s".formatted(event.getClass()));
        }
    }

    private void sendProjectCreatedNotification(ProjectCreated projectCreated) {
        final User user = userStoragePort.getUserById(projectCreated.getProjectId())
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(projectCreated.getUserId())));
        final ProjectDetailsView projectDetailsView = projectStoragePort.getById(projectCreated.getProjectId(), user);
        sendNotification(slackProperties.getDevRelChannel(), "New project created", ProjectCreatedEventMapper.mapToSlackBlock(user,
                projectDetailsView, slackProperties.getEnvironment()));
    }

    private void sendBillingProfileVerificationNotification(final BillingProfileVerificationUpdated billingProfileVerificationUpdated) {
        final User user = userStoragePort.getUserById(billingProfileVerificationUpdated.getUserId().value())
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(billingProfileVerificationUpdated.getUserId().value())));
        sendNotification(slackProperties.getKycKybChannel(), "New KYC/KYB event",
                BillingProfileVerificationEventMapper.mapToSlackBlock(billingProfileVerificationUpdated, user,
                        slackProperties.getTagAllChannel()));
    }

    private void sendUserAppliedOnProjectNotification(final UserAppliedOnProject userAppliedOnProject) {
        final User user = userStoragePort.getUserById(userAppliedOnProject.getUserId())
                .orElseThrow(() -> OnlyDustException.notFound("User not found %s".formatted(userAppliedOnProject.getUserId())));
        final ProjectDetailsView projectDetailsView = projectStoragePort.getById(userAppliedOnProject.getProjectId(), user);
        sendNotification(slackProperties.getDevRelChannel(), "New user application on project", UserAppliedOnProjectEventMapper.mapToSlackBlock(user,
                projectDetailsView, slackProperties.getEnvironment()));
    }

    private void sendNotification(final String slackChannel, final String slackDefaultMessage, final String slackBlock) {
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
