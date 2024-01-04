package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.*;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountingServiceTest {
    AccountingFacadePort accountingFacadePort;
    CommitteeAccountingStorage accountingStorage;
    SponsorId sponsorId;
    CommitteeId committeeId;
    ProjectId projectId;

    @BeforeEach
    void setUp() {
        accountingStorage = new CommitteeAccountingStorage();
        accountingFacadePort = new AccountingService(accountingStorage);
        sponsorId = SponsorId.random();
        committeeId = CommitteeId.random();
        projectId = ProjectId.random();
    }

    @Test
    public void should_receive_money_from_sponsor() {
        accountingFacadePort.registerTransferFromSponsor(sponsorId, committeeId, BigDecimal.ONE, Currency.Op,
                Network.OPTIMISM);

        assertThat(accountingStorage.getCommitteeBalances()).containsExactlyInAnyOrder(
                new CommitteeAccountingStorage.CommitteeBalance(committeeId, BigDecimal.valueOf(1L), Currency.Op)
        );

        accountingFacadePort.allocateFundsToProject(committeeId, projectId, BigDecimal.ONE, Currency.Op);
    }

    @Test
    public void should_not_spend_money_i_dont_have() {
        accountingFacadePort.registerTransferFromSponsor(sponsorId, committeeId, BigDecimal.ONE, Currency.Op,
                Network.OPTIMISM);

        assertThatThrownBy(() -> accountingFacadePort.allocateFundsToProject(committeeId, projectId,
                BigDecimal.valueOf(2L), Currency.Op))
                .isInstanceOf(OnlyDustException.class).hasMessage("Not enough funds");
    }

    @Test
    public void should_not_spend_money_from_another_committee() {
        final var otherCommitteeId = CommitteeId.random();
        accountingFacadePort.registerTransferFromSponsor(sponsorId, otherCommitteeId, BigDecimal.ONE, Currency.Op,
                Network.OPTIMISM);

        assertThatThrownBy(() -> accountingFacadePort.allocateFundsToProject(committeeId, projectId,
                BigDecimal.ONE, Currency.Op))
                .isInstanceOf(OnlyDustException.class).hasMessage("Not enough funds");
    }

    @Test
    public void should_not_mix_currencies() {
        accountingFacadePort.registerTransferFromSponsor(sponsorId, committeeId, BigDecimal.ONE, Currency.Op,
                Network.OPTIMISM);

        assertThatThrownBy(() -> accountingFacadePort.allocateFundsToProject(committeeId, projectId,
                BigDecimal.ONE, Currency.Usdc))
                .isInstanceOf(OnlyDustException.class).hasMessage("Not enough funds");
    }

    @Test
    public void should_refund_sponsor() {
        accountingFacadePort.registerTransferFromSponsor(sponsorId, committeeId, BigDecimal.ONE, Currency.Op,
                Network.OPTIMISM);

        accountingFacadePort.registerRefundToSponsor(sponsorId, BigDecimal.ONE, Currency.Op);
    }

    @Test
    public void should_only_refund_with_money_we_received_from_sponsor() {
        assertThatThrownBy(() -> accountingFacadePort.registerRefundToSponsor(sponsorId, BigDecimal.ONE, Currency.Op))
                .isInstanceOf(OnlyDustException.class).hasMessage("Not enough funds");
    }
}