package onlydust.com.marketplace.api.domain.model.bank;

import lombok.AllArgsConstructor;

@AllArgsConstructor(staticName = "valueOf")
public class IBAN {
    private final String value;

    public String toPlainString() {
        return value;
    }
}
