package onlydust.com.marketplace.accounting.domain;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.model.RewardId;
import onlydust.com.marketplace.accounting.domain.model.SponsorAccount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.FullRefundEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.MintEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.RefundEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookAggregate.TransferEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBookEvent;
import onlydust.com.marketplace.accounting.domain.model.accountbook.IdentifiedAccountBookEvent;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.model.ProjectId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountBookTest {

    public static AccountBookAggregate accountBookFromEvents(final @NonNull List<AccountBookEvent> events) {
        List<IdentifiedAccountBookEvent> identifiedAccountBookEvents = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            identifiedAccountBookEvents.add(new IdentifiedAccountBookEvent<>(i + 1, ZonedDateTime.now().minusMinutes(10).plusSeconds(i), events.get(i)));
        }
        final var accountBookAggregate = AccountBookAggregate.empty();
        accountBookAggregate.receive(identifiedAccountBookEvents);
        return accountBookAggregate;
    }

    public static AccountBookAggregate accountBookFromEvents(final @NonNull AccountBookEvent... events) {
        return accountBookFromEvents(List.of(events));
    }

    @Test
    public void should_mint() {
        // Given
        final var account = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents();

        // When
        accountBook.mint(account, amount);

        // Then no exception is thrown
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(amount);
    }

    @Test
    public void should_burn() {
        // Given
        final var account = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(account, amount)
        );

        // When
        accountBook.burn(account, amount);

        // Then no exception is thrown
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_not_burn_money_we_dont_have() {
        // Given
        final var account = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents();

        // When
        assertThatThrownBy(() -> accountBook.burn(account, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot burn %s from %s".formatted(amount, account));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(account)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_transfer_money_from_an_account_to_another() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount)
        );

        // When
        accountBook.transfer(sender, recipient, amount);

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(amount);
    }

    @Test
    public void should_not_transfer_money_from_an_account_to_itself() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount)
        );

        // When
        assertThatThrownBy(() -> accountBook.transfer(sender, sender, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("An account (%s) cannot transfer money to itself".formatted(sender));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(amount);
    }

    @Test
    public void should_not_transfer_money_we_dont_have() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents();

        // When
        assertThatThrownBy(() -> accountBook.transfer(sender, recipient, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot transfer %s from %s to %s".formatted(amount, sender, recipient));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_get_refund_from_an_account_to_another() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount),
                new TransferEvent(sender, recipient, amount)
        );
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(amount);

        // When
        accountBook.refund(recipient, sender, amount);

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(amount);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_refund_refunded_money() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, amount),
                new TransferEvent(account1, account2, amount),
                new TransferEvent(account2, account3, amount)
        );

        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(amount);

        // When
        accountBook.refund(account3, account2, amount);
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);

        // Then
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(amount);
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.ZERO);

        // When
        accountBook.refund(account2, account1, amount);
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(amount);

        // Then
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_refund_money_from_account_with_partial_spent() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, PositiveAmount.of(100L)),
                new TransferEvent(account1, account2, PositiveAmount.of(100L)),
                new TransferEvent(account2, account3, PositiveAmount.of(60L))
        );

        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(40L));
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.of(60L));

        // When
        accountBook.refund(account2, account1, PositiveAmount.of(10L));

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(30L));

        // When
        accountBook.refund(account2, account1, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.of(40L));
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_not_refund_more_than_received() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, amount),
                new TransferEvent(account1, account2, amount)
        );
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(amount);

        // When
        assertThatThrownBy(() -> accountBook.refund(account2, account1, amount.add(PositiveAmount.of(1L))))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund %s from %s to %s".formatted(amount.add(PositiveAmount.of(1L)), account2, account1));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(amount);
    }

    @Test
    public void should_not_refund_spent_money() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, amount),
                new TransferEvent(account1, account2, amount),
                new TransferEvent(account2, account3, PositiveAmount.of(50L))
        );

        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.of(50L));

        // When
        assertThatThrownBy(() -> accountBook.refund(account2, account1, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund %s from %s to %s".formatted(amount, account2, account1));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.of(50L));
    }

    @Test
    public void should_refund_entirely_from_an_account_to_sender() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount),
                new TransferEvent(sender, recipient, amount)
        );
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(amount);

        // When
        final var transactions = accountBook.refund(recipient);

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(amount);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
        assertThat(transactions).containsExactlyInAnyOrder(new AccountBook.Transaction(AccountBook.Transaction.Type.REFUND, sender, recipient, amount));
    }

    @Test
    public void should_refund_entirely_from_an_account_to_multiple_senders() {
        // Given
        final var sender1 = AccountId.of(SponsorAccount.Id.random());
        final var sender2 = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount1 = PositiveAmount.of(100L);
        final var amount2 = PositiveAmount.of(70L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender1, amount1),
                new MintEvent(sender2, amount2),
                new TransferEvent(sender1, recipient, amount1),
                new TransferEvent(sender2, recipient, amount2)
        );
        assertThat(accountBook.state().balanceOf(sender1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(sender2)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(amount1.add(amount2));

        // When
        final var transactions = accountBook.refund(recipient);

        // Then
        assertThat(accountBook.state().balanceOf(sender1)).isEqualTo(amount1);
        assertThat(accountBook.state().balanceOf(sender2)).isEqualTo(amount2);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
        assertThat(transactions).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.REFUND, sender1, recipient, amount1),
                new AccountBook.Transaction(AccountBook.Transaction.Type.REFUND, sender2, recipient, amount2)
        );
    }

    @Test
    public void should_refund_entirely_from_an_account_with_previous_partial_refund() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount),
                new TransferEvent(sender, recipient, amount)
        );
        accountBook.refund(recipient, sender, PositiveAmount.of(10L));
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.of(90L));

        // When
        final var transactions = accountBook.refund(recipient);

        // Then
        assertThat(accountBook.pendingEvents()).containsExactly(
                new IdentifiedAccountBookEvent<>(3, ZonedDateTime.now(), new RefundEvent(recipient, sender, PositiveAmount.of(10L))),
                new IdentifiedAccountBookEvent<>(4, ZonedDateTime.now(), new FullRefundEvent(recipient))
        );
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(amount);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
        assertThat(transactions).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.REFUND, sender, recipient, PositiveAmount.of(90L))
        );
    }

    @Test
    public void should_not_refund_entirely_from_an_account_with_spent() {
        // Given
        final var sender = AccountId.of(SponsorAccount.Id.random());
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var recipient2 = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(sender, amount),
                new TransferEvent(sender, recipient, amount),
                new TransferEvent(recipient, recipient2, PositiveAmount.of(10L))
        );
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(recipient2)).isEqualTo(PositiveAmount.of(10L));

        // When
        assertThatThrownBy(() -> accountBook.refund(recipient))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot entirely refund %s because it has outgoing transactions".formatted(recipient));
        assertThat(accountBook.pendingEvents()).isEmpty();

        // Then
        assertThat(accountBook.state().balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.state().balanceOf(recipient)).isEqualTo(PositiveAmount.of(90L));
        assertThat(accountBook.state().balanceOf(recipient2)).isEqualTo(PositiveAmount.of(10L));
    }

    @Test
    public void should_receive_money_from_multiple_accounts() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, PositiveAmount.of(100L)),
                new MintEvent(account2, PositiveAmount.of(100L))
        );

        // When
        accountBook.transfer(account1, account3, PositiveAmount.of(50L));
        accountBook.transfer(account2, account3, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(70L));
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.of(80L));

        assertThat(accountBook.pendingEvents()).containsExactly(
                new IdentifiedAccountBookEvent<>(3, ZonedDateTime.now(), new TransferEvent(account1, account3, PositiveAmount.of(50L))),
                new IdentifiedAccountBookEvent<>(4, ZonedDateTime.now(), new TransferEvent(account2, account3, PositiveAmount.of(30L)))
        );
    }

    @Test
    public void should_transfer_to_multiple_accounts() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, PositiveAmount.of(100L))
        );

        // When
        accountBook.transfer(account1, account2, PositiveAmount.of(50L));
        accountBook.transfer(account1, account3, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.state().balanceOf(account1)).isEqualTo(PositiveAmount.of(20L));
        assertThat(accountBook.state().balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.state().balanceOf(account3)).isEqualTo(PositiveAmount.of(30L));

        assertThat(accountBook.pendingEvents()).containsExactly(
                new IdentifiedAccountBookEvent<>(2, ZonedDateTime.now(), new TransferEvent(account1, account2, PositiveAmount.of(50L))),
                new IdentifiedAccountBookEvent<>(3, ZonedDateTime.now(), new TransferEvent(account1, account3, PositiveAmount.of(30L)))
        );
    }

    @Test
    public void should_handle_multiple_transfers_and_refunds_from_multiple_accounts() {
        // Given
        final var sponsor1 = AccountId.of(SponsorAccount.Id.random());
        final var sponsor2 = AccountId.of(SponsorAccount.Id.random());
        final var committee1 = AccountId.of(SponsorAccount.Id.random());
        final var committee2 = AccountId.of(SponsorAccount.Id.random());
        final var project1 = AccountId.of(SponsorAccount.Id.random());
        final var project2 = AccountId.of(SponsorAccount.Id.random());
        final var contributor = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(sponsor1, PositiveAmount.of(100_000L)),
                new MintEvent(sponsor2, PositiveAmount.of(100_000L)),

                new TransferEvent(sponsor1, committee1, PositiveAmount.of(8_000L)),
                new TransferEvent(sponsor1, committee2, PositiveAmount.of(1_000L)),

                new TransferEvent(sponsor2, committee1, PositiveAmount.of(15_000L)),
                new TransferEvent(sponsor2, committee2, PositiveAmount.of(500L)),
                new TransferEvent(sponsor2, committee2, PositiveAmount.of(4_500L)),

                new TransferEvent(committee1, project1, PositiveAmount.of(8_000L)),
                new TransferEvent(committee1, project1, PositiveAmount.of(12_000L)),
                new TransferEvent(committee1, project2, PositiveAmount.of(2_000L)),

                new TransferEvent(committee2, project2, PositiveAmount.of(1_000L)),
                new TransferEvent(committee2, project2, PositiveAmount.of(500L)),
                new TransferEvent(committee2, project2, PositiveAmount.of(500L)),
                new TransferEvent(committee2, project2, PositiveAmount.of(1_000L)),

                new TransferEvent(project2, contributor, PositiveAmount.of(2_000L)),
                new TransferEvent(project2, contributor, PositiveAmount.of(1_000L)),
                new TransferEvent(project2, contributor, PositiveAmount.of(500L)),
                new TransferEvent(project2, contributor, PositiveAmount.of(100L))
        );

        // Then
        assertThat(accountBook.state().balanceOf(sponsor1)).isEqualTo(PositiveAmount.of(91_000L));
        assertThat(accountBook.state().balanceOf(sponsor2)).isEqualTo(PositiveAmount.of(80_000L));
        assertThat(accountBook.state().balanceOf(committee1)).isEqualTo(PositiveAmount.of(1_000L));
        assertThat(accountBook.state().balanceOf(committee2)).isEqualTo(PositiveAmount.of(3_000L));
        assertThat(accountBook.state().balanceOf(project1)).isEqualTo(PositiveAmount.of(20_000L));
        assertThat(accountBook.state().balanceOf(project2)).isEqualTo(PositiveAmount.of(1_400L));
        assertThat(accountBook.state().balanceOf(contributor)).isEqualTo(PositiveAmount.of(3_600L));

        // When
        accountBook.refund(project2, committee2, PositiveAmount.of(700L));

        // Then
        assertThat(accountBook.state().balanceOf(project2)).isEqualTo(PositiveAmount.of(700L));
        assertThat(accountBook.state().balanceOf(committee2)).isEqualTo(PositiveAmount.of(3_700L));
        // Check other accounts are not impacted
        assertThat(accountBook.state().balanceOf(sponsor1)).isEqualTo(PositiveAmount.of(91_000L));
        assertThat(accountBook.state().balanceOf(sponsor2)).isEqualTo(PositiveAmount.of(80_000L));
        assertThat(accountBook.state().balanceOf(committee1)).isEqualTo(PositiveAmount.of(1_000L));
        assertThat(accountBook.state().balanceOf(project1)).isEqualTo(PositiveAmount.of(20_000L));
        assertThat(accountBook.state().balanceOf(contributor)).isEqualTo(PositiveAmount.of(3_600L));

        assertThat(accountBook.pendingEvents()).containsExactly(
                new IdentifiedAccountBookEvent<>(19, ZonedDateTime.now(), new RefundEvent(project2, committee2, PositiveAmount.of(700L)))
        );
    }

    @Test
    public void should_get_account_balance() {
        // Given
        final var recipient = AccountId.of(SponsorAccount.Id.random());
        final var amount = PositiveAmount.of(100L);
        final var accountBook = accountBookFromEvents(
                new MintEvent(recipient, amount)
        );

        // When
        final Amount balance = accountBook.state().balanceOf(recipient);

        // Then
        assertThat(balance).isEqualTo(amount);
        assertThat(accountBook.pendingEvents()).isEmpty();
    }

    @Test
    public void should_get_refundable_balance() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, PositiveAmount.of(100L)),
                new TransferEvent(account1, account2, PositiveAmount.of(50L)),
                new TransferEvent(account1, account3, PositiveAmount.of(30L)),
                new TransferEvent(account2, account3, PositiveAmount.of(5L))
        );

        // When
        final var refundableBalanceFromAccount2ToAccount1 = accountBook.state().refundableBalance(account2, account1);
        final var refundableBalanceFromAccount3ToAccount1 = accountBook.state().refundableBalance(account3, account1);
        final var refundableBalanceFromAccount3ToAccount2 = accountBook.state().refundableBalance(account3, account2);
        final var refundableBalanceFromAccount1ToAccount3 = accountBook.state().refundableBalance(account1, account3);

        // Then
        assertThat(refundableBalanceFromAccount2ToAccount1).isEqualTo(PositiveAmount.of(45L));
        assertThat(refundableBalanceFromAccount3ToAccount1).isEqualTo(PositiveAmount.of(30L));
        assertThat(refundableBalanceFromAccount3ToAccount2).isEqualTo(PositiveAmount.of(5L));
        assertThat(refundableBalanceFromAccount1ToAccount3).isEqualTo(PositiveAmount.ZERO);

        assertThat(accountBook.pendingEvents()).isEmpty();
    }

    @Test
    public void should_get_the_amount_of_money_transferred_from_an_account_to_another() {
        // Given
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account1, PositiveAmount.of(100L)),
                new TransferEvent(account1, account2, PositiveAmount.of(50L)),
                new TransferEvent(account2, account3, PositiveAmount.of(5L)),
                new RefundEvent(account2, account1, PositiveAmount.of(10L))
        );

        // When
        final var transferredAmountFromAccount1ToAccount2 = accountBook.state().transferredAmount(account1, account2);
        final var transferredAmountFromAccount1ToAccount3 = accountBook.state().transferredAmount(account1, account3);
        final var transferredAmountFromAccount2ToAccount3 = accountBook.state().transferredAmount(account2, account3);
        final var transferredAmountFromAccount2ToAccount1 = accountBook.state().transferredAmount(account2, account1);
        final var transferredAmountFromAccount3ToAccount1 = accountBook.state().transferredAmount(account3, account1);

        // Then
        assertThat(transferredAmountFromAccount1ToAccount2).isEqualTo(PositiveAmount.of(40L));
        assertThat(transferredAmountFromAccount1ToAccount3).isEqualTo(PositiveAmount.of(5L));
        assertThat(transferredAmountFromAccount2ToAccount3).isEqualTo(PositiveAmount.of(5L));
        assertThat(transferredAmountFromAccount2ToAccount1).isEqualTo(PositiveAmount.ZERO);
        assertThat(transferredAmountFromAccount3ToAccount1).isEqualTo(PositiveAmount.ZERO);

        assertThat(accountBook.state().hasParent(account1, List.of(account2, account3))).isFalse();
        assertThat(accountBook.state().hasParent(account2, List.of(account1, account3))).isTrue();
        assertThat(accountBook.state().hasParent(account2, List.of(account3))).isFalse();
        assertThat(accountBook.state().hasParent(account3, List.of(account1, account2))).isTrue();
        assertThat(accountBook.state().hasParent(account3, List.of(account2))).isTrue();

        assertThat(accountBook.pendingEvents()).isEmpty();
    }

    @Test
    public void should_get_the_list_of_accounts_that_received_money_from_me() {
        // Given
        final var account0 = AccountId.of(SponsorAccount.Id.random());
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var account4 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account0, PositiveAmount.of(100L)),
                new TransferEvent(account0, account1, PositiveAmount.of(100L)),
                new RefundEvent(account1, account0, PositiveAmount.of(1L)),

                new TransferEvent(account1, account2, PositiveAmount.of(10L)),
                new TransferEvent(account2, account3, PositiveAmount.of(5L)),
                new RefundEvent(account3, account2, PositiveAmount.of(1L)),
                new TransferEvent(account1, account4, PositiveAmount.of(42L))
        );

        // When
        final List<AccountBook.Transaction> transactionsFromAccount1 = accountBook.state().transactionsFrom(account1);
        final List<AccountBook.Transaction> transactionsFromAccount2 = accountBook.state().transactionsFrom(account2);
        final List<AccountBook.Transaction> transactionsFromAccount3 = accountBook.state().transactionsFrom(account3);
        final List<AccountBook.Transaction> transactionsFromAccount4 = accountBook.state().transactionsFrom(account4);

        // Then
        assertThat(transactionsFromAccount1).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account1, account2, PositiveAmount.of(10L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account2, account3, PositiveAmount.of(4L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account1, account4, PositiveAmount.of(42L))
        );
        assertThat(transactionsFromAccount2).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account2, account3, PositiveAmount.of(4L))
        );
        assertThat(transactionsFromAccount3).isEmpty();
        assertThat(transactionsFromAccount4).isEmpty();

        assertThat(accountBook.pendingEvents()).isEmpty();
    }

    @Test
    public void should_get_the_list_of_accounts_that_sent_money_to_me() {
        // Given
        final var account0 = AccountId.of(SponsorAccount.Id.random());
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());
        final var account4 = AccountId.of(SponsorAccount.Id.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(account0, PositiveAmount.of(100L)),
                new TransferEvent(account0, account1, PositiveAmount.of(100L)),
                new RefundEvent(account1, account0, PositiveAmount.of(1L)),

                new TransferEvent(account1, account2, PositiveAmount.of(10L)),
                new TransferEvent(account2, account3, PositiveAmount.of(5L)),
                new RefundEvent(account3, account2, PositiveAmount.of(1L)),
                new TransferEvent(account1, account4, PositiveAmount.of(42L))
        );

        // When
        final List<AccountBook.Transaction> transactionsToAccount1 = accountBook.state().transactionsTo(account1);
        final List<AccountBook.Transaction> transactionsToAccount2 = accountBook.state().transactionsTo(account2);
        final List<AccountBook.Transaction> transactionsToAccount3 = accountBook.state().transactionsTo(account3);
        final List<AccountBook.Transaction> transactionsToAccount4 = accountBook.state().transactionsTo(account4);

        // Then
        assertThat(transactionsToAccount1).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account0, account1, PositiveAmount.of(99L))
        );
        assertThat(transactionsToAccount2).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account0, account1, PositiveAmount.of(99L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account1, account2, PositiveAmount.of(10L))
        );
        assertThat(transactionsToAccount3).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account0, account1, PositiveAmount.of(99L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account1, account2, PositiveAmount.of(10L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account2, account3, PositiveAmount.of(4L))
        );
        assertThat(transactionsToAccount4).containsExactlyInAnyOrder(
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account0, account1, PositiveAmount.of(99L)),
                new AccountBook.Transaction(AccountBook.Transaction.Type.TRANSFER, account1, account4, PositiveAmount.of(42L))
        );

        assertThat(accountBook.pendingEvents()).isEmpty();
    }


    @Test
    public void should_get_the_list_of_origin_accounts_that_sent_money_to_me_with_the_corresponding_amount() {
        // Given
        final var account0 = AccountId.of(SponsorAccount.Id.random());
        final var account1 = AccountId.of(SponsorAccount.Id.random());
        final var account2 = AccountId.of(SponsorAccount.Id.random());
        final var account3 = AccountId.of(SponsorAccount.Id.random());

        final var accountBook = accountBookFromEvents(
                new MintEvent(account0, PositiveAmount.of(100L)),
                new MintEvent(account1, PositiveAmount.of(100L)),

                new TransferEvent(account0, account2, PositiveAmount.of(30L)),
                new RefundEvent(account2, account0, PositiveAmount.of(5L)),
                new TransferEvent(account2, account3, PositiveAmount.of(20L)),

                new TransferEvent(account1, account3, PositiveAmount.of(15L)),

                new TransferEvent(account0, account2, PositiveAmount.of(50L)),
                new TransferEvent(account2, account3, PositiveAmount.of(10L))
        );

        // When
        final var amounts = accountBook.state().balancePerOrigin(account3);

        // Then
        assertThat(amounts).containsOnly(
                Map.entry(account0, PositiveAmount.of(30L)),
                Map.entry(account1, PositiveAmount.of(15L))
        );
    }


    @Test
    void should_get_the_amount_received_by_an_account() {
        // Given
        final var sponsor = AccountId.of(SponsorAccount.Id.random());
        final var sponsor2 = AccountId.of(SponsorAccount.Id.random());
        final var project = AccountId.of(ProjectId.random());
        final var reward = AccountId.of(RewardId.random());
        final var accountBook = accountBookFromEvents(
                new MintEvent(sponsor, PositiveAmount.of(100L)),
                new MintEvent(sponsor2, PositiveAmount.of(100L))
        );

        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(0L));
        }

        accountBook.transfer(sponsor, project, PositiveAmount.of(50L));
        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(50L));
        }

        accountBook.refund(project, sponsor, PositiveAmount.of(5L));
        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(45L));
        }

        accountBook.transfer(project, reward, PositiveAmount.of(10L));
        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(45L));
        }

        accountBook.refund(reward);
        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(45L));
        }

        accountBook.transfer(sponsor2, project, PositiveAmount.of(100L));
        {
            // When
            final var amount = accountBook.state().amountReceivedBy(project);
            // Then
            assertThat(amount).isEqualTo(PositiveAmount.of(145L));
        }
    }

    //@Test
    public void benchmark() {
        // Given
        final var iterations = 100;
        final var sponsorAccountCount = 2L;
        final var projectCount = 40L;
        final var rewardCount = projectCount * 200L;

        final List<AccountBookEvent> events = new ArrayList<>();

        final List<AccountId> sponsorAccounts = new ArrayList<>();
        for (int i = 0; i < sponsorAccountCount; i++) {
            final var account = AccountId.of(SponsorAccount.Id.random());
            sponsorAccounts.add(account);
            events.add(new MintEvent(account, PositiveAmount.of(projectCount * rewardCount)));
        }

        final List<AccountId> projects = new ArrayList<>();
        for (int i = 0; i < projectCount; i++) {
            final var account = AccountId.of(ProjectId.random());
            projects.add(account);
            events.add(new TransferEvent(
                    sponsorAccounts.get(i % sponsorAccounts.size()),
                    account,
                    PositiveAmount.of(rewardCount)));
        }

        final List<AccountId> rewards = new ArrayList<>();
        for (int i = 0; i < rewardCount; i++) {
            final var account = AccountId.of(RewardId.random());
            rewards.add(account);
            events.add(new TransferEvent(
                    projects.get(i % projects.size()),
                    account,
                    PositiveAmount.of(1L)));

            if (i % 10 == 0) {
                events.add(new RefundEvent(
                        account,
                        projects.get(i % projects.size()),
                        PositiveAmount.of(BigDecimal.valueOf(0.1))));
            }
        }

        System.out.printf("Benchmarking with %d events%n", events.size());

        Runtime.getRuntime().gc();
        final var startMemory = Runtime.getRuntime().totalMemory();
        final var startTime = System.currentTimeMillis();
        accountBookFromEvents(events);
        final var endMemory = Runtime.getRuntime().totalMemory();
        for (int i = 1; i < iterations; i++) {
            accountBookFromEvents(events);
        }
        final var endTime = System.currentTimeMillis();
        System.out.printf("Benchmark took %d ms and %d MB%n", (endTime - startTime) / iterations, (endMemory - startMemory) / 1024 / 1024);
    }
}
