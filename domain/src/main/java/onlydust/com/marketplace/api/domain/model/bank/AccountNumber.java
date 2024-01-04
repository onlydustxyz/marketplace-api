package onlydust.com.marketplace.api.domain.model.bank;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class AccountNumber {
    private final String value;

    public String asString() {
        return value;
    }
}
