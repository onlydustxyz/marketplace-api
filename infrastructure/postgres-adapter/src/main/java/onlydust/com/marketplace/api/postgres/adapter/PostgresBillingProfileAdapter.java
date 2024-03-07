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
import onlydust.com.marketplace.api.postgres.adapter.entity.write.*;
import onlydust.com.marketplace.api.postgres.adapter.repository.*;
import onlydust.com.marketplace.kernel.pagination.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;

import static java.time.ZonedDateTime.now;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.notFound;

@AllArgsConstructor
public class PostgresBillingProfileAdapter implements BillingProfileStoragePort {
    private final @NonNull GlobalSettingsRepository globalSettingsRepository;
    private final @NonNull BillingProfileRepository billingProfileRepository;
    private final @NonNull KybRepository kybRepository;
    private final @NonNull KycRepository kycRepository;
    private final @NonNull PayoutInfoRepository payoutInfoRepository;
    private final @NonNull WalletRepository walletRepository;
    private final @NonNull BillingProfileUserRepository billingProfileUserRepository;
    private final @NonNull BillingProfileUserViewRepository billingProfileUserViewRepository;
    private final @NonNull ChildrenKycRepository childrenKycRepository;
    private final @NonNull BillingProfileUserInvitationRepository billingProfileUserInvitationRepository;
    private final @NonNull PayoutPreferenceRepository payoutPreferenceRepository;


    @Override
    @Transactional
    public void updateInvoiceMandateAcceptanceDate(@NonNull final BillingProfile.Id billingProfileId, @NonNull final ZonedDateTime acceptanceDate) {
        final var billingProfile = billingProfileRepository.findById(billingProfileId.value())
                .orElseThrow(() -> notFound("Billing profile %s not found".formatted(billingProfileId)));
        billingProfile.setInvoiceMandateAcceptedAt(Date.from(acceptanceDate.toInstant()));
        billingProfileRepository.save(billingProfile);
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
        final var invoiceMandateLatestVersionDate = globalSettingsRepository.get().getInvoiceMandateLatestVersionDate();
        return billingProfileRepository.findBillingProfilesForUserId(userId.value())
                .stream()
                .map(BillingProfileEntity::toView)
                .peek(bp -> bp.setInvoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate))
                .toList();
    }

    @Override
    @Transactional
    public void save(IndividualBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KycEntity> optionalKycEntity = kycRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKycEntity.isEmpty()) {
            kycRepository.save(KycEntity.fromDomain(billingProfile.kyc()));
        }
    }

    @Override
    @Transactional
    public void save(SelfEmployedBillingProfile billingProfile) {
        billingProfileRepository.save(BillingProfileEntity.fromDomain(billingProfile, billingProfile.owner().id(), now()));
        final Optional<KybEntity> optionalKybEntity = kybRepository.findByBillingProfileId(billingProfile.id().value());
        if (optionalKybEntity.isEmpty()) {
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb()));
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
            kybRepository.save(KybEntity.fromDomain(billingProfile.kyb()));
        }
    }

    @Override
    public void savePayoutPreference(BillingProfile.Id billingProfileId, UserId userId, ProjectId projectId) {
        payoutPreferenceRepository.save(PayoutPreferenceEntity.builder()
                .billingProfileId(billingProfileId.value())
                .id(PayoutPreferenceEntity.PrimaryKey.builder()
                        .userId(userId.value())
                        .projectId(projectId.value())
                        .build())
                .build());
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
        final var invoiceMandateLatestVersionDate = globalSettingsRepository.get().getInvoiceMandateLatestVersionDate();

        return billingProfileRepository.findById(billingProfileId.value()).map(billingProfileEntity -> switch (billingProfileEntity.getType()) {
            case INDIVIDUAL -> {
                BillingProfileView billingProfileView = BillingProfileView.builder()
                        .type(BillingProfile.Type.INDIVIDUAL)
                        .id(billingProfileId)
                        .name(billingProfileEntity.getName())
                        .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
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
                        .invoiceMandateAcceptedAt(billingProfileEntity.getInvoiceMandateAcceptedAt())
                        .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate)
                        .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
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
                        .invoiceMandateAcceptedAt(billingProfileEntity.getInvoiceMandateAcceptedAt())
                        .invoiceMandateLatestVersionDate(invoiceMandateLatestVersionDate)
                        .payoutInfo(isNull(billingProfileEntity.getPayoutInfo()) ? null : billingProfileEntity.getPayoutInfo().toDomain())
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
    @Transactional(readOnly = true)
    public Optional<Kyc> findKycById(UUID id) {
        return kycRepository.findById(id).map(KycEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveKyc(Kyc kyc) {
        kycRepository.save(KycEntity.fromDomain(kyc));
    }

    @Override
    @Transactional
    public void updateBillingProfileStatus(BillingProfile.Id billingProfileId, VerificationStatus status) {
        billingProfileRepository.updateBillingProfileVerificationStatus(billingProfileId.value(), VerificationStatusEntity.fromDomain(status).name());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kyb> findKybById(UUID id) {
        return kybRepository.findById(id).map(KybEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveKyb(Kyb kyb) {
        kybRepository.save(KybEntity.fromDomain(kyb));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationStatus> findAllChildrenKycStatuesFromParentKyb(Kyb parentKyb) {
        return childrenKycRepository.findAllByParentApplicantId(parentKyb.getExternalApplicantId()).stream()
                .map(ChildrenKycEntity::getVerificationStatus)
                .map(VerificationStatusEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Kyb> findKybByParentExternalId(String parentExternalApplicantId) {
        return kybRepository.findByApplicantId(parentExternalApplicantId).map(KybEntity::toDomain);
    }

    @Override
    @Transactional
    public void saveChildrenKyc(String externalApplicantId, String parentExternalApplicantId, VerificationStatus verificationStatus) {
        childrenKycRepository.save(ChildrenKycEntity.builder()
                .applicantId(externalApplicantId)
                .parentApplicantId(parentExternalApplicantId)
                .verificationStatus(VerificationStatusEntity.fromDomain(verificationStatus))
                .build());
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
    public void deleteCoworkerInvitation(BillingProfile.Id billingProfileId, GithubUserId invitedUser) {
        billingProfileUserInvitationRepository.deleteById(new BillingProfileUserInvitationEntity.PrimaryKey(billingProfileId.value(), invitedUser.value()));
    }

    @Override
    public void saveCoworker(BillingProfile.Id billingProfileId, UserId invitedUser, BillingProfile.User.Role role, ZonedDateTime acceptedAt) {
        billingProfileUserRepository.save(BillingProfileUserEntity.builder()
                .billingProfileId(billingProfileId.value())
                .userId(invitedUser.value())
                .role(BillingProfileUserEntity.Role.fromDomain(role))
                .joinedAt(Date.from(acceptedAt.toInstant()))
                .build());
    }

    @Override
    public Optional<BillingProfileCoworkerView> getInvitedCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findInvitedUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserViewEntity::toView);
    }

    @Override
    public Optional<BillingProfileCoworkerView> getCoworker(BillingProfile.Id billingProfileId, GithubUserId invitedGithubUserId) {
        return billingProfileUserViewRepository.findUserByBillingProfileIdAndGithubId(billingProfileId.value(), invitedGithubUserId.value())
                .map(BillingProfileUserViewEntity::toView);
    }

    @Override
    public void deleteCoworker(BillingProfile.Id billingProfileId, UserId userId) {
        billingProfileUserRepository.deleteById(new BillingProfileUserEntity.PrimaryKey(userId.value(), billingProfileId.value()));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillingProfileAdminView> findBillingProfileAdminForInvoice(Invoice.Id invoiceId) {
//        return oldBillingProfileAdminViewRepository.findByInvoiceId(invoiceId.value())
//                .map(OldBillingProfileAdminViewEntity::toDomain);
        //TODO
        return Optional.empty();
    }
}
