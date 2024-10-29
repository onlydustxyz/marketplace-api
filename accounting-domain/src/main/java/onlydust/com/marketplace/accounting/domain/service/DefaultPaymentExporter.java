package onlydust.com.marketplace.accounting.domain.service;

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

public class DefaultPaymentExporter implements PaymentExporter {
    @Override
    public String csv(List<PayableReward> payableRewards, Map<RewardId, Wallet> wallets, Map<RewardId, Invoice> rewardInvoices) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : payableRewards) {
                csvPrinter.printRecord(
                        reward.currency().standard().map(s -> s.name().toLowerCase()).orElse("native"),
                        reward.currency().address().map(Object::toString).orElse(""),
                        wallets.get(reward.id()).address(),
                        rewardInvoices.get(reward.id()).applyTaxes(reward.amount().getValue()),
                        ""
                );
            }
        } catch (final Exception e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        return sw.toString();
    }
}
