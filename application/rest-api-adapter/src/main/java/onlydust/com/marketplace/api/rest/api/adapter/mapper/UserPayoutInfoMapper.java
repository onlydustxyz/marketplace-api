package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.api.contract.model.UserPayoutSettingsRequest;
import onlydust.com.marketplace.api.contract.model.UserPayoutSettingsResponse;
import onlydust.com.marketplace.api.contract.model.UserPayoutSettingsResponseSepaAccount;
import onlydust.com.marketplace.api.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.api.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {

    static UserPayoutSettingsResponse userPayoutSettingsToResponse(UserPayoutSettings view) {
        final UserPayoutSettingsResponse contract = new UserPayoutSettingsResponse();
        contract.setHasValidPayoutSettings(view.hasValidPayoutSettings());
        if (view.getEthWallet() != null)
            contract.setEthWallet(view.getEthWallet().asString());
        if (view.getAptosAddress() != null)
            contract.setAptosAddress(view.getAptosAddress().toString());
        if (view.getOptimismAddress() != null)
            contract.setOptimismAddress(view.getOptimismAddress().toString());
        if (view.getStarknetAddress() != null)
            contract.setStarknetAddress(view.getStarknetAddress().toString());
        if (nonNull(view.getSepaAccount())) {
            final var sepaAccount = new UserPayoutSettingsResponseSepaAccount();
            sepaAccount.setBic(view.getSepaAccount().getBic());
            sepaAccount.setIban(view.getSepaAccount().getAccountNumber().asString());
            contract.setSepaAccount(sepaAccount);
        }

        contract.missingEthWallet(view.isMissingEthereumWallet());
        contract.missingAptosWallet(view.isMissingAptosWallet());
        contract.missingOptimismWallet(view.isMissingOptimismWallet());
        contract.missingStarknetWallet(view.isMissingStarknetWallet());
        contract.missingSepaAccount(view.isMissingSepaAccount());
        return contract;
    }


    static UserPayoutSettings userPayoutSettingsToDomain(final UserPayoutSettingsRequest contract) {

        UserPayoutSettings userPayoutSettings = UserPayoutSettings.builder()

                .starknetAddress(contract.getStarknetAddress() == null ? null :
                        StarkNet.accountAddress(contract.getStarknetAddress()))
                .aptosAddress(contract.getAptosAddress() == null ? null :
                        Aptos.accountAddress(contract.getAptosAddress()))
                .optimismAddress(contract.getOptimismAddress() == null ? null :
                        Optimism.accountAddress(contract.getOptimismAddress()))
                .ethWallet(contract.getEthWallet() == null ? null :
                        Ethereum.wallet(contract.getEthWallet()))
                .build();
        if (nonNull(contract.getSepaAccount())) {
            final var iban = AccountNumber.of(contract.getSepaAccount().getIban());
            userPayoutSettings = userPayoutSettings.toBuilder()
                    .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                            .bic(contract.getSepaAccount().getBic())
                            .accountNumber(iban)
                            .build())
                    .build();
        }
        return userPayoutSettings;
    }
}
