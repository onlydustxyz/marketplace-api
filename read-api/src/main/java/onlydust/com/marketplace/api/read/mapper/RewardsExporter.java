package onlydust.com.marketplace.api.read.mapper;

import onlydust.com.marketplace.api.read.entities.reward.FullRewardStatusReadEntity;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.StringWriter;
import java.util.List;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class RewardsExporter {

    private static final String[] HEADERS = new String[]{
            "Project",
            "Invoice Creator Github",
            "Invoice Creator email",
            "Invoice Creator name",
            "Recipient name",
            "Recipient login",
            "Amount",
            "Currency",
            "Contributions",
            "Status",
            "Requested at",
            "Processed at",
            "Transaction Hash",
            "Payout information",
            "Pretty ID",
            "Sponsors",
            "Programs",
            "Verification status",
            "Account type",
            "Invoice number",
            "Invoice id",
            "Budget",
            "Conversion rate",
            "Dollar Amount"
    };

    public static String csv(List<FullRewardStatusReadEntity> rewards) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : rewards) {
                csvPrinter.printRecord(
                        reward.project().name(),
                        isNull(reward.invoice()) ? null : reward.invoice().createdByUser().login(),
                        isNull(reward.invoice()) ? null : reward.invoice().createdByUser().email(),
                        isNull(reward.invoice()) || isNull(reward.invoice().createdByUser().kyc()) ? null :
                                reward.invoice().createdByUser().kyc().firstName() + " " + reward.invoice().createdByUser().kyc().lastName(),
                        isNull(reward.billingProfile()) ? reward.recipient().login() : reward.billingProfile().subject(),
                        reward.recipient().login(),
                        reward.amount(),
                        reward.currency().code(),
                        reward.itemGithubUrls().stream().sorted().toList(),
                        reward.status(),
                        reward.requestedAt(),
                        reward.paidAt(),
                        reward.transactionReferences(),
                        reward.paidToAccountNumbers(),
                        reward.prettyId(),
                        reward.sponsorNames().stream().sorted().toList(),
                        reward.programNames().stream().sorted().toList(),
                        isNull(reward.billingProfile()) ? null : reward.billingProfile().verificationStatus(),
                        isNull(reward.billingProfile()) ? null : reward.billingProfile().type(),
                        isNull(reward.invoice()) ? null : reward.invoice().number(),
                        isNull(reward.invoice()) ? null : reward.invoice().id(),
                        "%s - %s".formatted(reward.project().name(), reward.currency().code()),
                        reward.usdConversionRate(),
                        reward.amountUsdEquivalent()
                );
            }
        } catch (final Exception e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        return sw.toString();
    }
}
