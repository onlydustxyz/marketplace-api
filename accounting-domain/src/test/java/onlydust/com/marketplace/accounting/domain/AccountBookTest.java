package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountBookTest {

    AccountBook accountBook;

    @BeforeEach
    void setUp() {
        accountBook = new AccountBook();
    }

    @Test
    public void should_mint() {
        // Given
        final var account = Account.Id.random();
        final var amount = PositiveAmount.of(100L);

        // When
        accountBook.mint(account, amount);

        // Then no exception is thrown
        assertThat(accountBook.balanceOf(account)).isEqualTo(amount);
    }

    @Test
    public void should_burn() {
        // Given
        final var account = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(account, amount);

        // When
        accountBook.burn(account, amount);

        // Then no exception is thrown
        assertThat(accountBook.balanceOf(account)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_not_burn_money_we_dont_have() {
        // Given
        final var account = Account.Id.random();
        final var amount = PositiveAmount.of(100L);

        // When
        assertThatThrownBy(() -> accountBook.burn(account, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund %s from %s to %s".formatted(amount, account, AccountBookState.ROOT));

        // Then
        assertThat(accountBook.balanceOf(account)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_transfer_money_from_an_account_to_another() {
        // Given
        final var sender = Account.Id.random();
        final var recipient = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(sender, amount);

        // When
        accountBook.transfer(sender, recipient, amount);

        // Then
        assertThat(accountBook.balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(recipient)).isEqualTo(amount);
    }

    @Test
    public void should_not_transfer_money_from_an_account_to_itself() {
        // Given
        final var sender = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(sender, amount);

        // When
        assertThatThrownBy(() -> accountBook.transfer(sender, sender, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("An account (%s) cannot transfer money to itself".formatted(sender));

        // Then
        assertThat(accountBook.balanceOf(sender)).isEqualTo(amount);
    }

    @Test
    public void should_not_transfer_money_we_dont_have() {
        // Given
        final var sender = Account.Id.random();
        final var recipient = Account.Id.random();
        final var amount = PositiveAmount.of(100L);

        // When
        assertThatThrownBy(() -> accountBook.transfer(sender, recipient, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot transfer %s from %s to %s".formatted(amount, sender, recipient));

        // Then
        assertThat(accountBook.balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_get_refund_from_an_account_to_another() {
        // Given
        final var sender = Account.Id.random();
        final var recipient = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(sender, amount);
        accountBook.transfer(sender, recipient, amount);
        assertThat(accountBook.balanceOf(sender)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(recipient)).isEqualTo(amount);

        // When
        accountBook.refund(recipient, sender, amount);

        // Then
        assertThat(accountBook.balanceOf(sender)).isEqualTo(amount);
        assertThat(accountBook.balanceOf(recipient)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_refund_refunded_money() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(account1, amount);
        accountBook.transfer(account1, account2, amount);
        accountBook.transfer(account2, account3, amount);
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account3)).isEqualTo(amount);

        // When
        accountBook.refund(account3, account2, amount);
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);

        // Then
        assertThat(accountBook.balanceOf(account2)).isEqualTo(amount);
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.ZERO);

        // When
        accountBook.refund(account2, account1, amount);
        assertThat(accountBook.balanceOf(account1)).isEqualTo(amount);

        // Then
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_refund_money_from_account_with_partial_spent() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        accountBook.mint(account1, PositiveAmount.of(100L));
        accountBook.transfer(account1, account2, PositiveAmount.of(100L));
        accountBook.transfer(account2, account3, PositiveAmount.of(60L));
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(40L));
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.of(60L));

        // When
        accountBook.refund(account2, account1, PositiveAmount.of(10L));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.of(10L));
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(30L));

        // When
        accountBook.refund(account2, account1, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.of(40L));
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_not_refund_more_than_received() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(account1, amount);
        accountBook.transfer(account1, account2, amount);
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(amount);

        // When
        assertThatThrownBy(() -> accountBook.refund(account2, account1, amount.add(PositiveAmount.of(1L))))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund %s from %s to %s".formatted(amount.add(PositiveAmount.of(1L)), account2, account1));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(amount);
    }

    @Test
    public void should_not_refund_spent_money() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(account1, amount);
        accountBook.transfer(account1, account2, amount);
        accountBook.transfer(account2, account3, PositiveAmount.of(50L));
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.of(50L));

        // When
        assertThatThrownBy(() -> accountBook.refund(account2, account1, amount))
                .isInstanceOf(OnlyDustException.class)
                .hasMessage("Cannot refund %s from %s to %s".formatted(amount, account2, account1));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.ZERO);
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.of(50L));
    }

    @Test
    public void should_receive_money_from_multiple_accounts() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        accountBook.mint(account1, PositiveAmount.of(100L));
        accountBook.mint(account2, PositiveAmount.of(100L));

        // When
        accountBook.transfer(account1, account3, PositiveAmount.of(50L));
        accountBook.transfer(account2, account3, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(70L));
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.of(80L));
    }

    @Test
    public void should_transfer_to_multiple_accounts() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        accountBook.mint(account1, PositiveAmount.of(100L));

        // When
        accountBook.transfer(account1, account2, PositiveAmount.of(50L));
        accountBook.transfer(account1, account3, PositiveAmount.of(30L));

        // Then
        assertThat(accountBook.balanceOf(account1)).isEqualTo(PositiveAmount.of(20L));
        assertThat(accountBook.balanceOf(account2)).isEqualTo(PositiveAmount.of(50L));
        assertThat(accountBook.balanceOf(account3)).isEqualTo(PositiveAmount.of(30L));
    }

    @Test
    public void should_handle_multiple_transfers_and_refunds_from_multiple_accounts() {
        // Given
        final var sponsor1 = Account.Id.random();
        final var sponsor2 = Account.Id.random();
        final var committee1 = Account.Id.random();
        final var committee2 = Account.Id.random();
        final var project1 = Account.Id.random();
        final var project2 = Account.Id.random();
        final var contributor = Account.Id.random();
        accountBook.mint(sponsor1, PositiveAmount.of(100_000L));
        accountBook.mint(sponsor2, PositiveAmount.of(100_000L));

        accountBook.transfer(sponsor1, committee1, PositiveAmount.of(8_000L));
        accountBook.transfer(sponsor1, committee2, PositiveAmount.of(1_000L));

        accountBook.transfer(sponsor2, committee1, PositiveAmount.of(15_000L));
        accountBook.transfer(sponsor2, committee2, PositiveAmount.of(500L));
        accountBook.transfer(sponsor2, committee2, PositiveAmount.of(4_500L));

        accountBook.transfer(committee1, project1, PositiveAmount.of(8_000L));
        accountBook.transfer(committee1, project1, PositiveAmount.of(12_000L));
        accountBook.transfer(committee1, project2, PositiveAmount.of(2_000L));

        accountBook.transfer(committee2, project2, PositiveAmount.of(1_000L));
        accountBook.transfer(committee2, project2, PositiveAmount.of(500L));
        accountBook.transfer(committee2, project2, PositiveAmount.of(500L));
        accountBook.transfer(committee2, project2, PositiveAmount.of(1_000L));

        accountBook.transfer(project2, contributor, PositiveAmount.of(2_000L));
        accountBook.transfer(project2, contributor, PositiveAmount.of(1_000L));
        accountBook.transfer(project2, contributor, PositiveAmount.of(500L));
        accountBook.transfer(project2, contributor, PositiveAmount.of(100L));

        // Then
        assertThat(accountBook.balanceOf(sponsor1)).isEqualTo(PositiveAmount.of(91_000L));
        assertThat(accountBook.balanceOf(sponsor2)).isEqualTo(PositiveAmount.of(80_000L));
        assertThat(accountBook.balanceOf(committee1)).isEqualTo(PositiveAmount.of(1_000L));
        assertThat(accountBook.balanceOf(committee2)).isEqualTo(PositiveAmount.of(3_000L));
        assertThat(accountBook.balanceOf(project1)).isEqualTo(PositiveAmount.of(20_000L));
        assertThat(accountBook.balanceOf(project2)).isEqualTo(PositiveAmount.of(1_400L));
        assertThat(accountBook.balanceOf(contributor)).isEqualTo(PositiveAmount.of(3_600L));

        // When
        accountBook.refund(project2, committee2, PositiveAmount.of(700L));

        // Then
        assertThat(accountBook.balanceOf(project2)).isEqualTo(PositiveAmount.of(700L));
        assertThat(accountBook.balanceOf(committee2)).isEqualTo(PositiveAmount.of(3_700L));
        // Check other accounts are not impacted
        assertThat(accountBook.balanceOf(sponsor1)).isEqualTo(PositiveAmount.of(91_000L));
        assertThat(accountBook.balanceOf(sponsor2)).isEqualTo(PositiveAmount.of(80_000L));
        assertThat(accountBook.balanceOf(committee1)).isEqualTo(PositiveAmount.of(1_000L));
        assertThat(accountBook.balanceOf(project1)).isEqualTo(PositiveAmount.of(20_000L));
        assertThat(accountBook.balanceOf(contributor)).isEqualTo(PositiveAmount.of(3_600L));
    }

    @Test
    public void should_get_account_balance() {
        // Given
        final var accountBook = new AccountBook();
        final var recipient = Account.Id.random();
        final var amount = PositiveAmount.of(100L);
        accountBook.mint(recipient, amount);

        // When
        final Amount balance = accountBook.balanceOf(recipient);

        // Then
        assertThat(balance).isEqualTo(amount);
    }

    @Test
    public void should_get_refundable_balance() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        accountBook.mint(account1, PositiveAmount.of(100L));
        accountBook.transfer(account1, account2, PositiveAmount.of(50L));
        accountBook.transfer(account1, account3, PositiveAmount.of(30L));
        accountBook.transfer(account2, account3, PositiveAmount.of(5L));

        // When
        final var refundableBalanceFromAccount2ToAccount1 = accountBook.refundableBalance(account2, account1);
        final var refundableBalanceFromAccount3ToAccount1 = accountBook.refundableBalance(account3, account1);
        final var refundableBalanceFromAccount3ToAccount2 = accountBook.refundableBalance(account3, account2);
        final var refundableBalanceFromAccount1ToAccount3 = accountBook.refundableBalance(account1, account3);

        // Then
        assertThat(refundableBalanceFromAccount2ToAccount1).isEqualTo(PositiveAmount.of(45L));
        assertThat(refundableBalanceFromAccount3ToAccount1).isEqualTo(PositiveAmount.of(30L));
        assertThat(refundableBalanceFromAccount3ToAccount2).isEqualTo(PositiveAmount.of(5L));
        assertThat(refundableBalanceFromAccount1ToAccount3).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_get_the_amount_of_money_transferred_from_an_account_to_another() {
        // Given
        final var account1 = Account.Id.random();
        final var account2 = Account.Id.random();
        final var account3 = Account.Id.random();
        accountBook.mint(account1, PositiveAmount.of(100L));
        accountBook.transfer(account1, account2, PositiveAmount.of(50L));
        accountBook.transfer(account2, account3, PositiveAmount.of(5L));
        accountBook.refund(account2, account1, PositiveAmount.of(10L));

        // When
        final var transferredAmountFromAccount1ToAccount2 = accountBook.transferredAmount(account1, account2);
        final var transferredAmountFromAccount1ToAccount3 = accountBook.transferredAmount(account1, account3);
        final var transferredAmountFromAccount2ToAccount3 = accountBook.transferredAmount(account2, account3);
        final var transferredAmountFromAccount2ToAccount1 = accountBook.transferredAmount(account2, account1);
        final var transferredAmountFromAccount3ToAccount1 = accountBook.transferredAmount(account3, account1);

        // Then
        assertThat(transferredAmountFromAccount1ToAccount2).isEqualTo(PositiveAmount.of(40L));
        assertThat(transferredAmountFromAccount1ToAccount3).isEqualTo(PositiveAmount.of(5L));
        assertThat(transferredAmountFromAccount2ToAccount3).isEqualTo(PositiveAmount.of(5L));
        assertThat(transferredAmountFromAccount2ToAccount1).isEqualTo(PositiveAmount.ZERO);
        assertThat(transferredAmountFromAccount3ToAccount1).isEqualTo(PositiveAmount.ZERO);
    }

    @Test
    public void should_get_the_list_of_accounts_that_received_money_from_me() {
    }

    @Test
    public void should_get_the_list_of_accounts_that_sent_money_to_me() {
    }
}
