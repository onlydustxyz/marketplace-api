package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.api.postgres.adapter.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.PaymentRequestEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.PaymentRequestRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresSponsorAccountStorageAdapterIT extends AbstractPostgresIT {
    @Autowired
    private PostgresSponsorAccountStorageAdapter adapter;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private PaymentRequestRepository paymentRequestRepository;
    @Autowired
    private ProjectIdRepository projectIdRepository;
    @Autowired
    private PostgresSponsorLedgerProviderAdapter sponsorLedgerProvider;
    @Autowired
    private PostgresRewardLedgerProviderAdapter rewardLedgerProvider;
    @Autowired
    private PostgresProjectLedgerProviderAdapter projectLedgerProvider;

    static final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
    final RewardId rewardId = RewardId.random();

    @BeforeEach
    void setUp() {
        if (!postgresCurrencyAdapter.exists(currency.code()))
            postgresCurrencyAdapter.save(currency);
    }

    @Test
    void should_return_empty_when_ledger_not_found() {
        assertThat(adapter.get(SponsorAccount.Id.random())).isEmpty();
    }

    @Test
    void should_return_sponsor_ledger_when_found() {
        // Given
        final var sponsorId = SponsorId.random();
        final SponsorEntity sponsor = new SponsorEntity(sponsorId.value(), "sponsor", "", "");
        sponsorRepository.save(sponsor);
        final var ledger = new SponsorAccount(SponsorId.of(sponsor.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(sponsorId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = sponsorLedgerProvider.get(sponsorId, currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(sponsorId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }


    @Test
    void should_return_project_ledger_when_found() {
        // Given
        final var projectId = ProjectId.random();
        final ProjectIdEntity project = new ProjectIdEntity(projectId.value());
        projectIdRepository.save(project);
        final var ledger = new SponsorAccount(ProjectId.of(project.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(projectId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = projectLedgerProvider.get(projectId, currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(projectId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }


    @Test
    void should_return_reward_ledger_when_found() {
        // Given
        paymentRequestRepository.save(PaymentRequestEntity.builder()
                .id(rewardId.value())
                .requestorId(UUID.randomUUID())
                .recipientId(0L)
                .requestedAt(new Date())
                .amount(BigDecimal.TEN)
                .hoursWorked(0)
                .projectId(UUID.randomUUID())
                .currency(CurrencyEnumEntity.eth).build());
        final var ledger = new SponsorAccount(rewardId, currency);

        // When
        adapter.save(ledger);

        // Then
        {
            final var savedLedger = adapter.get(ledger.id());
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(rewardId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }

        {
            final var savedLedger = rewardLedgerProvider.get(rewardId, currency);
            assertThat(savedLedger).isPresent();
            assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
            assertThat(savedLedger.get().ownerId()).isEqualTo(rewardId);
            assertThat(savedLedger.get().currency()).isEqualTo(currency);
        }
    }
}