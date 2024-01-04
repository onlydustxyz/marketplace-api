package onlydust.com.marketplace.api.rest.api.adapter.mapper;

import static java.util.Objects.isNull;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper.toZoneDateTime;
import static onlydust.com.marketplace.api.rest.api.adapter.mapper.ProjectBudgetMapper.mapCurrency;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import onlydust.com.marketplace.api.contract.model.AllocatedTime;
import onlydust.com.marketplace.api.contract.model.ContactInformation;
import onlydust.com.marketplace.api.contract.model.ContactInformationChannel;
import onlydust.com.marketplace.api.contract.model.GetMeResponse;
import onlydust.com.marketplace.api.contract.model.MyRewardAmountResponse;
import onlydust.com.marketplace.api.contract.model.PrivateUserProfileResponse;
import onlydust.com.marketplace.api.contract.model.ProjectLedShortResponse;
import onlydust.com.marketplace.api.contract.model.ProjectVisibility;
import onlydust.com.marketplace.api.contract.model.PublicUserProfileResponse;
import onlydust.com.marketplace.api.contract.model.RewardTotalAmountsResponse;
import onlydust.com.marketplace.api.contract.model.UserContributionStats;
import onlydust.com.marketplace.api.contract.model.UserProfileCoverColor;
import onlydust.com.marketplace.api.contract.model.UserProfileProjects;
import onlydust.com.marketplace.api.contract.model.UserProfileRequest;
import onlydust.com.marketplace.api.contract.model.UserProfileStats;
import onlydust.com.marketplace.api.domain.model.Contact;
import onlydust.com.marketplace.api.domain.model.User;
import onlydust.com.marketplace.api.domain.model.UserAllocatedTimeToContribute;
import onlydust.com.marketplace.api.domain.model.UserProfile;
import onlydust.com.marketplace.api.domain.model.UserProfileCover;
import onlydust.com.marketplace.api.domain.model.UserRole;
import onlydust.com.marketplace.api.domain.view.TotalEarnedPerCurrency;
import onlydust.com.marketplace.api.domain.view.TotalsEarned;
import onlydust.com.marketplace.api.domain.view.UserProfileView;
import org.springframework.beans.BeanUtils;

public interface UserMapper {

  static UserProfile userProfileRequestToDomain(final UserProfileRequest userProfileRequest) {
    return UserProfile.builder()
        .avatarUrl(userProfileRequest.getAvatarUrl())
        .bio(userProfileRequest.getBio())
        .website(userProfileRequest.getWebsite())
        .location(userProfileRequest.getLocation())
        .cover(userProfileRequest.getCover() == null ? null : coverToUserProfileDomain(userProfileRequest.getCover()))
        .contacts(contactToDomain(userProfileRequest.getContacts()))
        .technologies(userProfileRequest.getTechnologies())
        .allocatedTimeToContribute(allocatedTimeToDomain(userProfileRequest.getAllocatedTimeToContribute()))
        .isLookingForAJob(userProfileRequest.getIsLookingForAJob())
        .build();
  }

  static List<Contact> contactToDomain(List<ContactInformation> contacts) {
    return contacts.stream()
        .map(contactInformation -> {
          final Contact.Visibility visibility = switch (contactInformation.getVisibility()) {
            case PUBLIC -> Contact.Visibility.PUBLIC;
            case PRIVATE -> Contact.Visibility.PRIVATE;
          };
          final Contact.Channel channel = switch (contactInformation.getChannel()) {
            case EMAIL -> Contact.Channel.EMAIL;
            case LINKEDIN -> Contact.Channel.LINKEDIN;
            case TWITTER -> Contact.Channel.TWITTER;
            case TELEGRAM -> Contact.Channel.TELEGRAM;
            case DISCORD -> Contact.Channel.DISCORD;
            case WHATSAPP -> Contact.Channel.WHATSAPP;
          };
          return Contact.builder()
              .contact(contactInformation.getContact())
              .channel(channel)
              .visibility(visibility)
              .build();
        }).toList();
  }

  static UserProfileCover coverToUserProfileDomain(final @NonNull UserProfileCoverColor cover) {
    return switch (cover) {
      case BLUE -> UserProfileCover.BLUE;
      case CYAN -> UserProfileCover.CYAN;
      case MAGENTA -> UserProfileCover.MAGENTA;
      case YELLOW -> UserProfileCover.YELLOW;
    };
  }


  static PublicUserProfileResponse userProfileToPublicResponse(UserProfileView userProfileView) {
    final PrivateUserProfileResponse privateUserProfileResponse = userProfileToPrivateResponse(userProfileView);
    privateUserProfileResponse.setIsLookingForAJob(null);
    privateUserProfileResponse.setAllocatedTimeToContribute(null);
    privateUserProfileResponse.setContacts(
        privateUserProfileResponse.getContacts().stream().filter(contact -> contact.getVisibility() == ContactInformation.VisibilityEnum.PUBLIC)
            .collect(Collectors.toList())
    );
    privateUserProfileResponse.setProjects(userProjectsToResponse(userProfileView.getProjectsStats(), false));

    final PublicUserProfileResponse publicUserProfileResponse = new PublicUserProfileResponse();
    BeanUtils.copyProperties(privateUserProfileResponse, publicUserProfileResponse);
    return publicUserProfileResponse;
  }

  static PrivateUserProfileResponse userProfileToPrivateResponse(UserProfileView userProfileView) {
    final PrivateUserProfileResponse userProfileResponse = new PrivateUserProfileResponse();
    userProfileResponse.setGithubUserId(userProfileView.getGithubId());
    userProfileResponse.setId(userProfileView.getId());
    userProfileResponse.setLogin(userProfileView.getLogin());
    userProfileResponse.setAvatarUrl(userProfileView.getAvatarUrl());
    userProfileResponse.setBio(userProfileView.getBio());
    userProfileResponse.setWebsite(userProfileView.getWebsite());
    userProfileResponse.setHtmlUrl((isNull(userProfileView.getHtmlUrl()) ? null :
        URI.create(userProfileView.getHtmlUrl())));
    userProfileResponse.setCover(coverToUserProfileResponse(userProfileView.getCover()));
    userProfileResponse.setCreatedAt(toZoneDateTime(userProfileView.getCreateAt()));
    userProfileResponse.setLastSeenAt(toZoneDateTime(userProfileView.getLastSeenAt()));
    userProfileResponse.setLocation(userProfileView.getLocation());
    userProfileResponse.setContacts(contactToResponse(userProfileView.getContacts()));
    userProfileResponse.setStats(userStatsToResponse(userProfileView.getProfileStats()));
    userProfileResponse.setProjects(userProjectsToResponse(userProfileView.getProjectsStats(), true));
    userProfileResponse.setTechnologies(userProfileView.getTechnologies());
    userProfileResponse.setAllocatedTimeToContribute(allocatedTimeToResponse(userProfileView.getAllocatedTimeToContribute()));
    userProfileResponse.setIsLookingForAJob(userProfileView.getIsLookingForAJob());
    userProfileResponse.setFirstContributedAt(toZoneDateTime(userProfileView.getFirstContributedAt()));
    return userProfileResponse;
  }

  static AllocatedTime allocatedTimeToResponse(UserAllocatedTimeToContribute allocatedTimeToContribute) {
    return isNull(allocatedTimeToContribute) ? null : switch (allocatedTimeToContribute) {
      case NONE -> AllocatedTime.NONE;
      case LESS_THAN_ONE_DAY -> AllocatedTime.LESS_THAN_ONE_DAY;
      case ONE_TO_THREE_DAYS -> AllocatedTime.ONE_TO_THREE_DAYS;
      case GREATER_THAN_THREE_DAYS -> AllocatedTime.GREATER_THAN_THREE_DAYS;
    };
  }

  static UserAllocatedTimeToContribute allocatedTimeToDomain(AllocatedTime allocatedTimeToContribute) {
    return isNull(allocatedTimeToContribute) ? null : switch (allocatedTimeToContribute) {
      case NONE -> UserAllocatedTimeToContribute.NONE;
      case LESS_THAN_ONE_DAY -> UserAllocatedTimeToContribute.LESS_THAN_ONE_DAY;
      case ONE_TO_THREE_DAYS -> UserAllocatedTimeToContribute.ONE_TO_THREE_DAYS;
      case GREATER_THAN_THREE_DAYS -> UserAllocatedTimeToContribute.GREATER_THAN_THREE_DAYS;
    };
  }

  static List<UserProfileProjects> userProjectsToResponse(final Set<UserProfileView.ProjectStats> projectStats,
      boolean includePrivateProjects) {
    return projectStats.stream()
        .filter(ps -> includePrivateProjects || ps.getVisibility() == onlydust.com.marketplace.api.domain.model.ProjectVisibility.PUBLIC)
        .map(ps -> {
          final UserProfileProjects userProfileProjects = new UserProfileProjects();
          userProfileProjects.setUserContributionCount(ps.getUserContributionCount());
          userProfileProjects.setUserLastContributedAt(toZoneDateTime(ps.getUserLastContributedAt()));
          userProfileProjects.setId(ps.getId());
          userProfileProjects.setName(ps.getName());
          userProfileProjects.setTotalGranted(ps.getTotalGranted());
          userProfileProjects.setLogoUrl(ps.getLogoUrl());
          userProfileProjects.setContributorCount(ps.getContributorCount());
          userProfileProjects.setIsLead(ps.getIsProjectLead());
          userProfileProjects.setLeadSince(toZoneDateTime(ps.getProjectLeadSince()));
          userProfileProjects.setSlug(ps.getSlug());
          userProfileProjects.setVisibility(switch (ps.getVisibility()) {
            case PUBLIC -> ProjectVisibility.PUBLIC;
            case PRIVATE -> ProjectVisibility.PRIVATE;
          });
          return userProfileProjects;
        })
        .toList();
  }

  static UserProfileStats userStatsToResponse(final UserProfileView.ProfileStats profileStats) {
    final UserProfileStats userProfileStats = new UserProfileStats();
    userProfileStats.setContributedProjectCount(profileStats.getContributedProjectCount());
    userProfileStats.setTotalsEarned(totalsEarnedToResponse(profileStats.getTotalsEarned()));
    userProfileStats.setContributionCountVariationSinceLastWeek(profileStats
        .getContributionCountVariationSinceLastWeek());
    userProfileStats.setLeadedProjectCount(profileStats.getLeadedProjectCount());
    userProfileStats.setContributionCount(profileStats.getContributionCount());
    userProfileStats.setContributionCountPerWeeks(
        profileStats.getContributionStats()
            .stream()
            .map(UserMapper::mapContributionStat).toList()
    );
    return userProfileStats;
  }

  static RewardTotalAmountsResponse totalsEarnedToResponse(TotalsEarned totalsEarned) {
    final RewardTotalAmountsResponse response = new RewardTotalAmountsResponse();
    response.setTotalAmount(totalsEarned.getTotalDollarsEquivalent());
    for (TotalEarnedPerCurrency totalEarnedPerCurrency : totalsEarned.getDetails()) {
      final MyRewardAmountResponse myRewardAmountResponse = new MyRewardAmountResponse();
      myRewardAmountResponse.setTotalAmount(totalEarnedPerCurrency.getTotalAmount());
      myRewardAmountResponse.totalDollarsEquivalent(totalEarnedPerCurrency.getTotalDollarsEquivalent());
      if (totalEarnedPerCurrency.getCurrency() != null) {
        myRewardAmountResponse.setCurrency(mapCurrency(totalEarnedPerCurrency.getCurrency()));
      }
      response.addDetailsItem(myRewardAmountResponse);
    }
    return response;
  }

  static List<ContactInformation> contactToResponse(final Set<Contact> contacts) {
    return contacts.stream()
        .map(contactInformation -> {
          final ContactInformation response = new ContactInformation();
          response.setContact(contactInformation.getContact());
          response.setChannel(switch (contactInformation.getChannel()) {
            case EMAIL -> ContactInformationChannel.EMAIL;
            case LINKEDIN -> ContactInformationChannel.LINKEDIN;
            case TWITTER -> ContactInformationChannel.TWITTER;
            case TELEGRAM -> ContactInformationChannel.TELEGRAM;
            case DISCORD -> ContactInformationChannel.DISCORD;
            case WHATSAPP -> ContactInformationChannel.WHATSAPP;
          });
          response.setVisibility(switch (contactInformation.getVisibility()) {
            case PUBLIC -> ContactInformation.VisibilityEnum.PUBLIC;
            case PRIVATE -> ContactInformation.VisibilityEnum.PRIVATE;
          });
          return response;
        }).toList();
  }

  public static UserProfileCoverColor coverToUserProfileResponse(final @NonNull UserProfileCover cover) {
    return switch (cover) {
      case BLUE -> UserProfileCoverColor.BLUE;
      case CYAN -> UserProfileCoverColor.CYAN;
      case MAGENTA -> UserProfileCoverColor.MAGENTA;
      case YELLOW -> UserProfileCoverColor.YELLOW;
    };
  }

  static GetMeResponse userToGetMeResponse(final User authenticatedUser) {
    final GetMeResponse getMeResponse = new GetMeResponse();
    getMeResponse.setId(authenticatedUser.getId());
    getMeResponse.setGithubUserId(authenticatedUser.getGithubUserId());
    getMeResponse.setAvatarUrl(authenticatedUser.getGithubAvatarUrl());
    getMeResponse.setLogin(authenticatedUser.getGithubLogin());
    getMeResponse.setHasSeenOnboardingWizard(authenticatedUser.hasSeenOnboardingWizard());
    getMeResponse.setHasAcceptedLatestTermsAndConditions(authenticatedUser.hasAcceptedLatestTermsAndConditions());
    getMeResponse.setHasValidPayoutInfos(authenticatedUser.getHasValidPayoutInfos());
    getMeResponse.setProjectsLed(authenticatedUser.getProjectsLed()
        .stream().map(projectLedView -> new ProjectLedShortResponse()
            .id(projectLedView.getId())
            .slug(projectLedView.getSlug())
            .name(projectLedView.getName())
            .logoUrl(projectLedView.getLogoUrl())
            .contributorCount(projectLedView.getContributorCount())
        )
        .toList());
    getMeResponse.setPendingProjectsLed(authenticatedUser.getPendingProjectsLed()
        .stream().map(projectLedView -> new ProjectLedShortResponse()
            .id(projectLedView.getId())
            .slug(projectLedView.getSlug())
            .name(projectLedView.getName())
            .logoUrl(projectLedView.getLogoUrl())
            .contributorCount(projectLedView.getContributorCount())
        )
        .toList());
    getMeResponse.setProjectsAppliedTo(authenticatedUser.getProjectsAppliedTo());
    getMeResponse.setIsAdmin(authenticatedUser.hasRole(UserRole.ADMIN));
    return getMeResponse;
  }

  static UserContributionStats mapContributionStat(UserProfileView.ProfileStats.ContributionStats contributionStats) {
    return new UserContributionStats()
        .codeReviewCount(contributionStats.getCodeReviewCount())
        .issueCount(contributionStats.getIssueCount())
        .pullRequestCount(contributionStats.getPullRequestCount())
        .week(contributionStats.getWeek())
        .year(contributionStats.getYear());
  }
}
