package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.project.domain.view.HackathonDetailsView;
import onlydust.com.marketplace.project.domain.view.UserProfileView;

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

    static String mapToSlackBlock(UserProfileView userProfileView, HackathonDetailsView hackathonDetailsView, String environment) {
        return BLOCK.formatted(
                userProfileView.getLogin(), FrontUrlHelper.getBackOfficeFrontendUrlFromEnvironment(environment) + "user/%s".formatted(userProfileView.getId()),
                hackathonDetailsView.title(),
                FrontUrlHelper.getMarketplaceFrontendUrlFromEnvironment(environment) + "h/%s".formatted(hackathonDetailsView.slug()),
                userProfileView.getTelegram()
        );
    }
}
