package onlydust.com.marketplace.accounting.domain.service;

import onlydust.com.marketplace.accounting.domain.view.BackofficeRewardView;
import onlydust.com.marketplace.accounting.domain.view.ShortSponsorView;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.internalServerError;

public class RewardsExporter {

    private static final String[] HEADERS = new String[]{
            "Project",
            "Recipient",
            "Recipient Github",
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

    public static String csv(List<BackofficeRewardView> rewards) {
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        final StringWriter sw = new StringWriter();
        try (final var csvPrinter = new CSVPrinter(sw, csvFormat)) {
            for (final var reward : rewards) {
                csvPrinter.printRecord(
                        reward.project().name(),
                        //TODO add kyc/kyb name (company name or person name)
                        isNull(reward.billingProfileAdmin()) ? null : reward.billingProfileAdmin().admins().stream()
                                .map(a -> new InvoiceCreator(a.login(), a.email(), a.firstName() + " " + a.lastName())).toList(),
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
                        isNull(reward.billingProfileAdmin()) ? null : reward.billingProfileAdmin().verificationStatus(),
                        isNull(reward.billingProfileAdmin()) ? null : reward.billingProfileAdmin().billingProfileType(),
                        isNull(reward.invoice()) ? null : reward.invoice().number(),
                        isNull(reward.invoice()) ? null : reward.invoice().id(),
                        "%s - %s".formatted(reward.project().name(), reward.money().currency().code()),
                        //TODO: get the real conversion rate instead of computing it
                        isNull(reward.money().dollarsEquivalent()) || BigDecimal.ZERO.compareTo(reward.money().amount()) == 0 ? null :
                                reward.money().dollarsEquivalent().divide(reward.money().amount(), RoundingMode.FLOOR),
                        reward.money().dollarsEquivalent()
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
