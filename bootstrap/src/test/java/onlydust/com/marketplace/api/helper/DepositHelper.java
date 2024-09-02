package onlydust.com.marketplace.api.helper;

import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DepositHelper {
    @Autowired
    private AccountingFacadePort accountingFacadePort;

    private final Faker faker = new Faker();

    public Deposit preview(SponsorId sponsorId, Network network) {
        return preview(sponsorId, network, "0x" + faker.random().hex());
    }

    public Deposit preview(SponsorId sponsorId, Network network, String transactionReference) {
        return accountingFacadePort.previewDeposit(sponsorId, network, transactionReference);
    }

    public Deposit create(UserId sponsorLeadId, SponsorId sponsorId, Network network) {
        final var deposit = preview(sponsorId, network, "0x" + faker.random().hex());
        accountingFacadePort.submitDeposit(sponsorLeadId, deposit.id(), new Deposit.BillingInformation(
                faker.company().name(),
                faker.address().fullAddress(),
                faker.address().country(),
                faker.idNumber().valid(),
                faker.idNumber().valid(),
                faker.internet().emailAddress(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress()
        ));
        return deposit;
    }
}
