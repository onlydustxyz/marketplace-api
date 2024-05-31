package onlydust.com.marketplace.api.rest.api.adapter;

import lombok.AllArgsConstructor;
import onlydust.com.backoffice.api.contract.BackofficeProjectCategoriesApi;
import onlydust.com.marketplace.project.domain.model.ProjectCategorySuggestion;
import onlydust.com.marketplace.project.domain.port.input.ProjectFacadePort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.springframework.http.ResponseEntity.noContent;

@RestController
@AllArgsConstructor
public class BackofficeProjectCategoriesRestApi implements BackofficeProjectCategoriesApi {
    private final ProjectFacadePort projectFacadePort;

    @Override
    public ResponseEntity<Void> deleteProjectCategorySuggestion(UUID id) {
        projectFacadePort.deleteCategorySuggestion(ProjectCategorySuggestion.Id.of(id));
        return noContent().build();
    }
}
