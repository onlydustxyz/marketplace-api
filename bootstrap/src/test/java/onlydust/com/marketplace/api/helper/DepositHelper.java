package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.kernel.model.SponsorId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepositHelper {
    @Autowired
    private AccountingFacadePort accountingFacadePort;

    private final Faker faker = new Faker();

    public Deposit create(SponsorId sponsorId, Network network) {
        return accountingFacadePort.previewDeposit(sponsorId, network, "0x" + faker.random().hex());
    }

    public Deposit create(SponsorId sponsorId, Network network, String transactionReference) {
        return accountingFacadePort.previewDeposit(sponsorId, network, transactionReference);
    }
}
