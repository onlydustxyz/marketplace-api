package onlydust.com.marketplace.api.domain.port.output;

public interface TrackingIssuePort {

  void createIssueForTechTeam(String title, String description);
}
