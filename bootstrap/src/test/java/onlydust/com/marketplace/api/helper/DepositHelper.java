package onlydust.com.marketplace.api.helper;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.binary.Base32;
import com.github.javafaker.Faker;
import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.accounting.domain.model.accountbook.AccountingTransactionProjection;
import onlydust.com.marketplace.accounting.domain.port.in.AccountingFacadePort;
import onlydust.com.marketplace.accounting.domain.port.out.AccountBookStorage;
import onlydust.com.marketplace.accounting.domain.port.out.DepositStoragePort;
import onlydust.com.marketplace.accounting.domain.service.CurrentDateProvider;
import onlydust.com.marketplace.kernel.model.SponsorId;
import onlydust.com.marketplace.kernel.model.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class DepositHelper {
    @Autowired
    private DatabaseHelper databaseHelper;
    @Autowired
    private AccountingFacadePort accountingFacadePort;
    @Autowired
    private DepositStoragePort depositStoragePort;
    @Autowired
    private AccountBookStorage accountBookStorage;

    private final Faker faker = new Faker();

    public Deposit preview(UserId userId, SponsorId sponsorId, Network network) {
        return preview(userId, sponsorId, network, "0x" + faker.random().hex());
    }

    public Deposit preview(UserId userId, SponsorId sponsorId, Network network, String transactionReference) {
        return accountingFacadePort.previewDeposit(userId, sponsorId, network, transactionReference);
    }

    public Deposit.Id create(SponsorId sponsorId, Network network, Currency.Id currencyId, BigDecimal amount) {
        return create(sponsorId, network, currencyId, amount, Deposit.Status.COMPLETED);
    }

    public Deposit.Id create(SponsorId sponsorId, Network network, Currency.Id currencyId, BigDecimal amount, Deposit.Status status) {
        final var transactionId = UUID.randomUUID();
        final var depositId = UUID.randomUUID();
        final var billingInformation = new Deposit.BillingInformation(
                faker.lordOfTheRings().location(),
                faker.address().fullAddress(),
                faker.country().countryCode3(),
                String.valueOf(faker.random().nextLong()),
                faker.finance().bic(),
                faker.internet().emailAddress(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().emailAddress()
        );

        databaseHelper.executeQuery("""
                INSERT INTO accounting.transfer_transactions (id, blockchain, reference, index, timestamp, sender_address, recipient_address, amount, contract_address)
                VALUES (:id, cast(:blockchain as accounting.network), :reference, :index, :timestamp, :senderAddress, :recipientAddress, :amount, :contractAddress)
                """, Map.of(
                "id", transactionId,
                "blockchain", network.blockchain().get().name(),
                "reference", randomTransactionReference(network),
                "index", 0,
                "timestamp", CurrentDateProvider.now(),
                "senderAddress", randomAddress(network),
                "recipientAddress", randomAddress(network),
                "amount", amount,
                "contractAddress", randomAddress(network)
        ));
        databaseHelper.executeQuery("""
                INSERT INTO accounting.deposits (id, transaction_id, sponsor_id, currency_id, status, billing_information)
                VALUES (:id, :transactionId, :sponsorId, :currencyId, cast(:status as accounting.deposit_status), :billingInformation)
                """, Map.of(
                "id", depositId,
                "transactionId", transactionId,
                "sponsorId", sponsorId.value(),
                "currencyId", currencyId.value(),
                "status", status.name(),
                "billingInformation", billingInformation
        ));

        final var deposit = depositStoragePort.find(Deposit.Id.of(depositId)).orElseThrow();
        accountBookStorage.save(AccountingTransactionProjection.of(deposit));
        return Deposit.Id.of(depositId);
    }

    private String randomTransactionReference(Network network) {
        return switch (network) {
            case STELLAR -> faker.random().hex();
            default -> "0x" + faker.random().hex();
        };
    }

    private String randomAddress(Network network) {
        return switch (network) {
            case STELLAR -> new Base32().encodeAsString(faker.random().hex().getBytes()).replaceAll("=", "");
            default -> "0x" + faker.random().hex();
        };
    }
}
