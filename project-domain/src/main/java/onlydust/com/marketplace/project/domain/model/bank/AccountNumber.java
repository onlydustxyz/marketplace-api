package onlydust.com.marketplace.project.domain.model.bank;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class AccountNumber {
    private final String value;

    public String asString() {
        return value;
    }
}
