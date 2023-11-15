package onlydust.com.marketplace.api.github_api.adapters;

import lombok.AllArgsConstructor;
import onlydust.com.marketplace.api.domain.exception.OnlyDustException;
import onlydust.com.marketplace.api.domain.model.CreateAndCloseIssueCommand;
import onlydust.com.marketplace.api.domain.port.output.DustyBotStoragePort;
import onlydust.com.marketplace.api.domain.view.CreatedAndClosedIssueView;
import onlydust.com.marketplace.api.github_api.GithubHttpClient;
import onlydust.com.marketplace.api.github_api.dto.CloseIssueRequestDTO;
import onlydust.com.marketplace.api.github_api.dto.CreateIssueRequestDTO;
import onlydust.com.marketplace.api.github_api.dto.IssueResponseDTO;

@AllArgsConstructor
public class GithubDustyBotAdapter implements DustyBotStoragePort {

    private final GithubHttpClient dustyBotClient;

    @Override
    public CreatedAndClosedIssueView createIssue(final CreateAndCloseIssueCommand createAndCloseIssueCommand) {
        final IssueResponseDTO createdIssueDTO = dustyBotClient.post(String.format("/repos/%s/%s/issues",
                        createAndCloseIssueCommand.getGithubRepoOwnerName(),
                        createAndCloseIssueCommand.getGithubRepoName()),
                CreateIssueRequestDTO.builder()
                        .body(createAndCloseIssueCommand.getDescription())
                        .title(createAndCloseIssueCommand.getTitle())
                        .build(), IssueResponseDTO.class

        ).orElseThrow(() -> OnlyDustException.internalServerError("Failed to create issue"));
        return createdIssueDTO.toView(createAndCloseIssueCommand.getGithubRepoName());
    }

    @Override
    public CreatedAndClosedIssueView closeIssue(final CreateAndCloseIssueCommand createAndCloseIssueCommand) {
        final IssueResponseDTO closedIssueDTO = dustyBotClient.post(String.format("/repos/%s/%s/issues/%s",
                        createAndCloseIssueCommand.getGithubRepoOwnerName(),
                        createAndCloseIssueCommand.getGithubRepoName(),
                        createAndCloseIssueCommand.getGithubIssueNumber()),
                CloseIssueRequestDTO.builder().build(), IssueResponseDTO.class

        ).orElseThrow(() -> OnlyDustException.internalServerError("Failed to close issue"));
        return closedIssueDTO.toView(createAndCloseIssueCommand.getGithubRepoName());
    }
}
