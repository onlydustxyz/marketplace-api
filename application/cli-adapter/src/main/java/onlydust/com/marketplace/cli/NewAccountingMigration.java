package onlydust.com.marketplace.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.CurrencyStorage;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.OldEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.BillingProfileRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.UserViewRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.EventRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class NewAccountingMigration implements CommandLineRunner {
    public static final String UNKNOWN = "UNKNOWN";
    private final @NonNull EventRepository eventRepository;
    private final @NonNull CurrencyStorage currencyStorage;
    private final @NonNull AccountingFacadePort accountingFacadePort;
    private final @NonNull SponsorRepository sponsorRepository;
    private final @NonNull UserViewRepository userViewRepository;
    private final @NonNull BillingProfileRepository billingProfileRepository;

    private final Map<UUID, Budget> budgets = new HashMap<>();
    private final Map<RewardId, Reward> rewards = new HashMap<>();
    private final Map<SponsorId, Map<Currency, List<Budget>>> sponsorBudgets = new HashMap<>();

    @Override
    //    @Transactional
    public void run(String... args) {
        final var stopWatch = new StopWatch("accounting-migration");

        try {
            stopWatch.start("Reading old events");
            readOldEvents();
            stopWatch.stop();

            stopWatch.start("Aggregating budgets");
            aggregateBudgets();
            stopWatch.stop();

            stopWatch.start("Creating sponsor accounts");
            sponsorBudgets.forEach((sponsorId, value) -> value.forEach((currency, budgets) -> {
                final var sponsorAccountStatement = createSponsorAccount(sponsorId, currency, budgets);
                allocateBudgetToProjects(sponsorAccountStatement.account(), currency, budgets);
            }));
            stopWatch.stop();

            stopWatch.start("Processing rewards");
            rewards.values().forEach(this::processReward);
            stopWatch.stop();
        } finally {
            LOGGER.info(stopWatch.prettyPrint());
        }
    }

    private void processReward(Reward reward) {
        if (reward.cancelled)
            return;

        LOGGER.info("Processing reward %s of %s %s for %s".formatted(reward.id, reward.amount, reward.currency.code(), reward.recipientId));
        accountingFacadePort.createReward(reward.projectId, reward.id, reward.amount, reward.currency.id());

        if (reward.processed) {
            LOGGER.info("Paying reward %s".formatted(reward.id));
            final var reference = new SponsorAccount.PaymentReference(reward.network, reward.reference, contributorName(reward.recipientId),
                    reward.accountNumber);
            accountingFacadePort.pay(reward.id, reward.currency.id(), reference);
        }
    }

    private String contributorName(@NonNull Long recipientId) {
        final var recipient = userViewRepository.findByGithubUserId(recipientId);

        if (recipient.isPresent()) {
            final var billingProfile = billingProfileRepository.findBillingProfilesForUserId(recipient.get().getId()).stream().findFirst();

            if (billingProfile.isPresent()) {
                if (billingProfile.get().getKyb() != null && billingProfile.get().getKyb().name() != null)
                    return billingProfile.get().getKyb().name();

                if (billingProfile.get().getKyc() != null && billingProfile.get().getKyc().getFirstName() != null && billingProfile.get().getKyc().getLastName() != null)
                    return billingProfile.get().getKyc().getFirstName() + " " + billingProfile.get().getKyc().getLastName();
            }

            return recipient.get().getGithubLogin();
        }

        return recipientId.toString();
    }

    private void aggregateBudgets() {
        budgets.values().forEach(budget -> sponsorBudgets.computeIfAbsent(budget.sponsorId, s -> new HashMap<>()).computeIfAbsent(budget.currency,
                c -> new ArrayList<>()).add(budget));
    }

    private SponsorAccountStatement createSponsorAccount(SponsorId sponsorId, Currency currency, List<Budget> budgets) {
        final var total = budgets.stream().map(b -> b.amount).reduce(PositiveAmount.ZERO, Amount::add);
        final var sponsor = sponsorRepository.findById(sponsorId.value()).orElseThrow(() -> notFound("Sponsor not found: %s".formatted(sponsorId)));

        LOGGER.info("Creating %s account for sponsor %s (budget: %s)".formatted(currency.code(), sponsor.getName(), total));
        final var transaction = new SponsorAccount.Transaction(Network.fromCurrencyCode(currency.code().toString()), UNKNOWN, total, sponsor.getName(),
                UNKNOWN);

        return accountingFacadePort.createSponsorAccount(sponsorId, currency.id(), PositiveAmount.of(total), unlockDateOf(currency), transaction);
    }

    private void allocateBudgetToProjects(SponsorAccount sponsorAccount, Currency currency, List<Budget> budgets) {
        budgets.forEach(budget -> {
            if (budget.amount.isStrictlyPositive()) {
                LOGGER.info("Allocating %s %s from sponsor %s to project %s ".formatted(budget.amount, budget.currency.code(), budget.sponsorId,
                        budget.projectId));
                accountingFacadePort.allocate(sponsorAccount.id(), budget.projectId, PositiveAmount.of(budget.amount), currency.id());
            } else if (budget.amount.isNegative()) {
                LOGGER.info("Unallocating %s %s from project %s to sponsor %s ".formatted(budget.amount.negate(), budget.currency.code(), budget.projectId,
                        budget.sponsorId));
                accountingFacadePort.unallocate(budget.projectId, sponsorAccount.id(), PositiveAmount.of(budget.amount.negate()), currency.id());
            }
        });
    }

    private ZonedDateTime unlockDateOf(Currency currency) {
        return Optional.ofNullable(onlydust.com.marketplace.project.domain.model.Currency.valueOf(currency.code().toString().toUpperCase()).unlockDate()).map(d -> d.toInstant().atZone(ZoneOffset.UTC)).orElse(null);
    }

    private void readOldEvents() {
        LOGGER.info("Reading old events...");
        eventRepository.findAll().forEach(this::read);
    }

    @SneakyThrows
    private void read(OldEventEntity oldEventEntity) {
        final var payload = new ObjectMapper().readTree(oldEventEntity.getPayload()).fields().next();
        switch (oldEventEntity.getAggregateName()) {
            case "PROJECT":
                switch (payload.getKey()) {
                    case "BudgetLinked":
                        readProjectBudgetLinked(payload.getValue());
                        break;
                    default:
                        skip(oldEventEntity.getAggregateName(), payload.getKey());
                        break;
                }
                break;
            case "BUDGET":
                switch (payload.getKey()) {
                    case "Allocated":
                        readBudgetAllocated(payload.getValue());
                        break;
                    default:
                        skip(oldEventEntity.getAggregateName(), payload.getKey());
                        break;
                }
                break;
            case "PAYMENT":
                switch (payload.getKey()) {
                    case "Requested":
                        readPaymentRequested(payload.getValue());
                        break;
                    case "Cancelled":
                        readPaymentCancelled(payload.getValue());
                        break;
                    case "Processed":
                        readPaymentProcessed(payload.getValue());
                        break;
                    default:
                        skip(oldEventEntity.getAggregateName(), payload.getKey());
                        break;
                }
                break;
            default:
                skip(oldEventEntity.getAggregateName(), payload.getKey());
                break;
        }
    }

    private void readPaymentRequested(JsonNode value) {
        LOGGER.info("Reading Payment.Requested event: " + value);
        final var reward = reward(RewardId.of(value.get("id").asText()));
        reward.amount = PositiveAmount.of(new BigDecimal(value.get("amount").get("amount").asText()));
        reward.currency = currencyStorage.findByCode(Currency.Code.of(value.get("amount").get("currency").asText())).orElseThrow(() -> notFound(("Currency not" +
                                                                                                                                                 " found: %s").formatted(value.get("amount").get("currency").asText())));
        reward.projectId = ProjectId.of(value.get("project_id").asText());
        reward.recipientId = value.get("recipient_id").asLong();
        reward.network = Network.fromCurrencyCode(reward.currency.code().toString());
    }

    private void readPaymentCancelled(JsonNode value) {
        LOGGER.info("Reading Payment.Cancelled event: " + value);
        final var reward = reward(RewardId.of(value.get("id").asText()));
        reward.cancelled = true;
    }

    private void readPaymentProcessed(JsonNode value) {
        LOGGER.info("Reading Payment.Processed event: " + value);
        final var reward = reward(RewardId.of(value.get("id").asText()));
        reward.processed = true;
        reward.reference =
                Optional.ofNullable(value.get("receipt").get("Ethereum")).map(r -> r.get("transaction_hash").asText()).or(() -> Optional.ofNullable(value.get("receipt").get("Optimism")).map(r -> r.get("transaction_hash").asText())).or(() -> Optional.ofNullable(value.get("receipt").get("Starknet")).map(r -> r.get("transaction_hash").asText())).or(() -> Optional.ofNullable(value.get("receipt").get("Aptos")).map(r -> r.get("transaction_hash").asText())).orElseGet(() -> value.get("receipt").get("Sepa").get("transaction_reference").asText());
        reward.accountNumber =
                Optional.ofNullable(value.get("receipt").get("Ethereum")).map(r -> r.get("recipient_address").asText()).or(() -> Optional.ofNullable(value.get("receipt").get("Optimism")).map(r -> r.get("recipient_address").asText())).or(() -> Optional.ofNullable(value.get("receipt").get("Starknet")).map(r -> r.get("recipient_address").asText())).or(() -> Optional.ofNullable(value.get("receipt").get("Aptos")).map(r -> r.get("recipient_address").asText())).orElseGet(() -> value.get("receipt").get("Sepa").get("recipient_iban").asText());
    }

    private void skip(String aggregateName, String key) {
        LOGGER.info("Skipping {}.{}", aggregateName, key);
    }

    private void readBudgetAllocated(JsonNode payload) {
        LOGGER.info("Reading Budget.Allocated event: " + payload);
        final var budget = budget(UUID.fromString(payload.get("id").asText()));

        budget.amount = budget.amount.add(Amount.of(payload.get("amount").asLong()));
        budget.sponsorId = SponsorId.of(payload.get("sponsor_id").asText());
    }

    private void readProjectBudgetLinked(JsonNode payload) {
        LOGGER.info("Reading Project.BudgetLinked event: " + payload);
        final var budget = budget(UUID.fromString(payload.get("budget_id").asText()));

        budget.projectId = ProjectId.of(payload.get("id").asText());

        final var currencyCode = payload.get("currency").asText();
        budget.currency =
                currencyStorage.findByCode(Currency.Code.of(currencyCode)).orElseThrow(() -> notFound("Currency not found: %s".formatted(currencyCode)));
    }

    private Budget budget(UUID id) {
        return budgets.computeIfAbsent(id, Budget::new);
    }

    private Reward reward(RewardId id) {
        return rewards.computeIfAbsent(id, Reward::new);
    }


    @ToString
    static class Budget {
        UUID id;
        Currency currency;
        Amount amount = Amount.ZERO;
        ProjectId projectId;
        SponsorId sponsorId;

        public Budget(UUID id) {
            this.id = id;
        }
    }

    @ToString
    static class Reward {
        RewardId id;
        Currency currency;
        PositiveAmount amount;
        ProjectId projectId;
        Long recipientId;
        boolean cancelled = false;
        boolean processed = false;
        Network network;
        String reference;
        String accountNumber;

        public Reward(RewardId id) {
            this.id = id;
        }
    }
}
