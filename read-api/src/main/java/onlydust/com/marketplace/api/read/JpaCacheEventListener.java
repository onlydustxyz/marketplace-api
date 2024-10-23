package onlydust.com.marketplace.api.read;


import lombok.extern.slf4j.Slf4j;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JpaCacheEventListener implements CacheEventListener<Object, Object> {

    @Override
    public void onEvent(CacheEvent event) {
        //if (LOGGER.isTraceEnabled()) {
        LOGGER.info(
                "{ type: {}, key: {} }",
                event.getType(),
                event.getKey()
        );
        //}
    }

}
