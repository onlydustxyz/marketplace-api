package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.view.RewardDetailsView;
import onlydust.com.marketplace.accounting.domain.view.ShortSponsorView;
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
            "Recipient email",
            "Verification status",
            "Account type",
            "Invoice number",
            "Invoice id",
            "Budget",
            "Conversion rate",
            "Dollar Amount"
    };

    public static String csv(List<RewardDetailsView> rewards) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : rewards) {
                csvPrinter.printRecord(
                        reward.project().name(),
                        isNull(reward.invoice()) ? null : reward.invoice().createdBy().githubLogin(),
                        isNull(reward.invoice()) ? null : reward.invoice().createdBy().email(),
                        isNull(reward.invoice()) ? null : reward.invoice().createdBy().name(),
                        isNull(reward.billingProfile()) ? null : reward.billingProfile().subject(),
                        reward.money().amount(),
                        reward.money().currency().code(),
                        reward.githubUrls(),
                        reward.status(),
                        reward.requestedAt(),
                        reward.processedAt(),
                        reward.transactionReferences(),
                        reward.paidToAccountNumbers(),
                        reward.id().pretty(),
                        reward.sponsors().stream().map(ShortSponsorView::name).toList(),
                        isNull(reward.billingProfile()) ? null : reward.billingProfile().status(),
                        isNull(reward.billingProfile()) ? null : reward.billingProfile().type(),
                        isNull(reward.invoice()) ? null : reward.invoice().number(),
                        isNull(reward.invoice()) ? null : reward.invoice().id(),
                        "%s - %s".formatted(reward.project().name(), reward.money().currency().code()),
                        reward.money().usdConversionRate().orElseThrow(() -> internalServerError("Dollars conversion rate not found for reward %s".formatted(reward.id().value()))),
                        reward.money().dollarsEquivalent().orElseThrow(() -> internalServerError("Dollars equivalent not found for reward %s".formatted(reward.id().value())))
                );
            }
        } catch (final Exception e) {
            throw internalServerError("Error while exporting rewards to CSV", e);
        }

        return sw.toString();
    }

    private record InvoiceCreator(String login, String email, String fullName) {
        @Override
        public String toString() {
            return login + "," + email + "," + fullName;
        }
    }
}
