package onlydust.com.marketplace.api.bootstrap.suites;

import onlydust.com.marketplace.api.bootstrap.suites.tags.TagConcurrency;
import org.junit.platform.suite.api.IncludeTags;

@ITSuite
@IncludeTags(TagConcurrency.VALUE)
public class ConcurrencySuite {
}
