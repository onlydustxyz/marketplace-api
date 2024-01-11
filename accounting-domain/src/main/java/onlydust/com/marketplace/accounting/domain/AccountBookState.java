package onlydust.com.marketplace.accounting.domain;

import lombok.NonNull;
import onlydust.com.marketplace.accounting.domain.model.Account;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.accounting.domain.model.accountbook.graph.Edge;
import onlydust.com.marketplace.accounting.domain.model.accountbook.graph.Vertex;
import onlydust.com.marketplace.kernel.exception.OnlyDustException;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;

public class AccountBookState {
    public static final Account.Id ROOT = Account.Id.of(UUID.fromString("10000000-0000-0000-0000-000000000000"));

    private final Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(Edge.class);
    private final Map<Account.Id, List<Vertex>> accountVertices = new HashMap<>();

    private final Vertex root = new Vertex(UUID.randomUUID(), ROOT);

    public AccountBookState() {
        graph.addVertex(root);
        accountVertices.put(ROOT, new ArrayList<>(List.of(root)));
    }

    public void mint(@NonNull final Account.Id account, @NonNull final PositiveAmount amount) {
        createTransaction(root, account, amount);
    }

    public void burn(@NonNull final Account.Id account, @NonNull final PositiveAmount amount) {
        refund(account, ROOT, amount);
    }

    public void transfer(@NonNull final Account.Id from, @NonNull final Account.Id to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from);
        try {
            sendFromVertices(to, amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw OnlyDustException.badRequest("Cannot transfer %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    public void refund(@NonNull final Account.Id from, @NonNull final Account.Id to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from, to);
        try {
            refundFromVertices(amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw OnlyDustException.badRequest("Cannot refund %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    public @NonNull PositiveAmount balanceOf(@NonNull final Account.Id account) {
        final var unspentVertices = unspentVerticesOf(account);
        return unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    private void sendFromVertices(@NonNull final Account.Id to, @NonNull final PositiveAmount amount, @NonNull final List<VertexWithBalance> unspentVertices) throws InsufficientFundsException {
        final var unspentTotal = unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
        if (unspentTotal.isStrictlyLowerThan(amount)) {
            throw new InsufficientFundsException("Insufficient funds: %s < %s".formatted(unspentTotal, amount));
        }

        var remainingAmount = amount;
        for (VertexWithBalance unspentVertex : unspentVertices) {
            if (unspentVertex.balance().isGreaterThanOrEqual(remainingAmount)) {
                createTransaction(unspentVertex.vertex(), to, remainingAmount);
                return;
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


    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final Account.Id accountId) {
        return accountVertices(accountId).stream()
                .map(v -> new VertexWithBalance(v, balanceOf(v)))
                .filter(v -> v.balance().isStrictlyGreaterThan(PositiveAmount.ZERO))
                .toList();
    }

    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final Account.Id accountId, @NonNull final Account.Id from) {
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

    private @NonNull List<Vertex> accountVertices(@NonNull final Account.Id accountId) {
        final var vertices = accountVertices.get(accountId);
        if (vertices != null) {
            return vertices;
        }
        return List.of();
    }

    private void createTransaction(@NonNull final Vertex from, @NonNull final Account.Id to, @NonNull final PositiveAmount amount) {
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

    private void checkAccountsAreNotTheSame(@NonNull final Account.Id from, @NonNull final Account.Id to) {
        if (from.equals(to)) {
            throw OnlyDustException.badRequest("An account (%s) cannot transfer money to itself".formatted(from));
        }
    }

    private record VertexWithBalance(@NonNull Vertex vertex, @NonNull PositiveAmount balance) {
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}
