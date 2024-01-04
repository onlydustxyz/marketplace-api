package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import java.util.Date;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.VersionApi;
import onlydust.com.marketplace.api.contract.model.InlineResponse200;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.DateMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tags(@Tag(name = "Version"))
@AllArgsConstructor
public class VersionRestApi implements VersionApi {

  private final Date startingDate;

  @Override
  public ResponseEntity<InlineResponse200> getAPIVersion() {
    final InlineResponse200 inlineResponse200 = new InlineResponse200();
    inlineResponse200.setReleaseDate(DateMapper.toZoneDateTime(startingDate));
    return ResponseEntity.ok(inlineResponse200);
  }
}
