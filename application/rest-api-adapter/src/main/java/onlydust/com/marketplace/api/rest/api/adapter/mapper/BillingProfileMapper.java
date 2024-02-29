package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import onlydust.com.marketplace.accounting.domain.model.Currency;
import onlydust.com.marketplace.accounting.domain.model.Invoice;
import onlydust.com.marketplace.accounting.domain.model.Money;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.*;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileCoworkerView;
import onlydust.com.marketplace.accounting.domain.view.BillingProfileView;
import onlydust.com.marketplace.accounting.domain.view.ShortBillingProfileView;
import onlydust.com.marketplace.api.contract.model.VerificationStatus;
import onlydust.com.marketplace.api.contract.model.*;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.pagination.Page;
import onlydust.com.marketplace.kernel.pagination.PaginationHelper;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Objects.isNull;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.hasMore;
import static onlydust.com.marketplace.kernel.pagination.PaginationHelper.nextPageIndex;

public interface BillingProfileMapper {

    static BillingProfileResponse billingProfileToResponse(final BillingProfile billingProfile) {
        final BillingProfileResponse billingProfileResponse = new BillingProfileResponse();
        if (billingProfile instanceof IndividualBillingProfile individualBillingProfile) {
            return individualBillingProfileToResponse(individualBillingProfile, billingProfileResponse);
        } else if (billingProfile instanceof CompanyBillingProfile companyBillingProfile) {
            billingProfileResponse.setType(BillingProfileType.COMPANY);
            billingProfileResponse.setKyb(kybToResponse(companyBillingProfile.kyb()));
            billingProfileResponse.setId(companyBillingProfile.id().value());
            billingProfileResponse.setName(companyBillingProfile.name());
            billingProfileResponse.setIsSwitchableToSelfEmployed(companyBillingProfile.isSwitchableToSelfEmployed());
            billingProfileResponse.setStatus(verificationStatusToResponse(companyBillingProfile.status()));
            return billingProfileResponse;
        } else if (billingProfile instanceof SelfEmployedBillingProfile selfEmployedBillingProfile) {
            billingProfileResponse.setType(BillingProfileType.SELF_EMPLOYED);
            billingProfileResponse.setKyb(kybToResponse(selfEmployedBillingProfile.kyb()));
            billingProfileResponse.setId(selfEmployedBillingProfile.id().value());
            billingProfileResponse.setName(selfEmployedBillingProfile.name());
            billingProfileResponse.setIsSwitchableToSelfEmployed(selfEmployedBillingProfile.isSwitchableToCompany());
            billingProfileResponse.setStatus(verificationStatusToResponse(selfEmployedBillingProfile.status()));
            return billingProfileResponse;
        } else {
            throw OnlyDustException.internalServerError("Failed to cast billing profile to billing profile type");
        }
    }


    private static @NotNull BillingProfileResponse individualBillingProfileToResponse(IndividualBillingProfile individualBillingProfile,
                                                                                      BillingProfileResponse billingProfileResponse) {
        billingProfileResponse.setId(individualBillingProfile.id().value());
        billingProfileResponse.setName(individualBillingProfile.name());
        billingProfileResponse.setType(BillingProfileType.INDIVIDUAL);
        billingProfileResponse.setKyc(kycToResponse(individualBillingProfile.kyc()));
        billingProfileResponse.setCurrentYearPaymentAmount(individualBillingProfile.currentYearPaymentAmount().getValue());
        billingProfileResponse.setCurrentYearPaymentLimit(individualBillingProfile.currentYearPaymentLimit().getValue());
        billingProfileResponse.setIsSwitchableToSelfEmployed(false);
        billingProfileResponse.setStatus(verificationStatusToResponse(individualBillingProfile.status()));
        return billingProfileResponse;
    }

    private static @NotNull KYCResponse kycToResponse(final Kyc kyc) {
        final KYCResponse response = new KYCResponse();
        if (isNull(kyc)) {
            return response;
        }
        response.address(kyc.getAddress());
        response.birthdate(DateMapper.toZoneDateTime(kyc.getBirthdate()));
        response.country(isNull(kyc.getCountry()) ? null : kyc.getCountry().display().orElse(null));
        response.setFirstName(kyc.getFirstName());
        response.setLastName(kyc.getLastName());
        response.setIdDocumentCountryCode(kyc.getIdDocumentCountryCode());
        response.setIdDocumentNumber(kyc.getIdDocumentNumber());
        response.setIdDocumentType(isNull(kyc.getIdDocumentType()) ? null : switch (kyc.getIdDocumentType()) {
            case ID_CARD -> KYCResponse.IdDocumentTypeEnum.ID_CARD;
            case PASSPORT -> KYCResponse.IdDocumentTypeEnum.PASSPORT;
            case DRIVER_LICENSE -> KYCResponse.IdDocumentTypeEnum.DRIVER_LICENSE;
            case RESIDENCE_PERMIT -> KYCResponse.IdDocumentTypeEnum.RESIDENCE_PERMIT;
        });
        response.setId(kyc.getId());
        response.setUsCitizen(kyc.getUsCitizen());
        response.setValidUntil(DateMapper.toZoneDateTime(kyc.getValidUntil()));
        return response;
    }

    static @NotNull KYBResponse kybToResponse(final Kyb kyb) {
        final KYBResponse response = new KYBResponse();
        if (isNull(kyb)) {
            return response;
        }
        response.setAddress(kyb.getAddress());
        response.setCountry(isNull(kyb.getCountry()) ? null : kyb.getCountry().display().orElse(null));
        response.setName(kyb.getName());
        response.setId(kyb.getId());
        response.setEuVATNumber(kyb.getEuVATNumber());
        response.setRegistrationDate(DateMapper.toZoneDateTime(kyb.getRegistrationDate()));
        response.setRegistrationNumber(kyb.getRegistrationNumber());
        response.setSubjectToEuropeVAT(kyb.getSubjectToEuropeVAT());
        response.setUsEntity(kyb.getUsEntity());
        return response;
    }

    static VerificationStatus verificationStatusToResponse(final onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus verificationStatus) {
        return isNull(verificationStatus) ? null :
                switch (verificationStatus) {
                    case CLOSED -> VerificationStatus.CLOSED;
                    case REJECTED -> VerificationStatus.REJECTED;
                    case STARTED -> VerificationStatus.STARTED;
                    case UNDER_REVIEW -> VerificationStatus.UNDER_REVIEW;
                    case NOT_STARTED -> VerificationStatus.NOT_STARTED;
                    case VERIFIED -> VerificationStatus.VERIFIED;
                };
    }

    static InvoicePreviewResponse map(Invoice preview) {
        return new InvoicePreviewResponse()
                .id(preview.id().value())
                .number(preview.number().value())
                .createdAt(preview.createdAt())
                .dueAt(preview.dueAt())
                .billingProfileType(map(preview.billingProfileType()))
                .individualBillingProfile(preview.personalInfo().map(BillingProfileMapper::map).orElse(null))
                .companyBillingProfile(preview.companyInfo().map(BillingProfileMapper::map).orElse(null))
                .destinationAccounts(new DestinationAccountResponse()
                        .bankAccount(preview.bankAccount().map(BillingProfileMapper::map).orElse(null))
                        .wallets(preview.wallets().stream().map(BillingProfileMapper::map).toList())
                )
                .rewards(preview.rewards().stream().map(BillingProfileMapper::map).toList())
                .totalBeforeTax(map(preview.totalBeforeTax()))
                .taxRate(preview.taxRate())
                .totalTax(map(preview.totalTax()))
                .totalAfterTax(map(preview.totalAfterTax()))
                ;
    }

    static InvoiceRewardItemResponse map(Invoice.Reward reward) {
        return new InvoiceRewardItemResponse()
                .id(reward.id().value())
                .date(reward.createdAt())
                .projectName(reward.projectName())
                .amount(toConvertibleMoney(reward.amount(), reward.target()))
                ;
    }

    static Invoice.Sort map(String sort) {
        return switch (sort) {
            case "INVOICE_NUMBER" -> Invoice.Sort.NUMBER;
            case "CREATED_AT" -> Invoice.Sort.CREATED_AT;
            case "AMOUNT" -> Invoice.Sort.AMOUNT;
            case "STATUS" -> Invoice.Sort.STATUS;
            default -> throw badRequest("Invalid sort value: %s".formatted(sort));
        };
    }

    static WalletResponse map(Invoice.Wallet wallet) {
        return new WalletResponse()
                .network(wallet.network())
                .address(wallet.address());
    }

    static BankAccountResponse map(Invoice.BankAccount bankAccount) {
        return new BankAccountResponse()
                .bic(bankAccount.bic())
                .accountNumber(bankAccount.accountNumber());
    }

    static InvoicePreviewResponseIndividualBillingProfile map(Invoice.PersonalInfo personalInfo) {
        return new InvoicePreviewResponseIndividualBillingProfile()
                .firstName(personalInfo.firstName())
                .lastName(personalInfo.lastName())
                .address(personalInfo.address())
                .countryCode(personalInfo.countryCode())
                .country(personalInfo.countryName())
                ;
    }

    static InvoicePreviewResponseCompanyBillingProfile map(Invoice.CompanyInfo companyInfo) {
        return new InvoicePreviewResponseCompanyBillingProfile()
                .name(companyInfo.name())
                .address(companyInfo.address())
                .registrationNumber(companyInfo.registrationNumber())
                .vatRegulationState(map(companyInfo.vatRegulationState()))
                .euVATNumber(companyInfo.euVATNumber())
                .countryCode(companyInfo.countryCode())
                .country(companyInfo.countryName())
                ;
    }

    static VatRegulationState map(Invoice.VatRegulationState vatRegulationState) {
        return switch (vatRegulationState) {
            case APPLICABLE -> VatRegulationState.APPLICABLE;
            case REVERSE_CHARGE -> VatRegulationState.REVERSE_CHARGE;
            case NOT_APPLICABLE_NON_UE -> VatRegulationState.NOT_APPLICABLE_NON_UE;
            case NOT_APPLICABLE_FRENCH_NOT_SUBJECT -> VatRegulationState.NOT_APPLICABLE_FRENCH_NOT_SUBJECT;
        };
    }

    static NewMoney map(Money money) {
        return new NewMoney()
                .amount(money.getValue())
                .currency(map(money.getCurrency()));
    }

    static CurrencyContract map(Currency currency) {
        return CurrencyContract.fromValue(currency.code().toString());
    }

    static InvoiceStatus map(Invoice.Status status) {
        return switch (status) {
            case DRAFT -> InvoiceStatus.DRAFT;
            case TO_REVIEW, APPROVED -> InvoiceStatus.PROCESSING;
            case PAID -> InvoiceStatus.COMPLETE;
            case REJECTED -> InvoiceStatus.REJECTED;
        };
    }

    static ConvertibleMoney toConvertibleMoney(Money money, Money base) {
        return new ConvertibleMoney()
                .amount(money.getValue())
                .currency(map(money.getCurrency()))
                .target(new BaseMoney()
                        .amount(base.getValue())
                        .currency(map(base.getCurrency()))
                        .conversionRate(base.getValue().divide(money.getValue(), 2, RoundingMode.HALF_EVEN))
                );
    }

    static BillingProfileType map(BillingProfile.Type type) {
        return switch (type) {
            case COMPANY -> BillingProfileType.COMPANY;
            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
        };
    }


    static BillingProfileInvoicesPageResponse map(Page<Invoice> page, Integer pageIndex) {
        return new BillingProfileInvoicesPageResponse()
                .invoices(page.getContent().stream().map(BillingProfileMapper::mapToBillingProfileInvoicesPageItemResponse).toList())
                .hasMore(PaginationHelper.hasMore(pageIndex, page.getTotalPageNumber()))
                .totalPageNumber(page.getTotalPageNumber())
                .totalItemNumber(page.getTotalItemNumber())
                .nextPageIndex(PaginationHelper.nextPageIndex(pageIndex, page.getTotalPageNumber()));
    }

    static BillingProfileInvoicesPageItemResponse mapToBillingProfileInvoicesPageItemResponse(Invoice invoice) {
        return new BillingProfileInvoicesPageItemResponse()
                .id(invoice.id().value())
                .number(invoice.number().value())
                .createdAt(invoice.createdAt())
                .totalAfterTax(map(invoice.totalAfterTax()))
                .status(map(invoice.status()));
    }

    static MyBillingProfilesResponse myBillingProfileToResponse(final List<ShortBillingProfileView> shortBillingProfileViews) {
        final MyBillingProfilesResponse myBillingProfilesResponse = new MyBillingProfilesResponse();
        myBillingProfilesResponse.setBillingProfiles(isNull(shortBillingProfileViews) ? List.of() : shortBillingProfileViews.stream()
                .map(view -> new ShortBillingProfileResponse()
                        .name(view.getName())
                        .id(view.getId().value())
                        .invoiceMandateAccepted(view.isInvoiceMandateAccepted())
                        .type(switch (view.getType()) {
                            case INDIVIDUAL -> BillingProfileType.INDIVIDUAL;
                            case COMPANY -> BillingProfileType.COMPANY;
                            case SELF_EMPLOYED -> BillingProfileType.SELF_EMPLOYED;
                        })).collect(Collectors.toList()));
        return myBillingProfilesResponse;

    }

    static BillingProfileResponse billingProfileViewToResponse(BillingProfileView billingProfileView) {
        final BillingProfileResponse billingProfileResponse = new BillingProfileResponse();
        billingProfileResponse.setId(billingProfileView.getId().value());
        billingProfileResponse.setName(billingProfileView.getName());
        billingProfileResponse.setType(map(billingProfileView.getType()));
        billingProfileResponse.setKyb(isNull(billingProfileView.getKyb()) ? null : kybToResponse(billingProfileView.getKyb()));
        billingProfileResponse.setKyc(isNull(billingProfileView.getKyc()) ? null : kycToResponse(billingProfileView.getKyc()));
        billingProfileResponse.setStatus(verificationStatusToResponse(billingProfileView.getVerificationStatus()));
        return billingProfileResponse;
    }

    static BillingProfileCoworkersPageResponse coworkersPageToResponse(Page<BillingProfileCoworkerView> coworkersPage, int pageIndex) {
        return new BillingProfileCoworkersPageResponse()
                .coworkers(coworkersPage.getContent().stream()
                        .map(BillingProfileMapper::coworkerToResponse)
                        .sorted(Comparator.comparing(BillingProfileCoworkersPageItemResponse::getJoinedAt, nullsLast(naturalOrder()))
                                .thenComparing(BillingProfileCoworkersPageItemResponse::getInvitedAt, nullsLast(naturalOrder())))
                        .toList())
                .totalPageNumber(coworkersPage.getTotalPageNumber())
                .totalItemNumber(coworkersPage.getTotalItemNumber())
                .hasMore(hasMore(pageIndex, coworkersPage.getTotalPageNumber()))
                .nextPageIndex(nextPageIndex(pageIndex, coworkersPage.getTotalPageNumber()));
    }

    static BillingProfileCoworkersPageItemResponse coworkerToResponse(BillingProfileCoworkerView billingProfileCoworkerView) {
        return new BillingProfileCoworkersPageItemResponse()
                .githubUserId(billingProfileCoworkerView.githubUserId() != null ? billingProfileCoworkerView.githubUserId().value() : null)
                .login(billingProfileCoworkerView.login())
                .avatarUrl(billingProfileCoworkerView.avatarUrl())
                .htmlUrl(billingProfileCoworkerView.githubHtmlUrl())
                .isRegistered(billingProfileCoworkerView.userId() != null)
                .id(billingProfileCoworkerView.userId() != null ? billingProfileCoworkerView.userId().value() : null)
                .role(switch (billingProfileCoworkerView.role()) {
                    case ADMIN -> BillingProfileCoworkerRole.ADMIN;
                    case MEMBER -> BillingProfileCoworkerRole.MEMBER;
                })
                .removable(billingProfileCoworkerView.removable())
                .invitedAt(billingProfileCoworkerView.invitedAt())
                .joinedAt(billingProfileCoworkerView.joinedAt());

    }
}
