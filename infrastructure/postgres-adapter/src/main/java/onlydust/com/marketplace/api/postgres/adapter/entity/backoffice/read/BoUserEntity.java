package onlydust.com.marketplace.api.postgres.adapter.entity.backoffice.read;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import onlydust.com.marketplace.api.domain.view.backoffice.UserView;

import javax.persistence.*;
import java.time.ZonedDateTime;
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
    String companyFirstname;
    String companyLastname;
    String personFirstname;
    String personLastname;
    String address;
    String postCode;
    String city;
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
    String languages;
    String tcAcceptedAt;
    ZonedDateTime onboardingCompletedAt;

    public UserView toView() {
        return UserView.builder()
                .id(id)
                .companyName(companyName)
                .companyNum(companyNum)
                .companyFirstname(companyFirstname)
                .companyLastname(companyLastname)
                .personFirstname(personFirstname)
                .personLastname(personLastname)
                .address(address)
                .postCode(postCode)
                .city(city)
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
                .build();
    }
}
