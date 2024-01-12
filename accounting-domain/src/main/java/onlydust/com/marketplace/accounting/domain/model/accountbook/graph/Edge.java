package onlydust.com.marketplace.accounting.domain.model.accountbook.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import org.jgrapht.graph.DefaultEdge;

@AllArgsConstructor
@Getter
public class Edge extends DefaultEdge {
    private PositiveAmount amount;
    private final int outgoingOrder;


    public Vertex getSource() {
        return (Vertex) super.getSource();
    }

    public Vertex getTarget() {
        return (Vertex) super.getTarget();
    }

    public void decreaseAmount(PositiveAmount amount) {
        this.amount = PositiveAmount.of(this.amount.subtract(amount));
    }
}
