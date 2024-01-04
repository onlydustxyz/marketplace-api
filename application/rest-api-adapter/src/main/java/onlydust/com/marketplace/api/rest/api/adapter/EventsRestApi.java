package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.EventsApi;
import onlydust.com.marketplace.api.contract.model.OnContributionsChangeEvent;
import onlydust.com.marketplace.api.domain.port.input.ContributionObserverPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Events"))
@AllArgsConstructor
public class EventsRestApi implements EventsApi {

  private final ContributionObserverPort contributionObserverPort;

  @Override
  public ResponseEntity<Void> onContributionsChange(OnContributionsChangeEvent onContributionsChangeEvent) {
    contributionObserverPort.onContributionsChanged(onContributionsChangeEvent.getRepoIds());
    return ResponseEntity.noContent().build();
  }
}
