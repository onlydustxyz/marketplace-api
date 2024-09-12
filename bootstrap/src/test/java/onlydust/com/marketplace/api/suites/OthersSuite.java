package onlydust.com.marketplace.api.suites;

import onlydust.com.marketplace.api.suites.tags.*;
import org.junit.platform.suite.api.ExcludeTags;

@ITSuite
@ExcludeTags({
        TagAccounting.VALUE,
        TagBO.VALUE,
        TagMe.VALUE,
        TagProject.VALUE,
        TagReward.VALUE,
        TagUser.VALUE,
        TagConcurrency.VALUE,
        TagLLM.VALUE,
        TagBI.VALUE
})
public class OthersSuite {
}
