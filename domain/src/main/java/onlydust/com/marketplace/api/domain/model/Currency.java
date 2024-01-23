package onlydust.com.marketplace.api.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum Currency {
    Usd, Eth, Op, Apt, Strk, Lords, Usdc;

    public Date unlockDate() {
        return this == Currency.Op ? parseDate("2024-08-23") : null;
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw OnlyDustException.badRequest("Invalid date format", e);
        }
    }
}
