package onlydust.com.marketplace.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.SponsorId;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.service.CachedAccountBookProvider;
import org.apache.commons.cli.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;

import java.util.Optional;

import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookState.ToDot;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class AccountBookDisplay implements CommandLineRunner {
    private final CurrencyStorage currencyStorage;
    private final SponsorAccountStorage sponsorAccountStorage;
    private final CachedAccountBookProvider accountBookProvider;

    @Override
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("account_book_display")) return;

        final var options = cliArguments();

        parseArgs(args, options).ifPresent(cmd -> run(Currency.Code.of(cmd.getOptionValue("c")),
                cmd.hasOption("s") ? SponsorId.of(cmd.getOptionValue("s")) : null));
    }

    private void run(Currency.Code currencyCode, SponsorId sponsorId) {
        final var currency = currencyStorage.findByCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Currency %s not found".formatted(currencyCode)));
        final var sponsorAccount = sponsorId == null ? null :
                sponsorAccountStorage.getSponsorAccounts(sponsorId).stream().filter(s -> s.currency().equals(currency)).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Sponsor %s has no account for currency %s".formatted(sponsorId, currencyCode)));

        final var accountBook = accountBookProvider.get(currency);
        accountBook.state().export(ToDot("account_book.dot", sponsorAccount == null ? null : sponsorAccount.id()));
    }

    private static Optional<CommandLine> parseArgs(String[] args, Options options) {
        CommandLine commandLine;
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            LOGGER.error(e.getMessage());
            printHelp(options);
            return Optional.empty();
        }

        if (commandLine.hasOption("h")) {
            printHelp(options);
            return Optional.empty();
        }

        return Optional.of(commandLine);
    }

    private static void printHelp(Options options) {
        new HelpFormatter().printHelp("Usage:", options);
    }

    private Options cliArguments() {
        final var options = new Options();

        options.addOption(Option.builder("c")
                .longOpt("currency")
                .desc("Currency code")
                .hasArg()
                .required()
                .build());

        options.addOption(Option.builder("s")
                .longOpt("sponsor")
                .desc("Sponsor ID")
                .hasArg()
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("print Help")
                .build());

        return options;
    }
}
