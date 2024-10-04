package onlydust.com.marketplace.api.postgres.adapter.it;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("onlydust.com.marketplace")
@IncludeClassNamePatterns(".*IT")
public class PostgresSuite {
}
