package onlydust.com.marketplace.project.domain.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Builder(toBuilder = true)
@Data
public class OldPayRewardRequestCommand {
    @NonNull
    UUID rewardId;
    @NonNull
    String recipientAccount;
    @NonNull
    String transactionReference;
    @NonNull
    Currency currency;
}
