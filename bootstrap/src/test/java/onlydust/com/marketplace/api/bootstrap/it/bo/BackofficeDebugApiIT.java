package onlydust.com.marketplace.api.bootstrap.it.bo;

import onlydust.com.marketplace.accounting.domain.model.Payment;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.api.bootstrap.helper.UserAuthHelper;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.AccountBookEventEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.AccountBookEventRepository;
import onlydust.com.marketplace.user.domain.model.BackofficeUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class BackofficeDebugApiIT extends AbstractMarketplaceBackOfficeApiIT {
    private static final String CHECK_ACCOUNTING_EVENTS = "/bo/v1/debug/accounting/check-events";

    @Autowired
    private AccountBookEventRepository accountBookEventRepository;

    UserAuthHelper.AuthenticatedBackofficeUser olivier;

    @BeforeEach
    void setUp() {
        olivier = userAuthHelper.authenticateBackofficeUser("olivier.fuxet@gmail.com", List.of(BackofficeUser.Role.BO_ADMIN));
    }

    @Test
    public void should_check_accounting_events() {
        client
                .get()
                .uri(getApiURI(CHECK_ACCOUNTING_EVENTS))
                .header("Authorization", "Bearer " + olivier.jwt())
                .exchange()
                .expectStatus()
                .isNoContent();

        final var event = accountBookEventRepository.findAllByAccountBookIdOrderByIdAsc(UUID.fromString("c8f1d94a-e9d4-40d6-9b93-6818b9a7730c")).stream()
                .filter(e -> e.id().equals(168L)).findFirst().orElseThrow();

        final var invalidEvent = new AccountBookEventEntity(event.id(), event.accountBookId(), event.timestamp(),
                new AccountBookEventEntity.Payload(new AccountBookAggregate.BurnEvent(
                        AccountBook.AccountId.of(Payment.Id.of("b558e96f-10ac-483a-aca6-6e3de0895e77")), PositiveAmount.of(1_000_000_000L))));

        accountBookEventRepository.saveAndFlush(invalidEvent);

        client
                .get()
                .uri(getApiURI(CHECK_ACCOUNTING_EVENTS))
                .header("Authorization", "Bearer " + olivier.jwt())
                .exchange()
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .json("""
                        {"message":"Cannot burn 1000000000 from b558e96f-10ac-483a-aca6-6e3de0895e77"}
                        """);
    }
}
