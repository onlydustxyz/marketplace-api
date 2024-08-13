package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.port.in.DateProvider;

import java.time.ZonedDateTime;

public class CurrentDateProvider {
    static DateProvider instance = ZonedDateTime::now;

    public static ZonedDateTime now() {
        return instance.now();
    }

    public static void set(DateProvider dateProvider) {
        instance = dateProvider;
    }

    public static void reset() {
        instance = ZonedDateTime::now;
    }
}
