package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.port.output.DustyBotStoragePort;
import onlydust.com.marketplace.api.domain.view.RewardableItemView;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.CloseIssueRequestDTO;
import onlydust.com.marketplace.api.github_api.dto.CreateIssueRequestDTO;
import onlydust.com.marketplace.api.github_api.dto.IssueResponseDTO;

@AllArgsConstructor
public class GithubDustyBotAdapter implements DustyBotStoragePort {

    private final GithubHttpClient dustyBotClient;

    @Override
    public RewardableItemView createIssue(final String repoOwner,
                                          final String repoName,
                                          final String title,
                                          final String description) {
        final IssueResponseDTO createdIssueDTO = dustyBotClient.post(
                String.format("/repos/%s/%s/issues", repoOwner, repoName),
                CreateIssueRequestDTO.builder()
                        .body(description)
                        .title(title)
                        .build(),
                IssueResponseDTO.class
        ).orElseThrow(() -> OnlyDustException.internalServerError("Failed to create issue"));
        return createdIssueDTO.toView(repoName);
    }

    @Override
    public RewardableItemView closeIssue(final String repoOwner,
                                         final String repoName,
                                         final Long issueNumber) {
        final IssueResponseDTO closedIssueDTO = dustyBotClient.post(
                String.format("/repos/%s/%s/issues/%d", repoOwner, repoName, issueNumber),
                CloseIssueRequestDTO.builder().build(),
                IssueResponseDTO.class
        ).orElseThrow(() -> OnlyDustException.internalServerError("Failed to close issue"));
        return closedIssueDTO.toView(repoName);
    }
}
