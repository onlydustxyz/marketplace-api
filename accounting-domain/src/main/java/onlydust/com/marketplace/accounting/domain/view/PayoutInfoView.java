package onlydust.com.marketplace.accounting.domain.view;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

import java.util.HashSet;
import java.util.Set;

@Builder(toBuilder = true)
@Getter
public class PayoutInfoView {
    WalletLocator ethWallet;
    EvmAccountAddress optimismAddress;
    AptosAccountAddress aptosAddress;
    StarknetAccountAddress starknetAddress;
    BankAccount bankAccount;

    @Getter(AccessLevel.PROTECTED)
    @Builder.Default
    Set<Network> requiredNetworksForRewards = new HashSet<>();

    public boolean missingEthWallet() {
        return ethWallet == null && requiredNetworksForRewards.contains(Network.ETHEREUM);
    }

    public boolean missingOptimismAddress() {
        return optimismAddress == null && requiredNetworksForRewards.contains(Network.OPTIMISM);
    }

    public boolean missingAptosAddress() {
        return aptosAddress == null && requiredNetworksForRewards.contains(Network.APTOS);
    }

    public boolean missingStarknetAddress() {
        return starknetAddress == null && requiredNetworksForRewards.contains(Network.STARKNET);
    }

    public boolean missingBankAccount() {
        return bankAccount == null && requiredNetworksForRewards.contains(Network.SEPA);
    }
}
