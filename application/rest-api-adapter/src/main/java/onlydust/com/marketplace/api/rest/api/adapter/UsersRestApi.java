package onlydust.com.marketplace.api.rest.api.adapter;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.contract.UsersApi;
import onlydust.com.marketplace.api.contract.model.ContributorSearchResponse;
import onlydust.com.marketplace.api.contract.model.PublicUserProfileResponse;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.project.domain.port.input.ContributorFacadePort;
import onlydust.com.marketplace.project.domain.port.input.UserFacadePort;
import onlydust.com.marketplace.project.domain.view.UserProfileView;
import onlydust.com.marketplace.api.rest.api.adapter.mapper.ContributorSearchResponseMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static onlydust.com.marketplace.api.rest.api.adapter.mapper.UserMapper.userProfileToPublicResponse;


@RestController
@Tags(@Tag(name = "Users"))
@AllArgsConstructor
public class UsersRestApi implements UsersApi {

    private final static int DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_TRIGGER_EXTERNAL_SEARCH = 5;
    private final static int DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN = 50;

    private final UserFacadePort userFacadePort;
    private final ContributorFacadePort contributorFacadePort;

    @Override
    public ResponseEntity<PublicUserProfileResponse> getUserProfile(Long githubId) {
        final UserProfileView userProfileView = userFacadePort.getProfileById(githubId);
        final PublicUserProfileResponse userProfileResponse = userProfileToPublicResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<PublicUserProfileResponse> getUserProfileByLogin(String githubLogin) {
        final UserProfileView userProfileView = userFacadePort.getProfileByLogin(githubLogin);
        final PublicUserProfileResponse userProfileResponse = userProfileToPublicResponse(userProfileView);
        return ResponseEntity.ok(userProfileResponse);
    }

    @Override
    public ResponseEntity<ContributorSearchResponse> searchContributors(UUID projectId, List<Long> repoIds,
                                                                        String login,
                                                                        Integer maxInternalContributorCountToTriggerExternalSearch,
                                                                        Integer maxInternalContributorCountToReturn,
                                                                        Boolean externalSearchOnly) {

        if (projectId == null && repoIds == null && login == null) {
            throw OnlyDustException.badRequest("At least one of projectId, repoIds and login query param must be " +
                                               "provided");
        }
        final var contributors = contributorFacadePort.searchContributors(
                projectId,
                repoIds != null ? new HashSet<>(repoIds) : null,
                login,
                maxInternalContributorCountToTriggerExternalSearch != null ?
                        maxInternalContributorCountToTriggerExternalSearch :
                        DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_TRIGGER_EXTERNAL_SEARCH,
                maxInternalContributorCountToReturn != null ? maxInternalContributorCountToReturn :
                        DEFAULT_MAX_INTERNAL_CONTRIBUTOR_COUNT_TO_RETURN,
                Boolean.TRUE.equals(externalSearchOnly));
        return ResponseEntity.ok(ContributorSearchResponseMapper.of(contributors.getLeft(), contributors.getRight()));
    }
}
