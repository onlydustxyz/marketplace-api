package onlydust.com.marketplace.api.postgres.adapter.it;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.api.postgres.adapter.PostgresCurrencyAdapter;
import onlydust.com.marketplace.api.postgres.adapter.PostgresLedgerStorageAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.ProjectIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.SponsorEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.ProjectIdRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.SponsorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresLedgerStorageAdapterIT extends AbstractPostgresIT {
    @Autowired
    private PostgresLedgerStorageAdapter adapter;
    @Autowired
    private PostgresCurrencyAdapter postgresCurrencyAdapter;
    @Autowired
    private SponsorRepository sponsorRepository;
    @Autowired
    private ProjectIdRepository projectIdRepository;

    final Currency currency = Currency.crypto("Ether", Currency.Code.of("ETH"), 18);
    final SponsorEntity sponsor = new SponsorEntity(SponsorId.random().value(), "sponsor", "", "");
    final ProjectIdEntity project = new ProjectIdEntity(ProjectId.random().value());
    final Faker faker = new Faker();
    final ContributorId contributorId = ContributorId.of(faker.number().randomNumber());

    @BeforeEach
    void setUp() {
        postgresCurrencyAdapter.save(currency);
        sponsorRepository.save(sponsor);
        projectIdRepository.save(project);
    }

    @Test
    void should_return_empty_when_ledger_not_found() {
        assertThat(adapter.get(Ledger.Id.random())).isEmpty();
    }

    @Test
    void should_return_sponsor_ledger_when_found() {
        // Given
        final var ledger = new Ledger(SponsorId.of(sponsor.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        final var savedLedger = adapter.get(ledger.id());
        assertThat(savedLedger).isPresent();
        assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
        assertThat(savedLedger.get().ownerId()).isEqualTo(sponsor.getId());
        assertThat(savedLedger.get().currency()).isEqualTo(currency);
    }


    @Test
    void should_return_project_ledger_when_found() {
        // Given
        final var ledger = new Ledger(ProjectId.of(project.getId()), currency);

        // When
        adapter.save(ledger);

        // Then
        final var savedLedger = adapter.get(ledger.id());
        assertThat(savedLedger).isPresent();
        assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
        assertThat(savedLedger.get().ownerId()).isEqualTo(project.getId());
        assertThat(savedLedger.get().currency()).isEqualTo(currency);
    }


    @Test
    void should_return_contributor_ledger_when_found() {
        // Given
        final var ledger = new Ledger(contributorId, currency);

        // When
        adapter.save(ledger);

        // Then
        final var savedLedger = adapter.get(ledger.id());
        assertThat(savedLedger).isPresent();
        assertThat(savedLedger.get().id()).isEqualTo(ledger.id());
        assertThat(savedLedger.get().ownerId()).isEqualTo(contributorId);
        assertThat(savedLedger.get().currency()).isEqualTo(currency);
    }
}