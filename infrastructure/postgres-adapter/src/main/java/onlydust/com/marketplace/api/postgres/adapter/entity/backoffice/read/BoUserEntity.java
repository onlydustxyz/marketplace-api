package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.project.domain.view.backoffice.UserView;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@Entity
public class BoUserEntity {
    @Id
    UUID id;
    String companyName;
    String companyNum;
    Boolean isCompany;
    String firstname;
    String lastname;
    String address;
    String country;
    String telegram;
    String twitter;
    String discord;
    String linkedin;
    String whatsapp;
    String bic;
    String iban;
    String ens;
    String ethAddress;
    String optimismAddress;
    String starknetAddress;
    String aptosAddress;
    ZonedDateTime createdAt;
    ZonedDateTime updatedAt;
    ZonedDateTime lastSeenAt;
    String email;
    Long githubUserId;
    String githubLogin;
    String githubHtmlUrl;
    String githubAvatarUrl;
    String bio;
    String location;
    String website;
    @Column(name = "looking_for_a_job")
    Boolean lookingForAJob;
    String weeklyAllocatedTime;
    @JdbcTypeCode(SqlTypes.JSON)
    List<String> languages;
    String tcAcceptedAt;
    ZonedDateTime onboardingCompletedAt;
    Boolean usEntity;
    String verificationStatus;

    public UserView toView() {
        return UserView.builder()
                .id(id)
                .isCompany(isCompany)
                .companyName(companyName)
                .companyNum(companyNum)
                .firstname(firstname)
                .lastname(lastname)
                .address(address)
                .country(country)
                .telegram(telegram)
                .twitter(twitter)
                .discord(discord)
                .linkedIn(linkedin)
                .whatsApp(whatsapp)
                .bic(bic)
                .iban(iban)
                .ens(ens)
                .ethAddress(ethAddress)
                .optimismAddress(optimismAddress)
                .starknetAddress(starknetAddress)
                .aptosAddress(aptosAddress)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .lastSeenAt(lastSeenAt)
                .email(email)
                .githubUserId(githubUserId)
                .githubLogin(githubLogin)
                .githubHtmlUrl(githubHtmlUrl)
                .githubAvatarUrl(githubAvatarUrl)
                .bio(bio)
                .location(location)
                .website(website)
                .lookingForAJob(lookingForAJob)
                .weeklyAllocatedTime(weeklyAllocatedTime)
                .languages(languages)
                .tcAcceptedAt(tcAcceptedAt)
                .onboardingCompletedAt(onboardingCompletedAt)
                .usEntity(usEntity)
                .verificationStatus(verificationStatus)
                .build();
    }
}
