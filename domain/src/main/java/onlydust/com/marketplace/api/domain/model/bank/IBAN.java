package onlydust.com.marketplace.api.domain.model.bank;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "of")
public class IBAN {
    private final String value;

    public String asString() {
        return value;
    }
}
