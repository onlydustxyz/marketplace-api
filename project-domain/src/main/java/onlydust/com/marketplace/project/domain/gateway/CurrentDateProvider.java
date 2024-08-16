package onlydust.com.marketplace.project.domain.gateway;


import java.util.Date;

public class CurrentDateProvider {
    static DateProvider instance = Date::new;

    public static Date now() {
        return instance.now();
    }

    public static void set(DateProvider dateProvider) {
        instance = dateProvider;
    }

    public static void reset() {
        instance = Date::new;
    }
}
