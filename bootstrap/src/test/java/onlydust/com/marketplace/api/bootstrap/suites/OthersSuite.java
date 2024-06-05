package onlydust.com.marketplace.api.bootstrap.suites;

import onlydust.com.marketplace.api.bootstrap.suites.tags.*;
import org.junit.platform.suite.api.ExcludeTags;

@ITSuite
@ExcludeTags({
        TagAccounting.VALUE,
        TagBO.VALUE,
        TagMe.VALUE,
        TagProject.VALUE,
        TagReward.VALUE,
        TagUser.VALUE,
})
public class OthersSuite {
}
