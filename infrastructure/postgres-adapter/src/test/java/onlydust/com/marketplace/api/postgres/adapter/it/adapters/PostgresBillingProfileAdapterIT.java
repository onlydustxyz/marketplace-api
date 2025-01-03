package onlydust.com.marketplace.api.postgres.adapter.it.adapters;

import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.kernel.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.CompanyBillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.PayoutInfo;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.kernel.model.UserId;
import onlydust.com.marketplace.api.postgres.adapter.PostgresBillingProfileAdapter;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.UserEntity;
import onlydust.com.marketplace.api.postgres.adapter.it.AbstractPostgresIT;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.model.AuthenticatedUser;
import onlydust.com.marketplace.kernel.model.bank.BankAccount;
import onlydust.com.marketplace.kernel.model.blockchain.aptos.AptosAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.EvmAccountAddress;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.Name;
import onlydust.com.marketplace.kernel.model.blockchain.evm.ethereum.WalletLocator;
import onlydust.com.marketplace.kernel.model.blockchain.starknet.StarknetAccountAddress;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PostgresBillingProfileAdapterIT extends AbstractPostgresIT {

    @Autowired
    PostgresBillingProfileAdapter postgresBillingProfileAdapter;
    @Autowired
    KybRepository kybRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PayoutPreferenceRepository payoutPreferenceRepository;
    @Autowired
    WalletRepository walletRepository;
    @Autowired
    BillingProfileUserInvitationRepository billingProfileUserInvitationRepository;
    @Autowired
    BillingProfileUserRepository billingProfileUserRepository;
    @Autowired
    ChildrenKycRepository childrenKycRepository;
    @Autowired
    BankAccountRepository bankAccountRepository;

    @Test
    void should_delete_billing_profile_with_linked_entities() {
        // Given
        final UserId ownerId = UserId.random();
        userRepository.save(new UserEntity(ownerId.value(), 1L, faker.rickAndMorty().character(), faker.internet().url(), faker.internet().emailAddress(),
                new AuthenticatedUser.Role[]{AuthenticatedUser.Role.ADMIN}, new Date(), new Date(), new Date(), null));
        final UserId adminId = UserId.random();
        userRepository.save(new UserEntity(adminId.value(), 2L, faker.rickAndMorty().character(), faker.internet().url(), faker.internet().emailAddress(),
                new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER}, new Date(), new Date(), new Date(), null));
        final UserId memberId = UserId.random();
        userRepository.save(new UserEntity(memberId.value(), 3L, faker.rickAndMorty().character(), faker.internet().url(), faker.internet().emailAddress(),
                new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER}, new Date(), new Date(), new Date(), null));
        final UserId coworkerId = UserId.random();
        userRepository.save(new UserEntity(coworkerId.value(), 4L, faker.rickAndMorty().character(), faker.internet().url(), faker.internet().emailAddress(),
                new AuthenticatedUser.Role[]{AuthenticatedUser.Role.USER}, new Date(), new Date(), new Date(), null));

        final String name = faker.rickAndMorty().character();
        CompanyBillingProfile billingProfile = new CompanyBillingProfile(name, ownerId);
        postgresBillingProfileAdapter.save(billingProfile);
        final BillingProfile.Id billingProfileId = billingProfile.id();

        final var kyb = kybRepository.findByBillingProfileId(billingProfileId.value()).orElseThrow().toDomain();
        final String kybApplicantId = faker.internet().emailAddress();
        postgresBillingProfileAdapter.saveKyb(kyb.toBuilder()
                .usEntity(false)
                .externalApplicantId(faker.hacker().verb())
                .euVATNumber(faker.gameOfThrones().character())
                .country(Country.fromIso3("ARG"))
                .status(VerificationStatus.VERIFIED)
                .registrationDate(new Date())
                .ownerId(ownerId)
                .address(faker.address().fullAddress())
                .externalApplicantId(kybApplicantId)
                .build());
        postgresBillingProfileAdapter.saveChildrenKyc(faker.rickAndMorty().location(), kybApplicantId, VerificationStatus.CLOSED);
        postgresBillingProfileAdapter.saveCoworker(billingProfileId, adminId, BillingProfile.User.Role.ADMIN, ZonedDateTime.now());
        postgresBillingProfileAdapter.saveCoworker(billingProfileId, memberId, BillingProfile.User.Role.MEMBER, ZonedDateTime.now());
        postgresBillingProfileAdapter.saveCoworkerInvitation(billingProfileId, coworkerId, GithubUserId.of(1L), BillingProfile.User.Role.MEMBER,
                ZonedDateTime.now());
        postgresBillingProfileAdapter.savePayoutInfoForBillingProfile(PayoutInfo.builder()
                .aptosAddress(new AptosAccountAddress("0x657Dd41d9BBfe65CbE9f6224D48405b7CAd283Ea"))
                .ethWallet(new WalletLocator(new Name("ilysse.eth")))
                .optimismAddress(new EvmAccountAddress("0x657Dd41d9BBfe65CbE9f6224D48405b7CAd283Ea"))
                .starknetAddress(new StarknetAccountAddress("0x049d36570d4e46f48e99674bd3fcc84644ddd6b96f7c741b1562b82f9e004dc7"))
                .bankAccount(new BankAccount(faker.lorem().sentence(), faker.lorem().sentence()))
                .build(), billingProfileId);
        final ProjectId projectId = ProjectId.random();
        postgresBillingProfileAdapter.savePayoutPreference(billingProfileId, memberId, projectId);

        // When
        postgresBillingProfileAdapter.deleteBillingProfile(billingProfileId);

        // Then
        assertTrue(kybRepository.findById(kyb.getId()).isEmpty());
        assertTrue(payoutPreferenceRepository.findAll().stream()
                .filter(payoutPreferenceEntity -> payoutPreferenceEntity.getBillingProfileId().equals(billingProfileId.value()))
                .findAny().isEmpty());
        assertTrue(walletRepository.findAll().stream()
                .filter(walletEntity -> walletEntity.getBillingProfileId().equals(billingProfileId.value()))
                .findFirst().isEmpty());
        assertTrue(childrenKycRepository.findAll().stream()
                .filter(childrenKycEntity -> childrenKycEntity.getParentApplicantId().equals(kybApplicantId))
                .findAny().isEmpty());
        assertFalse(billingProfileUserRepository.findAll().stream()
                .anyMatch(billingProfileUserEntity -> billingProfileUserEntity.getBillingProfileId().equals(billingProfileId.value())));
        assertTrue(bankAccountRepository.findById(billingProfileId.value()).isEmpty());
    }
}
