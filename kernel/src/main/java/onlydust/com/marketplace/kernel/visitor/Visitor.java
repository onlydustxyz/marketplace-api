package onlydust.com.marketplace.kernel.visitor;

public interface Visitor<T extends Visitable<T>> {
    void visit(T visitable);
}
