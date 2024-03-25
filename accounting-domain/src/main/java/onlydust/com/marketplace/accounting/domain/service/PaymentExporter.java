package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.model.PayableReward;
import onlydust.com.marketplace.accounting.domain.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Wallet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class PaymentExporter {

    public static String csv(List<PayableReward> payableRewards, Map<RewardId, Wallet> wallets) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : payableRewards) {
                csvPrinter.printRecord(
                        reward.currency().standard().map(s -> s.name().toLowerCase()).orElse("native"),
                        reward.currency().address().map(Object::toString).orElse(""),
                        wallets.get(reward.id()).address(),
                        reward.amount()
                );
            }
        } catch (final Exception e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        return sw.toString();
    }
}
