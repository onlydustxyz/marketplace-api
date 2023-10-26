package onlydust.com.marketplace.api.postgres.adapter.it.repository;

import onlydust.com.marketplace.api.domain.model.UserPayoutInformation;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.UserPayoutInfoValidationEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.*;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.CurrencyEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.NetworkEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletIdEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.old.type.WalletTypeEnumEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.mapper.UserPayoutInfoMapper;
import onlydust.com.marketplace.api.postgres.adapter.repository.CustomUserPayoutInfoRepository;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
public class CustomUserPayoutInfoRepositoryIT extends AbstractPostgresIT {

    @Autowired
    BankAccountRepository bankAccountRepository;
    @Autowired
    AuthUserRepository authUserRepository;
    @Autowired
    UserPayoutInfoRepository userPayoutInfoRepository;
    @Autowired
    PaymentRequestRepository paymentRequestRepository;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    CustomUserPayoutInfoRepository customUserPayoutInfoRepository;

    @Test
    void should_invalidate_missing_contact_info() {
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidLocation());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidCompany());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidPerson());
    }

    @Test
    void should_validate_location() {
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoMapper.mapDomainToEntity(user.getId(),
                UserPayoutInformation.builder()
                        .location(UserPayoutInformation.Location.builder()
                                .postalCode(faker.address().zipCode())
                                .city(faker.address().city())
                                .country(faker.address().country())
                                .address(faker.address().fullAddress())
                                .build())
                        .build()
        ));

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidLocation());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidCompany());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidPerson());
    }

    @Test
    void should_validate_person() {
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoMapper.mapDomainToEntity(user.getId(),
                UserPayoutInformation.builder()
                        .person(UserPayoutInformation.Person.builder()
                                .firstName(faker.name().firstName())
                                .lastName(faker.name().lastName())
                                .build())
                        .build()
        ));

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidLocation());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidCompany());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidPerson());
    }

    @Test
    void should_validate_company() {
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoMapper.mapDomainToEntity(user.getId(),
                UserPayoutInformation.builder()
                        .isACompany(true)
                        .company(UserPayoutInformation.Company.builder()
                                .owner(UserPayoutInformation.Person.builder()
                                        .firstName(faker.name().firstName())
                                        .lastName(faker.name().lastName())
                                        .build())
                                .identificationNumber(faker.number().digit())
                                .name(faker.name().name())
                                .build())
                        .build()
        ));

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidLocation());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidCompany());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidPerson());
    }


    @Test
    void should_validate_payout_settings_given_no_payment_requests() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_invalidate_usd_payout_settings_given_payment_request_with_no_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.usd)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_usd_payout_settings_given_payment_request_with_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .bankAccount(BankAccountEntity.builder()
                        .userId(user.getId())
                        .bic(faker.random().hex())
                        .iban(faker.name().bloodGroup())
                        .build())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.usd)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_invalidate_eth_payout_settings_given_payment_request_with_no_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.eth)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_usd_payout_settings_given_payment_request_with_linked_wallet_name() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .wallets(Set.of(
                        WalletEntity.builder()
                                .type(WalletTypeEnumEntity.name)
                                .address(faker.random().hex())
                                .id(
                                        WalletIdEntity.builder()
                                                .network(NetworkEnumEntity.ethereum)
                                                .userId(user.getId())
                                                .build()
                                )
                                .build()
                ))
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.eth)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_usd_payout_settings_given_payment_request_with_linked_wallet_address() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .wallets(Set.of(
                        WalletEntity.builder()
                                .type(WalletTypeEnumEntity.address)
                                .address(faker.random().hex())
                                .id(
                                        WalletIdEntity.builder()
                                                .network(NetworkEnumEntity.ethereum)
                                                .userId(user.getId())
                                                .build()
                                )
                                .build()
                ))
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.eth)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }


    @Test
    void should_invalidate_op_payout_settings_given_payment_request_with_no_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.op)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_op_payout_settings_given_payment_request_with_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .wallets(Set.of(
                        WalletEntity.builder()
                                .type(WalletTypeEnumEntity.name)
                                .address(faker.random().hex())
                                .id(
                                        WalletIdEntity.builder()
                                                .network(NetworkEnumEntity.optimism)
                                                .userId(user.getId())
                                                .build()
                                )
                                .build()
                ))
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.op)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }


    @Test
    void should_invalidate_stark_payout_settings_given_payment_request_with_no_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.stark)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_stark_payout_settings_given_payment_request_with_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .wallets(Set.of(
                        WalletEntity.builder()
                                .type(WalletTypeEnumEntity.name)
                                .address(faker.random().hex())
                                .id(
                                        WalletIdEntity.builder()
                                                .network(NetworkEnumEntity.starknet)
                                                .userId(user.getId())
                                                .build()
                                )
                                .build()
                ))
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.stark)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }


    @Test
    void should_invalidate_aptos_payout_settings_given_payment_request_with_no_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.apt)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(false, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }

    @Test
    void should_validate_aptos_payout_settings_given_payment_request_with_linked_wallet() {
        // Given
        final AuthUserEntity user = authUserRepository.save(AuthUserEntity.builder()
                .id(UUID.randomUUID())
                .githubUserId(faker.number().numberBetween(1L, 1000L))
                .isAdmin(false)
                .loginAtSignup(faker.rickAndMorty().character())
                .createdAt(new Date())
                .build());
        userPayoutInfoRepository.save(UserPayoutInfoEntity.builder()
                .userId(user.getId())
                .wallets(Set.of(
                        WalletEntity.builder()
                                .type(WalletTypeEnumEntity.name)
                                .address(faker.random().hex())
                                .id(
                                        WalletIdEntity.builder()
                                                .network(NetworkEnumEntity.starknet)
                                                .userId(user.getId())
                                                .build()
                                )
                                .build()
                ))
                .build());
        paymentRequestRepository.saveAll(
                List.of(
                        PaymentRequestEntity.builder()
                                .id(UUID.randomUUID())
                                .projectId(UUID.randomUUID())
                                .recipientId(user.getGithubUserId())
                                .requestorId(UUID.randomUUID())
                                .hoursWorked(1)
                                .currency(CurrencyEnumEntity.stark)
                                .requestedAt(new Date())
                                .amount(BigDecimal.ONE)
                                .build()
                )
        );

        // When
        final UserPayoutInfoValidationEntity userPayoutInfoValidationEntity =
                customUserPayoutInfoRepository.getUserPayoutInfoValidationEntity(user.getId()).orElseThrow();

        // Then
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidAptosWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidBakingAccount());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidEthWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidOptimismWallet());
        assertEquals(true, userPayoutInfoValidationEntity.getHasValidStarknetWallet());
    }


}
