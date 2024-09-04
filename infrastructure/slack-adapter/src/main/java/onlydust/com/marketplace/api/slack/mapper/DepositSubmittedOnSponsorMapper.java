package onlydust.com.marketplace.api.slack.mapper;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import static onlydust.com.marketplace.api.slack.mapper.FrontUrlHelper.getBackOfficeFrontendUrlFromEnvironment;

public interface DepositSubmittedOnSponsorMapper {
    String BLOCK = """
            [
            		{
            			"type": "section",
            			"text": {
            				"type": "mrkdwn",
            				"text": "*Sponsor*: <%s|%s>\\n*User*: <%s|%s>\\n*Amount*: %s %s\\n*Transaction ref*: %s\\n*Date*: %s\\n*<%s|Verify transaction in BO>*"
            			}
            		}
            	]
            """;

    static String mapToSlackBlock(final AuthenticatedUser user, final Sponsor sponsor, final Deposit deposit, final String environment) {
        return BLOCK.formatted(
                getBackOfficeFrontendUrlFromEnvironment(environment) + "sponsors/" + sponsor.id(), sponsor.name(),
                getBackOfficeFrontendUrlFromEnvironment(environment) + "users/" + user.id(), user.login(),
                deposit.transaction().amount(), deposit.currency().code(),
                deposit.transaction().blockchain().getBlockExplorerUrl(deposit.transaction().reference()),
                deposit.transaction().timestamp(),
                getBackOfficeFrontendUrlFromEnvironment(environment) + "sponsors/transactions/"+ deposit.transaction().reference()
        );
    }
}
