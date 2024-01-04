package onlydust.com.marketplace.api.domain.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.stream.LongStream;
import onlydust.com.marketplace.api.domain.model.UserProfileCover;
import org.junit.jupiter.api.Test;

class UserProfileViewTest {

  @Test
  void getContributionCountVariationSinceLastWeek_without_any_stats() {
    // Given
    final var stats = UserProfileView.ProfileStats.builder()
        .contributionStats(List.of())
        .build();

    // When
    final var result = stats.getContributionCountVariationSinceLastWeek();

    // Then
    assertThat(result).isEqualTo(0);
  }

  @Test
  void getContributionCountVariationSinceLastWeek_with_stats_from_previous_week_only() {
    // Given
    LocalDate previousWeek = LocalDate.now().minusWeeks(1);
    final var stats = UserProfileView.ProfileStats.builder()
        .contributionStats(List.of(
            UserProfileView.ProfileStats.ContributionStats.builder()
                .year(previousWeek.getYear())
                .week(previousWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
                .codeReviewCount(2)
                .issueCount(5)
                .pullRequestCount(1)
                .build()
        ))
        .build();

    // When
    final var result = stats.getContributionCountVariationSinceLastWeek();

    // Then
    assertThat(result).isEqualTo(-8);
  }

  @Test
  void getContributionCountVariationSinceLastWeek_with_stats_from_current_week_only() {
    // Given
    LocalDate currentWeek = LocalDate.now();
    final var stats = UserProfileView.ProfileStats.builder()
        .contributionStats(List.of(
            UserProfileView.ProfileStats.ContributionStats.builder()
                .year(currentWeek.getYear())
                .week(currentWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
                .codeReviewCount(2)
                .issueCount(5)
                .pullRequestCount(1)
                .build()
        ))
        .build();

    // When
    final var result = stats.getContributionCountVariationSinceLastWeek();

    // Then
    assertThat(result).isEqualTo(8);
  }

  @Test
  void getContributionCountVariationSinceLastWeek_with_stats_from_current_week_and_previous_week() {
    // Given
    LocalDate currentWeek = LocalDate.now();
    LocalDate previousWeek = LocalDate.now().minusWeeks(1);
    final var stats = UserProfileView.ProfileStats.builder()
        .contributionStats(List.of(
            UserProfileView.ProfileStats.ContributionStats.builder()
                .year(previousWeek.getYear())
                .week(previousWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
                .codeReviewCount(2)
                .issueCount(5)
                .pullRequestCount(1)
                .build(),
            UserProfileView.ProfileStats.ContributionStats.builder()
                .year(currentWeek.getYear())
                .week(currentWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear()))
                .codeReviewCount(15)
                .issueCount(3)
                .pullRequestCount(0)
                .build()
        ))
        .build();

    // When
    final var result = stats.getContributionCountVariationSinceLastWeek();

    // Then
    assertThat(result).isEqualTo(10);
  }

  @Test
  void should_get_random_cover_by_default() {
    // Given a looooooot of covers
    final var covers = LongStream.rangeClosed(1, UserProfileCover.values().length * 10L)
        .mapToObj(id -> UserProfileView.builder().githubId(id).build().getCover())
        .toList();

    // Then
    assertThat(covers).doesNotContainNull();
    assertThat(covers).containsOnly(UserProfileCover.values());
    assertThat(covers.stream().sorted().distinct().toList().size()).isGreaterThan(1); // At least 2 different covers (randomness is not perfect)
  }
}
