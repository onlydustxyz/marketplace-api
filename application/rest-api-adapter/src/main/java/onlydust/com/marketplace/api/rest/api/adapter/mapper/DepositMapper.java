package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Deposit;
import onlydust.com.marketplace.api.contract.model.DepositBillingInformation;

public interface DepositMapper {
    static Deposit.BillingInformation fromBillingInformation(final DepositBillingInformation billingInformation) {
        return billingInformation == null ? null : new Deposit.BillingInformation(
                billingInformation.getCompanyName(),
                billingInformation.getCompanyAddress(),
                billingInformation.getCompanyCountry(),
                billingInformation.getCompanyId(),
                billingInformation.getVatNumber(),
                billingInformation.getBillingEmail(),
                billingInformation.getFirstName(),
                billingInformation.getLastName(),
                billingInformation.getEmail()
        );
    }
}
