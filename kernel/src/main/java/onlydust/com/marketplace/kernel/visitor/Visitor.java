package onlydust.com.marketplace.kernel.visitor;

public interface Visitor<T extends Visitable<T>, R> {
    R visit(T visitable);
}
