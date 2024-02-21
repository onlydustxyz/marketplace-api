package onlydust.com.marketplace.api.postgres.adapter.mapper;

import onlydust.com.marketplace.project.domain.model.UserPayoutSettings;
import onlydust.com.marketplace.project.domain.model.bank.AccountNumber;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.BankAccountEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.UserPayoutInfoEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.WalletEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import onlydust.com.marketplace.kernel.model.blockchain.Aptos;
import onlydust.com.marketplace.kernel.model.blockchain.Ethereum;
import onlydust.com.marketplace.kernel.model.blockchain.Optimism;
import onlydust.com.marketplace.kernel.model.blockchain.StarkNet;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Wallet;

import java.util.Arrays;
import java.util.UUID;

import static java.util.Objects.nonNull;

public interface UserPayoutInfoMapper {

    static UserPayoutSettings mapEntityToDomain(final UserPayoutInfoEntity userPayoutInfoEntity,
                                                final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity) {
        UserPayoutSettings userPayoutSettings = UserPayoutSettings.builder()
                .pendingPaymentsCurrencies(Arrays.stream(userPayoutInfoValidationEntity.getPaymentRequestsCurrencies())
                        .map(CurrencyEnumEntity::toDomain).toList())
                .build();
        userPayoutSettings = mapWalletsToDomain(userPayoutInfoEntity, userPayoutSettings);
        userPayoutSettings = mapBankingAccountToDomain(userPayoutInfoEntity, userPayoutSettings);
        return userPayoutSettings;

    }

    private static UserPayoutSettings mapBankingAccountToDomain(UserPayoutInfoEntity userPayoutInfoEntity,
                                                                UserPayoutSettings payoutSettings) {
        if (nonNull(userPayoutInfoEntity.getBankAccount())) {
            payoutSettings = payoutSettings.toBuilder()
                    .sepaAccount(UserPayoutSettings.SepaAccount.builder()
                            .bic(userPayoutInfoEntity.getBankAccount().getBic())
                            .accountNumber(AccountNumber.of(userPayoutInfoEntity.getBankAccount().getIban()))
                            .build())
                    .build();
        }
        return payoutSettings;
    }

    private static UserPayoutSettings mapWalletsToDomain(UserPayoutInfoEntity userPayoutInfoEntity,
                                                         UserPayoutSettings payoutSettings) {
        if (!userPayoutInfoEntity.getWallets().isEmpty()) {
            for (WalletEntity wallet : userPayoutInfoEntity.getWallets()) {
                if (wallet.getNetwork().equals(NetworkEnumEntity.aptos)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .aptosAddress(Aptos.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getNetwork().equals(NetworkEnumEntity.starknet)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .starknetAddress(StarkNet.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getNetwork().equals(NetworkEnumEntity.optimism)) {
                    payoutSettings = payoutSettings.toBuilder()
                            .optimismAddress(Optimism.accountAddress(wallet.getAddress()))
                            .build();
                }
                if (wallet.getNetwork().equals(NetworkEnumEntity.ethereum)) {
                    switch (wallet.getType()) {
                        case address:
                            payoutSettings = payoutSettings.toBuilder()
                                    .ethWallet(new Wallet(Ethereum.accountAddress(wallet.getAddress())))
                                    .build();
                            break;
                        case name:
                            payoutSettings = payoutSettings.toBuilder()
                                    .ethWallet(new Wallet(Ethereum.name(wallet.getAddress())))
                                    .build();
                            break;
                    }
                }
            }
        }
        return payoutSettings;
    }

    static UserPayoutInfoEntity mapDomainToEntity(UUID userId, UserPayoutSettings userPayoutSettings) {
        UserPayoutInfoEntity entity = UserPayoutInfoEntity.builder()
                .userId(userId)
                .build();
        if (nonNull(userPayoutSettings.getSepaAccount())) {
            entity = entity.toBuilder()
                    .bankAccount(BankAccountEntity.builder()
                            .bic(userPayoutSettings.getSepaAccount().getBic())
                            .iban(userPayoutSettings.getSepaAccount().getAccountNumber().asString())
                            .userId(userId)
                            .build())
                    .build();
        }
        mapWalletsToEntity(userId, userPayoutSettings, entity);
        return entity;
    }

    private static void mapWalletsToEntity(UUID userId, UserPayoutSettings payoutSettings,
                                           UserPayoutInfoEntity entity) {
        if (nonNull(payoutSettings.getAptosAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getAptosAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .userId(userId)
                    .network(NetworkEnumEntity.aptos)
                    .build());
        }
        if (nonNull(payoutSettings.getOptimismAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getOptimismAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .userId(userId)
                    .network(NetworkEnumEntity.optimism)
                    .build());
        }
        if (nonNull(payoutSettings.getStarknetAddress())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getStarknetAddress().toString())
                    .type(WalletTypeEnumEntity.address)
                    .userId(userId)
                    .network(NetworkEnumEntity.starknet)
                    .build());
        }
        if (nonNull(payoutSettings.getEthWallet())) {
            entity.addWallets(WalletEntity.builder()
                    .address(payoutSettings.getEthWallet().asString())
                    .type(payoutSettings.getEthWallet().accountAddress().isPresent() ?
                            WalletTypeEnumEntity.address : WalletTypeEnumEntity.name)
                    .userId(userId)
                    .network(NetworkEnumEntity.ethereum)
                    .build());
        }
    }
}
