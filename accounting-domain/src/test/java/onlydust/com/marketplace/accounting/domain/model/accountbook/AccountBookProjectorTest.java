package onlydust.com.marketplace.accounting.domain.model.accountbook;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.out.SponsorAccountStorage;
import onlydust.com.marketplace.accounting.domain.stubs.Currencies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AccountBookProjectorTest {
    private final SponsorAccountStorage storage = mock(SponsorAccountStorage.class);
    private final AccountBookProjector observer = new AccountBookProjector(storage);
    private final Faker faker = new Faker();
    public final AccountBook.AccountId sponsorAccountId = AccountBook.AccountId.of(SponsorAccount.Id.random());
    private final PositiveAmount amount = PositiveAmount.of(faker.number().randomNumber(3, true));
    private final ZonedDateTime timestamp = ZonedDateTime.now();

    @BeforeEach
    void setup() {
        reset(storage);
        when(storage.get(sponsorAccountId.sponsorAccountId()))
                .thenReturn(Optional.of(new SponsorAccount(SponsorId.random(), Currencies.USDC)));
    }

    @Test
    void onMint() {
        // When
        observer.onMint(timestamp, sponsorAccountId, amount);

        // Then
        assertSavedTransaction(timestamp, SponsorAccount.AllowanceTransaction.Type.MINT, amount, null);
    }

    @Test
    void onBurn() {
        // When
        observer.onBurn(timestamp, sponsorAccountId, amount);

        // Then
        assertSavedTransaction(timestamp, SponsorAccount.AllowanceTransaction.Type.BURN, amount, null);
    }

    @Test
    void onTransfer() {
        // Given
        final var projectAccountId = AccountBook.AccountId.of(ProjectId.random());

        // When
        observer.onTransfer(timestamp, sponsorAccountId, projectAccountId, amount);

        // Then
        assertSavedTransaction(timestamp, SponsorAccount.AllowanceTransaction.Type.TRANSFER, amount, projectAccountId.projectId());
    }

    @Test
    void onRefund() {
        // Given
        final var projectAccountId = AccountBook.AccountId.of(ProjectId.random());

        // When
        observer.onRefund(timestamp, projectAccountId, sponsorAccountId, amount);

        // Then
        assertSavedTransaction(timestamp, SponsorAccount.AllowanceTransaction.Type.REFUND, amount, projectAccountId.projectId());
    }

    private void assertSavedTransaction(ZonedDateTime timestamp, SponsorAccount.AllowanceTransaction.Type type, Amount amount, ProjectId projectId) {
        final var sponsorAccountCaptor = ArgumentCaptor.forClass(SponsorAccount.class);
        verify(storage).save(sponsorAccountCaptor.capture());
        final var sponsorAccount = sponsorAccountCaptor.getValue();

        assertThat(sponsorAccount.getAllowanceTransactions()).hasSize(1);
        final var transaction = sponsorAccount.getAllowanceTransactions().get(0);
        assertThat(transaction.timestamp()).isEqualTo(timestamp);
        assertThat(transaction.type()).isEqualTo(type);
        assertThat(transaction.amount()).isEqualTo(amount);
        assertThat(transaction.projectId()).isEqualTo(projectId);
    }
}