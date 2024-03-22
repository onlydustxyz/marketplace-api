package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.view.PayoutInfoView;
import onlydust.com.marketplace.api.contract.model.BillingProfilePayoutInfoRequest;
import onlydust.com.marketplace.api.contract.model.BillingProfilePayoutInfoResponse;
import onlydust.com.marketplace.api.contract.model.BillingProfilePayoutInfoResponseBankAccount;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

import static java.util.Objects.isNull;

public interface PayoutInfoMapper {

    static PayoutInfo mapToDomain(final BillingProfilePayoutInfoRequest request) {
        return PayoutInfo.builder()
                .aptosAddress(isNull(request.getAptosAddress()) ? null : new AptosAccountAddress(request.getAptosAddress()))
                .optimismAddress(isNull(request.getOptimismAddress()) ? null : new EvmAccountAddress(request.getOptimismAddress()))
                .starknetAddress(isNull(request.getStarknetAddress()) ? null : new StarknetAccountAddress(request.getStarknetAddress()))
                .ethWallet(isNull(request.getEthWallet()) ? null : Ethereum.wallet(request.getEthWallet()))
                .bankAccount(isNull(request.getBankAccount()) ? null : new BankAccount(request.getBankAccount().getBic(), request.getBankAccount().getNumber()))
                .build();
    }

    static BillingProfilePayoutInfoResponse mapToResponse(final PayoutInfoView payoutInfo) {
        final BillingProfilePayoutInfoResponse response = new BillingProfilePayoutInfoResponse();
        response.setHasValidPayoutSettings(!payoutInfo.missingAptosAddress() &&
                                           !payoutInfo.missingOptimismAddress() &&
                                           !payoutInfo.missingEthWallet() &&
                                           !payoutInfo.missingStarknetAddress() &&
                                           !payoutInfo.missingBankAccount());

        response.setAptosAddress(isNull(payoutInfo.getAptosAddress()) ? null : payoutInfo.getAptosAddress().toString());
        response.setMissingAptosWallet(payoutInfo.missingAptosAddress());

        response.setOptimismAddress(isNull(payoutInfo.getOptimismAddress()) ? null : payoutInfo.getOptimismAddress().toString());
        response.setMissingOptimismWallet(payoutInfo.missingOptimismAddress());

        response.setEthWallet(isNull(payoutInfo.getEthWallet()) ? null : payoutInfo.getEthWallet().asString());
        response.setMissingEthWallet(payoutInfo.missingEthWallet());

        response.setStarknetAddress(isNull(payoutInfo.getStarknetAddress()) ? null : payoutInfo.getStarknetAddress().toString());
        response.setMissingStarknetWallet(payoutInfo.missingStarknetAddress());

        response.setBankAccount(isNull(payoutInfo.getBankAccount()) ? null :
                new BillingProfilePayoutInfoResponseBankAccount().bic(payoutInfo.getBankAccount().bic()).number(payoutInfo.getBankAccount().accountNumber()));
        response.setMissingBankAccount(payoutInfo.missingBankAccount());

        return response;
    }
}
