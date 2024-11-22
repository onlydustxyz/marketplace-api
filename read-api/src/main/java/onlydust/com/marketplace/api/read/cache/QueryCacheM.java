package onlydust.com.marketplace.api.read.cache;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.QueryHints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.hibernate.annotations.QueryHints.CACHEABLE;
import static org.hibernate.annotations.QueryHints.CACHE_REGION;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@QueryHints({@QueryHint(name = CACHEABLE, value = "true"), @QueryHint(name = CACHE_REGION, value = "queryM")})
public @interface QueryCacheM {
}
