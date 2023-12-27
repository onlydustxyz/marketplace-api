package onlydust.com.marketplace.api.domain.model.blockchain.evm.ethereum;

import lombok.EqualsAndHashCode;

import java.util.regex.Pattern;

import static onlydust.com.marketplace.api.domain.exception.OnlyDustException.badRequest;

@EqualsAndHashCode
public class Name {
    private static final Pattern ENS_PATTERN = Pattern.compile("^.+\\.eth$");
    private final String ens;

    public Name(final String ens) {
        if (!ENS_PATTERN.matcher(ens).matches())
            throw badRequest("Provided ENS is not valid");

        this.ens = ens;
    }

    public String asString() {
        return ens;
    }
}
