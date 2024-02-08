package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import org.jgrapht.graph.DefaultEdge;

import java.util.Collection;

@AllArgsConstructor
@Getter
public class Edge extends DefaultEdge {
    private PositiveAmount amount;

    public Vertex getSource() {
        return (Vertex) super.getSource();
    }

    public Vertex getTarget() {
        return (Vertex) super.getTarget();
    }

    public void decreaseAmount(PositiveAmount amount) {
        this.amount = PositiveAmount.of(this.amount.subtract(amount));
    }

    public static PositiveAmount totalAmountOf(Collection<Edge> edges) {
        var acc = PositiveAmount.ZERO;
        for (Edge edge : edges)
            acc = acc.add(edge.getAmount());
        return acc;
    }
}
