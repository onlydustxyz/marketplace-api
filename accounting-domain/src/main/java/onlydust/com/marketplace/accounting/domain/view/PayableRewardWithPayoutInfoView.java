package onlydust.com.marketplace.accounting.domain.view;

import lombok.Builder;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;

import java.util.UUID;

@Builder
public record PayableRewardWithPayoutInfoView(@NonNull UUID id,
                                              @NonNull MoneyView money,
                                              Wallet wallet) {
}
