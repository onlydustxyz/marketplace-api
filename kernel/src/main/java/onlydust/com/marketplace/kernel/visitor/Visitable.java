package onlydust.com.marketplace.kernel.visitor;

public interface Visitable<T extends Visitable<T>> {
    void accept(Visitor<T> visitor);
}
