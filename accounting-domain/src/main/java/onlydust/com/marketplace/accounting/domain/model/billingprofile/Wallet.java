package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Network;

public record Wallet(@NonNull Network network, @NonNull String address) {
}
