package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@Getter
public class PayoutInfo {
    WalletLocator ethWallet;
    EvmAccountAddress optimismAddress;
    AptosAccountAddress aptosAddress;
    StarknetAccountAddress starknetAddress;
    BankAccount bankAccount;

    public List<Wallet> wallets() {
        final var wallets = new ArrayList<Wallet>();
        if (ethWallet != null) {
            wallets.add(new Wallet(Network.ETHEREUM, ethWallet.asString()));
        }
        if (optimismAddress != null) {
            wallets.add(new Wallet(Network.OPTIMISM, optimismAddress.toString()));
        }
        if (aptosAddress != null) {
            wallets.add(new Wallet(Network.APTOS, aptosAddress.toString()));
        }
        if (starknetAddress != null) {
            wallets.add(new Wallet(Network.STARKNET, starknetAddress.toString()));
        }
        return wallets;
    }
}
