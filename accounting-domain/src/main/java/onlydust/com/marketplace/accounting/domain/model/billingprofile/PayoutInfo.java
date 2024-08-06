package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import onlydust.com.marketplace.accounting.domain.model.Network;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.stellar.StellarAccountId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Builder(toBuilder = true)
public class PayoutInfo {
    WalletLocator ethWallet;
    EvmAccountAddress optimismAddress;
    AptosAccountAddress aptosAddress;
    StarknetAccountAddress starknetAddress;
    StellarAccountId stellarAccountId;
    BankAccount bankAccount;

    public Optional<WalletLocator> ethWallet() {
        return Optional.ofNullable(ethWallet);
    }

    public Optional<EvmAccountAddress> optimismAddress() {
        return Optional.ofNullable(optimismAddress);
    }

    public Optional<AptosAccountAddress> aptosAddress() {
        return Optional.ofNullable(aptosAddress);
    }

    public Optional<StarknetAccountAddress> starknetAddress() {
        return Optional.ofNullable(starknetAddress);
    }

    public Optional<StellarAccountId> stellarAccountId() {
        return Optional.ofNullable(stellarAccountId);
    }

    public Optional<BankAccount> bankAccount() {
        return Optional.ofNullable(bankAccount);
    }

    public List<Wallet> wallets() {
        final var wallets = new ArrayList<Wallet>();

        if (ethWallet != null) wallets.add(new Wallet(Network.ETHEREUM, ethWallet.asString()));
        if (optimismAddress != null) wallets.add(new Wallet(Network.OPTIMISM, optimismAddress.toString()));
        if (aptosAddress != null) wallets.add(new Wallet(Network.APTOS, aptosAddress.toString()));
        if (starknetAddress != null) wallets.add(new Wallet(Network.STARKNET, starknetAddress.toString()));
        if (stellarAccountId != null) wallets.add(new Wallet(Network.STELLAR, stellarAccountId.toString()));

        return wallets;
    }
}
