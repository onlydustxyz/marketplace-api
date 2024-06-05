package onlydust.com.marketplace.api.bootstrap.suites;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Suite
@SelectPackages("onlydust.com.marketplace")
@IncludeClassNamePatterns(".*IT")
public @interface ITSuite {
}
