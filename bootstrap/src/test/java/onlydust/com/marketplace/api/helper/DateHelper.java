package onlydust.com.marketplace.api.helper;

import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Supplier;

public interface DateHelper {
    static <R> R at(ZonedDateTime date, Supplier<R> callback) {
        try {
            onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider.set(() -> Date.from(date.toInstant()));
            CurrentDateProvider.set(() -> date);
            return callback.get();
        } finally {
            onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider.reset();
            CurrentDateProvider.reset();
        }
    }

    static <R> R at(String date, Supplier<R> callback) {
        return at(ZonedDateTime.parse(date), callback);
    }

    static void at(ZonedDateTime date, Runnable callback) {
        try {
            onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider.set(() -> Date.from(date.toInstant()));
            CurrentDateProvider.set(() -> date);
            callback.run();
        } finally {
            onlydust.com.marketplace.project.domain.gateway.CurrentDateProvider.reset();
            CurrentDateProvider.reset();
        }
    }

    static void at(String date, Runnable callback) {
        at(ZonedDateTime.parse(date), callback);
    }
}
