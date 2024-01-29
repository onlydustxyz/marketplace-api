package onlydust.com.marketplace.kernel.visitor;

public interface Visitable<T extends Visitable<T>> {
    <R> R accept(Visitor<T, R> visitor);
}
