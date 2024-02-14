package onlydust.com.marketplace.api.domain.view.backoffice;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
@EqualsAndHashCode
public class UserView {
    UUID id;
    Boolean isCompany;
    String companyName;
    String companyNum;
    String firstname;
    String lastname;
    String address;
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
