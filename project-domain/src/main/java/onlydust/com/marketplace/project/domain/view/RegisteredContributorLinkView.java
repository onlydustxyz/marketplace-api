package onlydust.com.marketplace.project.domain.view;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.UUID;

@Data
@Builder
public class RegisteredContributorLinkView implements UserLinkView{
    @NonNull
    UUID id;
    @NonNull
    Long githubUserId;
    @NonNull
    String login;
    @NonNull
    String avatarUrl;
}
