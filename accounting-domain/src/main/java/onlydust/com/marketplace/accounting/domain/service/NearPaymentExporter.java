package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import onlydust.com.marketplace.kernel.model.RewardId;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class NearPaymentExporter implements PaymentExporter {
    @Override
    public String csv(List<PayableReward> payableRewards, Map<RewardId, Wallet> wallets, Map<RewardId, Invoice> rewardInvoices) {
        final var rewards = payableRewards.stream()
                .filter(r -> r.currency().code().equals(Currency.Code.NEAR))
                .toList();
        
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : rewards) {
                csvPrinter.printRecord(
                        wallets.get(reward.id()).address(),
                        rewardInvoices.get(reward.id()).applyTaxes(reward.amount().getValue())
                );
            }
        } catch (final Exception e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        return sw.toString();
    }
}
