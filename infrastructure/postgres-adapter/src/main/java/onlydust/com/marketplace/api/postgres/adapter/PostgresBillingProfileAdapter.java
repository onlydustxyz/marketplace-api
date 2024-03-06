package onlydust.com.marketplace.api.postgres.adapter;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.ProjectId;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.model.user.GithubUserId;
import onlydust.com.marketplace.accounting.domain.model.user.UserId;
import onlydust.com.marketplace.accounting.domain.port.out.BillingProfileStoragePort;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileAdminView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.BillingProfileUserViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.read.OldBillingProfileAdminViewEntity;
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.old.OldBillingProfileAdminViewRepository;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.ZonedDateTime.now;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {
    private final @NonNull CompanyBillingProfileRepository companyBillingProfileRepository;
    private final @NonNull IndividualBillingProfileRepository individualBillingProfileRepository;
    private final @NonNull GlobalSettingsRepository globalSettingsRepository;
    private final @NonNull BillingProfileRepository billingProfileRepository;
    private final @NonNull KybRepository kybRepository;
    private final @NonNull KycRepository kycRepository;
    private final @NonNull PayoutInfoRepository payoutInfoRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BillingProfileUserRepository billingProfileUserRepository;
    private final @NonNull BillingProfileUserViewRepository billingProfileUserViewRepository;
    private final @NonNull BillingProfileUserInvitationRepository billingProfileUserInvitationRepository;
    private final @NonNull OldBillingProfileAdminViewRepository oldBillingProfileAdminViewRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean oldIsAdmin(UserId userId, BillingProfile.Id billingProfileId) {
        final var admin = companyBillingProfileRepository.findById(billingProfileId.value()).map(CompanyBillingProfileEntity::getUserId)
                .or(() -> individualBillingProfileRepository.findById(billingProfileId.value()).map(IndividualBillingProfileEntity::getUserId))
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        return admin.equals(userId.value());
    }

    @Override
    @Transactional
    public void updateInvoiceMandateAcceptanceDate(@NonNull final BillingProfile.Id billingProfileId, @NonNull final ZonedDateTime acceptanceDate) {
        final var billingProfile = companyBillingProfileRepository.findById(billingProfileId.value())
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        billingProfile.setInvoiceMandateAcceptedAt(Date.from(acceptanceDate.toInstant()));
        companyBillingProfileRepository.save(billingProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortBillingProfileView> findIndividualBillingProfileForUser(UserId ownerId) {
        return billingProfileRepository.findIndividualProfilesForUserId(ownerId.value())
                .stream().map(BillingProfileEntity::toView).findFirst();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShortBillingProfileView> findAllBillingProfilesForUser(UserId userId) {
        return billingProfileRepository.findBillingProfilesForUserId(userId.value())
                .stream().map(BillingProfileEntity::toView).toList();
    }

    @Override
    @Transactional
    public void save(IndividualBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKycEntity.isEmpty()) {
            kycRepository.save(KycEntity.fromDomain(billingProfile.kyc(), billingProfile.id()));
        }
    }

    @Override
    @Transactional
    public void save(SelfEmployedBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb(), billingProfile.id()));
        }
    }

    @Override
    @Transactional
    public void save(CompanyBillingProfile billingProfile) {
        // TODO : manage add/remove members -> waiting to start the feature on coworkers
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile,
                billingProfile.members().stream().map(BillingProfile.User::id).toList().get(0), now()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb(), billingProfile.id()));
        }
    }

    @Override
    public void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {

    }

    @Override
    public boolean isMandateAccepted(BillingProfile.Id billingProfileId) {
        if (individualBillingProfileRepository.findById(billingProfileId.value()).isPresent()) return true;

        final var billingProfile = companyBillingProfileRepository.findById(billingProfileId.value())
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));

        return billingProfile.toDomain(globalSettingsRepository.get().getInvoiceMandateLatestVersionDate())
                .isInvoiceMandateAccepted();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserMemberOf(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileRepository.findBillingProfilesForUserId(userId.value()).stream()
                .anyMatch(billingProfileEntity -> billingProfileEntity.getId().equals(billingProfileId.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdmin(BillingProfile.Id billingProfileId, UserId userId) {
        return billingProfileUserRepository.findByBillingProfileIdAndUserId(billingProfileId.value(), userId.value())
                .map(billingProfileUserEntity -> billingProfileUserEntity.getRole().equals(BillingProfileUserEntity.Role.ADMIN))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileView> findById(BillingProfile.Id billingProfileId) {
        return billingProfileRepository.findById(billingProfileId.value()).map(billingProfileEntity -> switch (billingProfileEntity.getType()) {
            case INDIVIDUAL -> {
                BillingProfileView billingProfileView = BillingProfileView.builder().type(BillingProfile.Type.INDIVIDUAL)
                        .id(billingProfileId)
                        .name(billingProfileEntity.getName())
                        .build();
                final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfileId.value());
                if (optionalKycEntity.isPresent()) {
                    billingProfileView = billingProfileView.toBuilder()
                            .kyc(optionalKycEntity.get().toDomain())
                            .build();
                }
                yield billingProfileView;
            }
            case COMPANY -> {
                BillingProfileView billingProfileView = BillingProfileView.builder().type(BillingProfile.Type.COMPANY)
                        .id(billingProfileId)
                        .name(billingProfileEntity.getName())
                        .build();
                final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfileId.value());
                if (optionalKybEntity.isPresent()) {
                    billingProfileView = billingProfileView.toBuilder()
                            .kyb(optionalKybEntity.get().toDomain())
                            .build();
                }
                yield billingProfileView;
            }
            case SELF_EMPLOYED -> {
                BillingProfileView billingProfileView = BillingProfileView.builder().type(BillingProfile.Type.SELF_EMPLOYED)
                        .id(billingProfileId)
                        .name(billingProfileEntity.getName())
                        .build();
                final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfileId.value());
                if (optionalKybEntity.isPresent()) {
                    billingProfileView = billingProfileView.toBuilder()
                            .kyb(optionalKybEntity.get().toDomain())
                            .build();
                }
                yield billingProfileView;
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PayoutInfo> findPayoutInfoByBillingProfile(BillingProfile.Id billingProfileId) {
        return payoutInfoRepository.findById(billingProfileId.value()).map(PayoutInfoEntity::toDomain);
    }

    @Override
    @Transactional
    public void savePayoutInfoForBillingProfile(PayoutInfo payoutInfo, BillingProfile.Id billingProfileId) {
        payoutInfoRepository.findById(billingProfileId.value())
                .ifPresent(payoutInfoEntity -> walletRepository.deleteByBillingProfileId(billingProfileId.value()));
        payoutInfoRepository.save(PayoutInfoEntity.toEntity(billingProfileId, payoutInfo));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BillingProfileCoworkerView> findCoworkersByBillingProfile(@NonNull BillingProfile.Id billingProfileId,
                                                                          @NonNull Set<BillingProfile.User.Role> roles,
                                                                          int pageIndex, int pageSize) {
        final var page = billingProfileUserViewRepository.findByBillingProfileId(billingProfileId.value(),
                roles.stream().map(BillingProfileUserEntity.Role::fromDomain).map(Enum::toString).toList(),
                PageRequest.of(pageIndex, pageSize, Sort.by("user_id")));
        return Page.<BillingProfileCoworkerView>builder()
                .content(page.getContent().stream().map(BillingProfileUserViewEntity::toView).toList())
                .totalItemNumber(page.getNumberOfElements())
                .totalPageNumber(page.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public void saveCoworkerInvitation(BillingProfile.Id billingProfileId, UserId invitedBy, GithubUserId invitedUser, BillingProfile.User.Role role,
                                       ZonedDateTime invitedAt) {
        billingProfileUserInvitationRepository.save(BillingProfileUserInvitationEntity.builder()
                .billingProfileId(billingProfileId.value())
                .invitedBy(invitedBy.value())
                .githubUserId(invitedUser.value())
                .invitedAt(Date.from(invitedAt.toInstant()))
                .role(BillingProfileUserEntity.Role.fromDomain(role))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileAdminView> findBillingProfileAdminForInvoice(Invoice.Id invoiceId) {
        return oldBillingProfileAdminViewRepository.findByInvoiceId(invoiceId.value())
                .map(OldBillingProfileAdminViewEntity::toDomain);
    }
}
