package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;

public record GithubPullRequest(@NonNull GithubIssue.Id id,
                                @NonNull Long repoId,
                                @NonNull Long number) {

    public record Id(Long value) {
        public static GithubPullRequest.Id of(Long value) {
            return new GithubPullRequest.Id(value);
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
