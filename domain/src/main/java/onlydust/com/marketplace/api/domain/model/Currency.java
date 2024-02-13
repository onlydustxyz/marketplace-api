package onlydust.com.marketplace.api.domain.model;

import onlydust.com.marketplace.kernel.exception.OnlyDustException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public enum Currency {
    USD, ETH, OP, APT, STRK, LORDS, USDC;

    public Date unlockDate() {
        return this == Currency.OP ? parseDate("2024-08-23") : null;
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            throw OnlyDustException.badRequest("Invalid date format", e);
        }
    }
}
