package onlydust.com.marketplace.accounting.domain.model.user;

public record GithubUserId(long value) {
    public static GithubUserId of(long value) {
        return new GithubUserId(value);
    }
}
