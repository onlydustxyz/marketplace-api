package onlydust.com.marketplace.api.suites;

import onlydust.com.marketplace.api.suites.tags.TagConcurrency;
import org.junit.platform.suite.api.IncludeTags;

@ITSuite
@IncludeTags(TagConcurrency.VALUE)
public class ConcurrencySuite {
}
