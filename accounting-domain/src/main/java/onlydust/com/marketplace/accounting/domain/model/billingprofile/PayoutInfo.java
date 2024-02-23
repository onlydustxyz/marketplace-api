package onlydust.com.marketplace.accounting.domain.model.billingprofile;

import lombok.Builder;
import lombok.Getter;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;

@Builder(toBuilder = true)
@Getter
public class PayoutInfo {
    Wallet ethWallet;
    EvmAccountAddress optimismAddress;
    AptosAccountAddress aptosAddress;
    StarknetAccountAddress starknetAddress;
    BankAccount bankAccount;
}
