package onlydust.com.marketplace.project.domain.model;

public record GithubComment(Id id) {

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
