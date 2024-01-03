package onlydust.com.marketplace.api.domain.model.blockchain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.regex.Pattern;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

@EqualsAndHashCode
public abstract class Hash {
    final @NonNull String inner;

    protected Hash(final int maxByteCount, final @NonNull String hash) {
        final var validator = Validator.of(maxByteCount);
        validator.check(hash);

        this.inner = validator.sanitize(hash);
    }

    public String asString() {
        return inner;
    }

    @AllArgsConstructor(staticName = "of")
    @EqualsAndHashCode
    private static class Validator {
        private static final Pattern HEX_PATTERN = Pattern.compile("^0[xX][a-fA-F0-9]+$");
        private final int maxByteCount;

        public void check(final @NonNull String hash) {
            if (!HEX_PATTERN.matcher(hash).matches()) throw badRequest("Provided hash is not hexadecimal");
            if (hash.length() < 3) throw badRequest("Provided hash is too short");
            if (hash.length() > maxByteCount * 2 + 2)
                throw badRequest("Provided hash should be less than %d bytes".formatted(maxByteCount));
        }

        public String sanitize(String hash) {
            return "0x" + (hash.length() % 2 == 0 ? "" : "0") + hash.substring(2);
        }
    }
}
