package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.project.domain.model.Hackathon;
import onlydust.com.marketplace.project.domain.view.GithubUserWithTelegramView;

public interface UserRegisteredOnHackathonEventMapper {

    String BLOCK = """
            [
                		{
                			"type": "section",
                			"text": {
                				"type": "mrkdwn",
                				"text": ":fire: *<%s|%s>* registered on hackathon *<%s|%s>*"
                			}
                		},
                		{
                			"type": "section",
                			"text": {
                				"type": "mrkdwn",
                				"text": ":iphone: *Contact her/him on telegram:* %s"
                			}
                		}
                	]""";

    static String mapToSlackBlock(final UserId userId, final GithubUserWithTelegramView githubUserWithTelegramView, final Hackathon hackathon,
                                  final String environment) {
        return BLOCK.formatted(
                FrontUrlHelper.getBackOfficeFrontendUrlFromEnvironment(environment) + "users/%s".formatted(userId), githubUserWithTelegramView.githubLogin(),
                FrontUrlHelper.getMarketplaceFrontendUrlFromEnvironment(environment) + "h/%s".formatted(hackathon.slug()),
                hackathon.title(),
                githubUserWithTelegramView.telegram()
        );
    }
}
