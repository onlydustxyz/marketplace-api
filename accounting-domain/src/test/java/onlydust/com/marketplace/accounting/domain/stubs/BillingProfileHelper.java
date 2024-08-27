package onlydust.com.marketplace.accounting.domain.stubs;

import onlydust.com.marketplace.accounting.domain.model.Country;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.BillingProfile;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyb;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.Kyc;
import onlydust.com.marketplace.accounting.domain.model.billingprofile.VerificationStatus;
import onlydust.com.marketplace.kernel.model.UserId;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public interface BillingProfileHelper {
    static void fillKyb(Kyb kyb) {
        kyb.setCountry(Country.fromIso3("FRA"));
        kyb.setRegistrationNumber("123456789");
        kyb.setEuVATNumber("FR123456789");
        kyb.setRegistrationDate(new Date());
        kyb.setAddress("1 rue de la paix");
        kyb.setName("OnlyDust SAS");
        kyb.setSubjectToEuropeVAT(true);
        kyb.setStatus(VerificationStatus.VERIFIED);
    }

    static void fillKyc(Kyc kyc) {
        kyc.setCountry(Country.fromIso3("FRA"));
        kyc.setAddress("1 rue de la paix");
        kyc.setStatus(VerificationStatus.VERIFIED);
        kyc.setFirstName("john");
        kyc.setLastName("doe");
    }

    static Kyc newKyc(BillingProfile.Id billingProfileId, UserId ownerId) {
        return newKyc(billingProfileId, ownerId, VerificationStatus.NOT_STARTED);
    }

    static Kyc newKyc(BillingProfile.Id billingProfileId, UserId ownerId, VerificationStatus status) {
        return Kyc.builder()
                .id(UUID.randomUUID())
                .status(status)
                .billingProfileId(billingProfileId)
                .ownerId(ownerId)
                .country(Country.fromIso3("FRA"))
                .firstName("John")
                .lastName("Doe")
                .address("1 rue de la paix")
                .birthdate(Date.from(ZonedDateTime.of(1990, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()))
                .consideredUsPersonQuestionnaire(false)
                .idDocumentCountry(Country.fromIso3("FRA"))
                .build();
    }

    static Kyb newKyb(BillingProfile.Id billingProfileId, UserId ownerId) {
        return newKyb(billingProfileId, ownerId, VerificationStatus.NOT_STARTED);
    }

    static Kyb newKyb(BillingProfile.Id billingProfileId, UserId ownerId, VerificationStatus status) {
        return Kyb.builder()
                .id(UUID.randomUUID())
                .status(status)
                .billingProfileId(billingProfileId)
                .name("OnlyDust SAS")
                .registrationNumber("123456789")
                .registrationDate(new Date())
                .address("1 rue de la paix")
                .country(Country.fromIso3("FRA"))
                .subjectToEuropeVAT(true)
                .usEntity(false)
                .ownerId(ownerId)
                .euVATNumber("FR123456789")
                .build();
    }
}
