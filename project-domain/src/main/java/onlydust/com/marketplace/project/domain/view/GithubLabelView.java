package onlydust.com.marketplace.project.domain.view;

import lombok.NonNull;

public record GithubLabelView(@NonNull Long id, @NonNull String name, String description
) {
}
