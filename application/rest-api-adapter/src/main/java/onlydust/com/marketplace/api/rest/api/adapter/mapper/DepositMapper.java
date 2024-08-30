package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.api.contract.model.DepositBillingInformation;
import onlydust.com.marketplace.api.contract.model.DepositSenderInformation;
import onlydust.com.marketplace.api.contract.model.PreviewDepositResponse;
import onlydust.com.marketplace.kernel.model.blockchain.Blockchain;
import onlydust.com.marketplace.project.domain.model.Sponsor;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.MoneyMapper.toMoney;

public interface DepositMapper {
    static PreviewDepositResponse toPreviewResponse(final @NonNull Deposit deposit,
                                                    final @NonNull Sponsor sponsor,
                                                    final @NonNull Amount currentBalance) {
        return new PreviewDepositResponse()
                .id(deposit.id().value())
                .amount(toMoney(deposit.transaction().amount(), deposit.currency()))
                .currentBalance(toMoney(currentBalance.getValue(), deposit.currency()))
                .finalBalance(toMoney(currentBalance.getValue().add(deposit.transaction().amount()), deposit.currency()))
                .senderInformation(toSenderInformation(sponsor, deposit.transaction()))
                .billingInformation(toBillingInformation(deposit.billingInformation()));
    }

    static DepositBillingInformation toBillingInformation(final Deposit.BillingInformation billingInformation) {
        return billingInformation == null ? null : new DepositBillingInformation()
                .companyName(billingInformation.companyName())
                .companyAddress(billingInformation.companyAddress())
                .companyCountry(billingInformation.companyCountry())
                .companyId(billingInformation.companyId())
                .vatNumber(billingInformation.vatNumber())
                .billingEmail(billingInformation.billingEmail())
                .firstName(billingInformation.firstName())
                .lastName(billingInformation.lastName())
                .email(billingInformation.email());
    }

    static DepositSenderInformation toSenderInformation(final @NonNull Sponsor sponsor,
                                                        final @NonNull Blockchain.TransferTransaction transaction) {
        return new DepositSenderInformation()
                .name(sponsor.name())
                .accountNumber(transaction.senderAddress())
                .transactionReference(transaction.reference());
    }
}
