package onlydust.com.marketplace.accounting.domain.model;

import org.jgrapht.graph.DefaultEdge;

import java.time.Instant;

public final class TxEdge extends DefaultEdge {
    private final long amount;
    private final Instant time;

    public TxEdge(long amount, Instant time) {
        this.amount = amount;
        this.time = time;
    }

    public long amount() {
        return amount;
    }

    public Instant time() {
        return time;
    }

    @Override
    public String toString() {
        return "TxEdge[" + "amount=" + amount + ", " + "time=" + time + ']';
    }
}
