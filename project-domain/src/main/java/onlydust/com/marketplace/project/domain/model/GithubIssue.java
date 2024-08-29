package onlydust.com.marketplace.project.domain.model;

import lombok.NonNull;

import java.util.List;

public record GithubIssue(@NonNull Id id,
                          @NonNull Long repoId,
                          @NonNull Long number,
                          @NonNull String title,
                          String description,
                          @NonNull String htmlUrl,
                          @NonNull String repoName,
                          int assigneeCount,
                          String authorLogin,
                          String authorAvatarUrl,
                          List<String> labels) {
    public boolean isAssigned() {
        return assigneeCount > 0;
    }

    public record Id(Long value) {
        public static Id of(Long value) {
            return new Id(value);
        }

        public static Id random() {
            return new Id((long) (Math.random() * 1_000_000));
        }

        @Override
        public String toString() {
            return value.toString();
        }
    }
}
