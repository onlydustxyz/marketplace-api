package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.api.contract.model.BillingProfilePayoutInfoRequest;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.*;

import static java.util.Objects.isNull;

public interface PayoutInfoMapper {
    static PayoutInfo mapToDomain(final BillingProfilePayoutInfoRequest request) {
        return PayoutInfo.builder()
                .aptosAddress(isNull(request.getAptosAddress()) ? null : Aptos.accountAddress(request.getAptosAddress()))
                .optimismAddress(isNull(request.getOptimismAddress()) ? null : Optimism.accountAddress(request.getOptimismAddress()))
                .starknetAddress(isNull(request.getStarknetAddress()) ? null : StarkNet.accountAddress(request.getStarknetAddress()))
                .stellarAccountId(isNull(request.getStellarAccountId()) ? null : Stellar.accountId(request.getStellarAccountId()))
                .ethWallet(isNull(request.getEthWallet()) ? null : Ethereum.wallet(request.getEthWallet()))
                .bankAccount(isNull(request.getBankAccount()) ? null : new BankAccount(request.getBankAccount().getBic(), request.getBankAccount().getNumber()))
                .build();
    }
}
