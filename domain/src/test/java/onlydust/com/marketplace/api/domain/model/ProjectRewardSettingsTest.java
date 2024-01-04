package onlydust.com.marketplace.api.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.junit.jupiter.api.Test;

class ProjectRewardSettingsTest {

  @Test
  void default_reward_settings() {
    final var now = Date.from(ZonedDateTime.of(2023, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant());

    final var projectRewardSettings = ProjectRewardSettings.defaultSettings(now);

    assertThat(projectRewardSettings.getIgnorePullRequests()).isFalse();
    assertThat(projectRewardSettings.getIgnoreIssues()).isFalse();
    assertThat(projectRewardSettings.getIgnoreCodeReviews()).isFalse();
    assertThat(projectRewardSettings.getIgnoreContributionsBefore()).isEqualTo(Date.from(ZonedDateTime.of(2023, 3
        , 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant()));
  }

  @Test
  void can_ignore_issues() {
    final var projectRewardSettings = new ProjectRewardSettings(false, true, false, null);
    assertThat(projectRewardSettings.getIgnoreIssues()).isTrue();
    assertThat(projectRewardSettings.getIgnorePullRequests()).isFalse();
    assertThat(projectRewardSettings.getIgnoreCodeReviews()).isFalse();
    assertThat(projectRewardSettings.getIgnoreContributionsBefore()).isNull();
  }

  @Test
  void can_ignore_pull_requests() {
    final var projectRewardSettings = new ProjectRewardSettings(true, false, false, null);
    assertThat(projectRewardSettings.getIgnoreIssues()).isFalse();
    assertThat(projectRewardSettings.getIgnorePullRequests()).isTrue();
    assertThat(projectRewardSettings.getIgnoreCodeReviews()).isFalse();
    assertThat(projectRewardSettings.getIgnoreContributionsBefore()).isNull();
  }

  @Test
  void can_ignore_code_reviews() {
    final var projectRewardSettings = new ProjectRewardSettings(false, false, true, null);
    assertThat(projectRewardSettings.getIgnoreIssues()).isFalse();
    assertThat(projectRewardSettings.getIgnorePullRequests()).isFalse();
    assertThat(projectRewardSettings.getIgnoreCodeReviews()).isTrue();
    assertThat(projectRewardSettings.getIgnoreContributionsBefore()).isNull();
  }

  @Test
  void can_ignore_contributions_before() {
    final var now = Date.from(ZonedDateTime.of(2023, 4, 1, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant());

    final var projectRewardSettings = new ProjectRewardSettings(false, false, false, now);
    assertThat(projectRewardSettings.getIgnoreIssues()).isFalse();
    assertThat(projectRewardSettings.getIgnorePullRequests()).isFalse();
    assertThat(projectRewardSettings.getIgnoreCodeReviews()).isFalse();
    assertThat(projectRewardSettings.getIgnoreContributionsBefore()).isEqualTo(now);
  }

  @Test
  void cannot_ignore_everything() {
    assertThrows(OnlyDustException.class, () ->
        new ProjectRewardSettings(true, true, true, null));
  }

}