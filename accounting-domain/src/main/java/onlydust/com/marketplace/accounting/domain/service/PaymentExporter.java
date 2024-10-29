package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.kernel.model.RewardId;

import java.util.List;
import java.util.Map;

public interface PaymentExporter {
    String csv(List<PayableReward> payableRewards, Map<RewardId, Wallet> wallets, Map<RewardId, Invoice> rewardInvoices);
}
