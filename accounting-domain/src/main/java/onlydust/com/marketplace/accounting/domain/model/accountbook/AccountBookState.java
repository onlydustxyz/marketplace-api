package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Ledger;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.graph.Edge;
import onlydust.com.marketplace.accounting.domain.model.accountbook.graph.Vertex;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import onlydust.com.marketplace.kernel.visitor.Visitable;
import onlydust.com.marketplace.kernel.visitor.Visitor;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

public class AccountBookState implements AccountBook, Visitable<AccountBookState> {
    public static final Ledger.Id ROOT = Ledger.Id.of(UUID.fromString("10000000-0000-0000-0000-000000000000"));

    private final Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(Edge.class);
    private final Map<Ledger.Id, List<Vertex>> accountVertices = new HashMap<>();

    private final Vertex root = new Vertex(UUID.randomUUID(), ROOT);

    public AccountBookState() {
        graph.addVertex(root);
        accountVertices.put(ROOT, new ArrayList<>(List.of(root)));
    }

    @Override
    public void mint(@NonNull final Ledger.Id account, @NonNull final PositiveAmount amount) {
        createTransaction(root, account, amount);
    }

    @Override
    public void burn(@NonNull final Ledger.Id account, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(account, ROOT);
        final var unspentVertices = unspentVerticesOf(account);
        try {
            sendFromVertices(ROOT, amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw OnlyDustException.badRequest("Cannot burn %s from %s".formatted(amount, account), e);
        }
    }

    @Override
    public void transfer(@NonNull final Ledger.Id from, @NonNull final Ledger.Id to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from);
        try {
            sendFromVertices(to, amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw OnlyDustException.badRequest("Cannot transfer %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    @Override
    public void refund(@NonNull final Ledger.Id from, @NonNull final Ledger.Id to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from, to);
        try {
            refundFromVertices(amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw OnlyDustException.badRequest("Cannot refund %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    public @NonNull PositiveAmount balanceOf(@NonNull final Ledger.Id account) {
        final var unspentVertices = unspentVerticesOf(account);
        return unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    public @NonNull PositiveAmount refundableBalance(@NonNull Ledger.Id from, @NonNull Ledger.Id to) {
        final var unspentVertices = unspentVerticesOf(from, to);
        return unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    public @NonNull PositiveAmount transferredAmount(@NonNull Ledger.Id from, @NonNull Ledger.Id to) {
        return accountVertices(to).stream()
                .filter(v -> hasParent(v, from))
                .map(v -> incomingEdgeOf(v).getAmount())
                .reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    public @NonNull List<Transaction> transactionsFrom(@NonNull Ledger.Id from) {
        final var startVertices = accountVertices(from);
        final Map<FromTo, PositiveAmount> aggregatedAmounts = new HashMap<>();

        startVertices.forEach(startVertex -> aggregateOutgoingTransactions(startVertex, aggregatedAmounts));
        return mapAggregatedAmountsToTransactions(aggregatedAmounts);
    }

    public @NonNull List<Transaction> transactionsTo(@NonNull Ledger.Id to) {
        final var startVertices = accountVertices(to);
        final Map<FromTo, PositiveAmount> aggregatedAmounts = new HashMap<>();

        startVertices.forEach(startVertex -> aggregateIncomingTransactions(startVertex, aggregatedAmounts));
        return mapAggregatedAmountsToTransactions(aggregatedAmounts);
    }

    private void aggregateOutgoingTransactions(Vertex startVertex, Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var iterator = new DepthFirstIterator<>(graph, startVertex);
        iterator.forEachRemaining(v -> {
            if (!v.equals(startVertex)) {
                final var incomingEdge = incomingEdgeOf(v);
                aggregatedAmounts.merge(new FromTo(incomingEdge.getSource().accountId(), v.accountId()), incomingEdge.getAmount(), PositiveAmount::add);
            }
        });
    }

    private void aggregateIncomingTransactions(@NonNull final Vertex vertex, @NonNull final Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var incomingEdge = incomingEdgeOf(vertex);
        if (incomingEdge.getSource().equals(root)) {
            return;
        }
        aggregatedAmounts.merge(new FromTo(incomingEdge.getSource().accountId(), vertex.accountId()), incomingEdge.getAmount(), PositiveAmount::add);
        aggregateIncomingTransactions(incomingEdge.getSource(), aggregatedAmounts);
    }

    private boolean hasParent(@NonNull final Vertex vertex, @NonNull final Ledger.Id parent) {
        if (vertex.equals(root)) {
            return false;
        }
        final var directParent = incomingEdgeOf(vertex).getSource();
        if (directParent.accountId().equals(parent)) {
            return true;
        }
        return hasParent(directParent, parent);
    }

    private void sendFromVertices(@NonNull final Ledger.Id to, @NonNull final PositiveAmount amount,
                                  @NonNull final List<VertexWithBalance> unspentVertices) throws InsufficientFundsException {
        final var unspentTotal = unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
        if (unspentTotal.isStrictlyLowerThan(amount)) {
            throw new InsufficientFundsException("Insufficient funds: %s < %s".formatted(unspentTotal, amount));
        }

        var remainingAmount = amount;
        for (VertexWithBalance unspentVertex : unspentVertices) {
            if (unspentVertex.balance().isGreaterThanOrEqual(remainingAmount)) {
                createTransaction(unspentVertex.vertex(), to, remainingAmount);
                break;
            }
            createTransaction(unspentVertex.vertex(), to, unspentVertex.balance());
            remainingAmount = PositiveAmount.of(remainingAmount.subtract(unspentVertex.balance()));
        }
    }

    private void refundFromVertices(@NonNull final PositiveAmount amount,
                                    @NonNull final List<VertexWithBalance> unspentVertices) throws InsufficientFundsException {
        final var unspentTotal = unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
        if (unspentTotal.isStrictlyLowerThan(amount)) {
            throw new InsufficientFundsException("Insufficient funds: %s < %s".formatted(unspentTotal, amount));
        }

        var remainingAmount = amount;
        for (VertexWithBalance unspentVertex : unspentVertices) {
            if (unspentVertex.balance().isStrictlyGreaterThan(remainingAmount)) {
                incomingEdgeOf(unspentVertex.vertex()).decreaseAmount(remainingAmount);
                return;
            }
            remainingAmount = PositiveAmount.of(remainingAmount.subtract(unspentVertex.balance()));
            if (graph.outgoingEdgesOf(unspentVertex.vertex()).isEmpty()) {
                removeTransaction(unspentVertex.vertex());
            } else {
                incomingEdgeOf(unspentVertex.vertex()).decreaseAmount(unspentVertex.balance());
            }
        }
    }


    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final Ledger.Id accountId) {
        return accountVertices(accountId).stream()
                .map(v -> new VertexWithBalance(v, balanceOf(v)))
                .filter(v -> v.balance().isStrictlyGreaterThan(PositiveAmount.ZERO))
                .toList();
    }

    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final Ledger.Id accountId, @NonNull final Ledger.Id from) {
        return accountVertices(accountId).stream()
                .filter(v -> incomingEdgeOf(v).getSource().accountId().equals(from))
                .map(v -> new VertexWithBalance(v, balanceOf(v)))
                .filter(v -> v.balance().isStrictlyGreaterThan(PositiveAmount.ZERO))
                .toList();
    }

    private @NonNull PositiveAmount balanceOf(@NonNull final Vertex vertex) {
        final PositiveAmount received = incomingEdgeOf(vertex).getAmount();
        final PositiveAmount spent = graph.outgoingEdgesOf(vertex).stream()
                .map(Edge::getAmount)
                .reduce(PositiveAmount.ZERO, PositiveAmount::add);
        return PositiveAmount.of(received.subtract(spent));
    }

    private @NonNull Edge incomingEdgeOf(@NonNull final Vertex vertex) {
        return graph.incomingEdgesOf(vertex).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Vertex %s has no incoming edge".formatted(vertex)));
    }

    private @NonNull List<Vertex> accountVertices(@NonNull final Ledger.Id accountId) {
        final var vertices = accountVertices.get(accountId);
        if (vertices != null) {
            return vertices;
        }
        return List.of();
    }

    private void createTransaction(@NonNull final Vertex from, @NonNull final Ledger.Id to, @NonNull final PositiveAmount amount) {
        final var toVertex = new Vertex(UUID.randomUUID(), to);
        graph.addVertex(toVertex);
        if (accountVertices.containsKey(to)) {
            accountVertices.get(to).add(toVertex);
        } else {
            accountVertices.put(to, new ArrayList<>(List.of(toVertex)));
        }

        final var transaction = new Edge(amount, graph.outgoingEdgesOf(from).size());
        graph.addEdge(from, toVertex, transaction);
    }

    private void removeTransaction(@NonNull final Vertex vertex) {
        if (!graph.outgoingEdgesOf(vertex).isEmpty()) {
            throw new IllegalStateException("Cannot remove a vertex with outgoing edges");
        }
        graph.removeEdge(incomingEdgeOf(vertex));
        graph.removeVertex(vertex);
        accountVertices.get(vertex.accountId()).remove(vertex);
    }

    private void checkAccountsAreNotTheSame(@NonNull final Ledger.Id from, @NonNull final Ledger.Id to) {
        if (from.equals(to)) {
            throw OnlyDustException.badRequest("An account (%s) cannot transfer money to itself".formatted(from));
        }
    }

    @Override
    public void accept(Visitor<AccountBookState> visitor) {
        visitor.visit(this);
    }

    private record VertexWithBalance(@NonNull Vertex vertex, @NonNull PositiveAmount balance) {
    }

    private record FromTo(@NonNull Ledger.Id from, @NonNull Ledger.Id to) {
    }

    private static ArrayList<Transaction> mapAggregatedAmountsToTransactions(Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var transactions = new ArrayList<Transaction>(aggregatedAmounts.size());
        aggregatedAmounts.forEach((fromTo, amount) -> transactions.add(new Transaction(fromTo.from(), fromTo.to(), amount)));
        return transactions;
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
