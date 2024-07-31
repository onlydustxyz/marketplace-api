package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.kernel.model.AuthenticatedUser;

public interface ProjectCategorySuggestionEventMapper {

    String BLOCK = """
            [
            	{
            		"type": "section",
            		"text": {
            			"type": "mrkdwn",
            			"text": ":rose: New project category *<%s|%s>* was suggested by *<%s|%s>* :rose:"
            		}
            	}
            ]""";

    static String mapToSlackBlock(AuthenticatedUser user, String categoryName, String environment) {
        return BLOCK.formatted(
                FrontUrlHelper.getBackOfficeFrontendUrlFromEnvironment(environment) + "project-categories", categoryName,
                FrontUrlHelper.getBackOfficeFrontendUrlFromEnvironment(environment) + "users/%s".formatted(user.id()), user.login()
        );
    }
}
