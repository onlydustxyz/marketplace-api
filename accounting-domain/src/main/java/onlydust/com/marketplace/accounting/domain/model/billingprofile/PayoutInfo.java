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
import java.util.Optional;

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

    public Optional<Wallet> wallet(Network network) {
        return switch (network) {
            case ETHEREUM -> Optional.ofNullable(ethWallet).map(a -> new Wallet(Network.ETHEREUM, a.asString()));
            case OPTIMISM -> Optional.ofNullable(optimismAddress).map(a -> new Wallet(Network.OPTIMISM, a.toString()));
            case STARKNET -> Optional.ofNullable(starknetAddress).map(a -> new Wallet(Network.STARKNET, a.toString()));
            case APTOS -> Optional.ofNullable(aptosAddress).map(a -> new Wallet(Network.APTOS, a.toString()));
            case SEPA -> Optional.ofNullable(bankAccount).map(ba -> new Wallet(Network.SEPA, ba.accountNumber()));
            case SWIFT -> Optional.ofNullable(bankAccount).map(ba -> new Wallet(Network.SWIFT, ba.accountNumber()));
        };
    }
}
