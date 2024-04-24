package onlydust.com.marketplace.cli;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.web3j.crypto.Keys;

import java.util.Set;

@AllArgsConstructor
@Slf4j
@Profile("cli")
public class EvmWalletSanitizer implements CommandLineRunner {
    private final WalletRepository walletRepository;
    private final static Set<NetworkEnumEntity> EVM_NETWORKS = Set.of(NetworkEnumEntity.ethereum, NetworkEnumEntity.optimism);

    @Override
    public void run(String... args) {
        if (args.length == 0 || !args[0].equals("sanitize_evm_wallets")) return;

        walletRepository.findAll().stream()
                .filter(w -> EVM_NETWORKS.contains(w.getNetwork()))
                .filter(w -> w.getType() == WalletTypeEnumEntity.address)
                .forEach(wallet -> {
                    final var sanitized = Keys.toChecksumAddress(wallet.getAddress());
                    if (!wallet.getAddress().equals(sanitized)) {
                        LOGGER.info("Wallet {} sanitized: {}", wallet.getAddress(), sanitized);
                        wallet.setAddress(sanitized);
                        walletRepository.save(wallet);
                    }
                });
    }
}
