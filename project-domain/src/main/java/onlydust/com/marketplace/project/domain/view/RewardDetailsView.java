package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import onlydust.com.marketplace.kernel.model.RewardStatus;
import onlydust.com.marketplace.project.domain.model.Project;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
public class RewardDetailsView {
    UUID id;
    Money amount;
    RewardStatus status;
    Date unlockDate;
    ContributorLinkView from;
    ContributorLinkView to;
    Date createdAt;
    Date processedAt;
    ReceiptView receipt;
    Project project;
    UUID invoiceId;
    UUID billingProfileId;
}
