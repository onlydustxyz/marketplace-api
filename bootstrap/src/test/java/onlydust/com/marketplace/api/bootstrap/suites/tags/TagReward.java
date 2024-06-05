package onlydust.com.marketplace.api.bootstrap.suites.tags;

import org.junit.jupiter.api.Tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Tag(TagReward.VALUE)
public @interface TagReward {
    public static final String VALUE = "reward";
}
