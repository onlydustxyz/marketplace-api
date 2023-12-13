package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import onlydust.com.marketplace.api.domain.model.Currency;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class UserView {
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
    String linkedIn;
    String whatsApp;
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
    Boolean lookingForAJob;
    String weeklyAllocatedTime;
    List<String> languages;
    String tcAcceptedAt;
    ZonedDateTime onboardingCompletedAt;

    @Value
    @Builder
    @EqualsAndHashCode
    public static class Filters {
        @Builder.Default
        List<UUID> users = List.of();
    }
}
