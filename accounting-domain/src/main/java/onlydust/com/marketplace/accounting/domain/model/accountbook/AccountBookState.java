package onlydust.com.marketplace.accounting.domain.model.accountbook;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import onlydust.com.marketplace.accounting.domain.model.Amount;
import onlydust.com.marketplace.accounting.domain.model.PositiveAmount;
import onlydust.com.marketplace.kernel.visitor.Visitable;
import onlydust.com.marketplace.kernel.visitor.Visitor;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.traverse.DepthFirstIterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

import static onlydust.com.marketplace.accounting.domain.model.accountbook.AccountBook.AccountId.ROOT;
import static onlydust.com.marketplace.kernel.exception.OnlyDustException.badRequest;

public class AccountBookState implements AccountBook, ReadOnlyAccountBookState, Visitable<AccountBookState> {
    private final Graph<Vertex, Edge> graph = new SimpleDirectedGraph<>(Edge.class);
    private final Map<AccountId, List<Vertex>> accountVertices = new HashMap<>();

    private final Vertex root = Vertex.of(ROOT);


    public AccountBookState() {
        graph.addVertex(root);
        accountVertices.put(ROOT, new ArrayList<>(List.of(root)));
    }

    private static ArrayList<Transaction> mapAggregatedAmountsToTransactions(Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var transactions = new ArrayList<Transaction>(aggregatedAmounts.size());
        aggregatedAmounts.forEach((fromTo, amount) -> transactions.add(new Transaction(fromTo.from(), fromTo.to(), amount)));
        return transactions;
    }

    @Override
    public synchronized void mint(@NonNull final AccountId account, @NonNull final PositiveAmount amount) {
        createTransaction(root, account, amount);
    }

    @Override
    public synchronized List<Transaction> burn(@NonNull final AccountId account, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(account, ROOT);
        final var unspentVertices = unspentVerticesOf(account);
        try {
            return sendFromVertices(ROOT, amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw badRequest("Cannot burn %s from %s".formatted(amount, account), e);
        }
    }

    @Override
    public synchronized void transfer(@NonNull final AccountId from, @NonNull final AccountId to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from);
        try {
            sendFromVertices(to, amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw badRequest("Cannot transfer %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    @Override
    public synchronized void refund(@NonNull final AccountId from, @NonNull final AccountId to, @NonNull final PositiveAmount amount) {
        checkAccountsAreNotTheSame(from, to);
        final var unspentVertices = unspentVerticesOf(from, to);
        try {
            refundFromVertices(amount, unspentVertices);
        } catch (InsufficientFundsException e) {
            throw badRequest("Cannot refund %s from %s to %s".formatted(amount, from, to), e);
        }
    }

    @Override
    public synchronized Set<AccountId> refund(@NonNull final AccountId from) {
        final var vertices = accountVertices(from);
        if (vertices.stream().anyMatch(v -> !graph.outgoingEdgesOf(v).isEmpty())) {
            throw badRequest("Cannot entirely refund %s because it has outgoing transactions".formatted(from));
        }
        return new ArrayList<>(vertices).stream().map(vertex -> {
            final var refunded = incomingEdgeOf(vertex).source.accountId;
            removeTransaction(vertex);
            return refunded;
        }).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public synchronized @NonNull PositiveAmount balanceOf(@NonNull final AccountId account) {
        final var unspentVertices = unspentVerticesOf(account);
        return unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    @Override
    public synchronized @NonNull PositiveAmount amountReceivedBy(@NonNull final AccountId account) {
        return accountVertices(account).stream()
                .map(v -> incomingEdgeOf(v).amount).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    @Override
    public synchronized @NonNull PositiveAmount refundableBalance(@NonNull AccountId from, @NonNull AccountId to) {
        final var unspentVertices = unspentVerticesOf(from, to);
        return unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    @Override
    public synchronized @NonNull PositiveAmount transferredAmount(@NonNull AccountId from, @NonNull AccountId to) {
        return accountVertices(to).stream()
                .filter(v -> hasParent(v, from))
                .map(v -> incomingEdgeOf(v).amount).reduce(PositiveAmount.ZERO, PositiveAmount::add);
    }

    @Override
    public synchronized boolean hasParent(@NonNull AccountId to, @NonNull Collection<AccountId> from) {
        return accountVertices(to).stream().anyMatch(v -> from.stream().anyMatch(f -> hasParent(v, f)));
    }

    @Override
    public synchronized @NonNull List<Transaction> transactionsFrom(@NonNull AccountId from) {
        final var startVertices = accountVertices(from);
        final Map<FromTo, PositiveAmount> aggregatedAmounts = new HashMap<>();

        startVertices.forEach(startVertex -> aggregateOutgoingTransactions(startVertex, aggregatedAmounts));
        return mapAggregatedAmountsToTransactions(aggregatedAmounts);
    }

    @Override
    public synchronized @NonNull List<Transaction> transactionsTo(@NonNull AccountId to) {
        final var startVertices = accountVertices(to);
        final Map<FromTo, PositiveAmount> aggregatedAmounts = new HashMap<>();

        startVertices.forEach(startVertex -> aggregateIncomingTransactions(startVertex, aggregatedAmounts));
        return mapAggregatedAmountsToTransactions(aggregatedAmounts);
    }

    @Override
    public synchronized @NonNull Map<AccountId, PositiveAmount> transferredAmountPerOrigin(@NonNull AccountId to) {
        return accountVertices(to).stream()
                .map(v -> new Transaction(source(v).accountId, to, incomingEdgeOf(v).amount))
                .collect(Collectors.groupingBy(Transaction::from,
                        Collectors.mapping(Transaction::amount,
                                Collectors.reducing(PositiveAmount.ZERO, PositiveAmount::add))
                ));
    }

    @Override
    public synchronized @NonNull Map<AccountId, PositiveAmount> balancePerOrigin(@NonNull AccountId to) {
        return accountVertices(to).stream()
                .map(v -> new Transaction(source(v).accountId, to, balanceOf(v)))
                .collect(Collectors.groupingBy(Transaction::from,
                        Collectors.mapping(Transaction::amount,
                                Collectors.reducing(PositiveAmount.ZERO, PositiveAmount::add))
                ));
    }

    private void aggregateOutgoingTransactions(Vertex startVertex, Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var iterator = new DepthFirstIterator<>(graph, startVertex);
        iterator.forEachRemaining(v -> {
            if (!v.equals(startVertex)) {
                final var incomingEdge = incomingEdgeOf(v);
                aggregatedAmounts.merge(new FromTo(incomingEdge.source.accountId, v.accountId), incomingEdge.amount, PositiveAmount::add);
            }
        });
    }

    private void aggregateIncomingTransactions(@NonNull final Vertex vertex, @NonNull final Map<FromTo, PositiveAmount> aggregatedAmounts) {
        final var incomingEdge = incomingEdgeOf(vertex);
        if (incomingEdge.source.equals(root)) {
            return;
        }
        aggregatedAmounts.merge(new FromTo(incomingEdge.source.accountId, vertex.accountId), incomingEdge.amount, PositiveAmount::add);
        aggregateIncomingTransactions(incomingEdge.source, aggregatedAmounts);
    }

    private boolean hasParent(@NonNull final Vertex vertex, @NonNull final AccountId parent) {
        if (vertex.equals(root)) {
            return false;
        }
        final var directParent = incomingEdgeOf(vertex).source;
        if (directParent.accountId.equals(parent)) {
            return true;
        }
        return hasParent(directParent, parent);
    }

    private List<Transaction> sendFromVertices(@NonNull final AccountId to, @NonNull final PositiveAmount amount,
                                               @NonNull final List<VertexWithBalance> unspentVertices) throws InsufficientFundsException {
        final var unspentTotal = unspentVertices.stream().map(VertexWithBalance::balance).reduce(PositiveAmount.ZERO, PositiveAmount::add);
        if (unspentTotal.isStrictlyLowerThan(amount)) {
            throw new InsufficientFundsException("Insufficient funds: %s < %s".formatted(unspentTotal, amount));
        }

        final var transactions = new ArrayList<Transaction>(unspentVertices.size());
        var remainingAmount = amount;
        for (VertexWithBalance unspentVertex : unspentVertices) {
            if (unspentVertex.balance().isGreaterThanOrEqual(remainingAmount)) {
                transactions.add(createTransaction(unspentVertex.vertex(), to, remainingAmount));
                break;
            }
            transactions.add(createTransaction(unspentVertex.vertex(), to, unspentVertex.balance()));
            remainingAmount = PositiveAmount.of(remainingAmount.subtract(unspentVertex.balance()));
        }

        return transactions;
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

    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final AccountId accountId) {
        return accountVertices(accountId).stream()
                .map(v -> new VertexWithBalance(v, balanceOf(v)))
                .filter(v -> v.balance().isStrictlyGreaterThan(PositiveAmount.ZERO))
                .toList();
    }

    private @NonNull List<VertexWithBalance> unspentVerticesOf(@NonNull final AccountId accountId, @NonNull final AccountId from) {
        return accountVertices(accountId).stream()
                .filter(v -> incomingEdgeOf(v).source.accountId.equals(from))
                .map(v -> new VertexWithBalance(v, balanceOf(v)))
                .filter(v -> v.balance().isStrictlyGreaterThan(PositiveAmount.ZERO))
                .toList();
    }

    @Override
    @NotNull
    public synchronized Map<AccountId, PositiveAmount> unspentChildren(@NotNull AccountId of) {
        return accountVertices(of).stream()
                .flatMap(v -> unspentChildren(v).entrySet().stream())
                .collect(Collectors.groupingBy(e -> e.getKey().accountId,
                        Collectors.mapping(Map.Entry::getValue,
                                Collectors.reducing(PositiveAmount.ZERO, PositiveAmount::add))
                ));
    }

    @Override
    @NotNull
    public synchronized Map<AccountId, PositiveAmount> unspentChildren() {
        return unspentChildren(ROOT);
    }

    private Map<Vertex, PositiveAmount> unspentChildren(Vertex of) {
        final var children = new HashMap<Vertex, PositiveAmount>();

        new DepthFirstIterator<>(graph, of).forEachRemaining(v -> {
            if (!v.equals(of) && balanceOf(v).isStrictlyGreaterThan(Amount.ZERO)) children.put(v, balanceOf(v));
        });

        return children;
    }

    private @NonNull PositiveAmount balanceOf(@NonNull final Vertex vertex) {
        final PositiveAmount received = incomingEdgeOf(vertex).amount;
        final var spent = Edge.totalAmountOf(graph.outgoingEdgesOf(vertex));
        return PositiveAmount.of(received.subtract(spent));
    }

    private @NonNull Edge incomingEdgeOf(@NonNull final Vertex vertex) {
        return graph.incomingEdgesOf(vertex).iterator().next();
    }

    private @NonNull List<Vertex> accountVertices(@NonNull final AccountId accountId) {
        final var vertices = accountVertices.get(accountId);
        if (vertices != null) {
            return vertices;
        }
        return List.of();
    }

    private Transaction createTransaction(@NonNull final Vertex from, @NonNull final AccountId to, @NonNull final PositiveAmount amount) {
        final var toVertex = Vertex.of(to);
        graph.addVertex(toVertex);
        if (accountVertices.containsKey(to)) {
            accountVertices.get(to).add(toVertex);
        } else {
            accountVertices.put(to, new ArrayList<>(List.of(toVertex)));
        }

        final var edge = new Edge(from, amount);
        graph.addEdge(from, toVertex, edge);

        return new Transaction(source(from).accountId, to, amount);
    }

    private Vertex source(@NonNull final Vertex vertex) {
        if (vertex.equals(root)) {
            return root;
        }

        final var parent = incomingEdgeOf(vertex).source;
        if (parent.equals(root)) {
            return vertex;
        }

        return source(parent);
    }

    private void removeTransaction(@NonNull final Vertex vertex) {
        if (!graph.outgoingEdgesOf(vertex).isEmpty()) {
            throw new IllegalStateException("Cannot remove a vertex with outgoing edges");
        }
        graph.removeEdge(incomingEdgeOf(vertex));
        graph.removeVertex(vertex);
        accountVertices.get(vertex.accountId).remove(vertex);
    }

    private void checkAccountsAreNotTheSame(@NonNull final AccountId from, @NonNull final AccountId to) {
        if (from.equals(to)) {
            throw badRequest("An account (%s) cannot transfer money to itself".formatted(from));
        }
    }

    @Override
    public synchronized <R> R accept(Visitor<AccountBookState, R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public synchronized void export(@NotNull Exporter exporter) {
        exporter.export(this);
    }

    private record VertexWithBalance(@NonNull Vertex vertex, @NonNull PositiveAmount balance) {
    }

    private record FromTo(@NonNull AccountId from, @NonNull AccountId to) {
    }

    public static class InsufficientFundsException extends Exception {
        public InsufficientFundsException(String message) {
            super(message);
        }
    }

    @AllArgsConstructor
    private static class Edge {
        private final Vertex source;
        private PositiveAmount amount;

        private static PositiveAmount totalAmountOf(Collection<Edge> edges) {
            var acc = PositiveAmount.ZERO;
            for (Edge edge : edges)
                acc = acc.add(edge.amount);
            return acc;
        }

        private void decreaseAmount(PositiveAmount amount) {
            this.amount = PositiveAmount.of(this.amount.subtract(amount));
        }
    }

    // We do not want a record here because we want to compare vertex
    // pointer addresses in .equals and .hashcode
    @SuppressWarnings("ClassCanBeRecord")
    @AllArgsConstructor(staticName = "of")
    private static class Vertex {
        private final AccountId accountId;
    }

    public interface Exporter {
        void export(AccountBookState state);
    }

    @AllArgsConstructor
    public static class DotExporter implements Exporter {
        private final String filePath;
        private final AccountId root;

        @Override
        @SneakyThrows
        public void export(AccountBookState state) {
            final var exporter = new DOTExporter<Vertex, Edge>();

            exporter.setVertexAttributeProvider(this::attributes);
            exporter.setEdgeAttributeProvider(this::attributes);
            exporter.setVertexIdProvider(this::idOf);

            final var writer = new BufferedWriter(new FileWriter(filePath));

            final var graph = getSubgraph(state, root);

            exporter.exportGraph(graph, writer);
        }

        private AsSubgraph<Vertex, Edge> getSubgraph(AccountBookState state, AccountId root) {
            return new AsSubgraph<>(state.graph, state.graph.vertexSet().stream()
                    .filter(v -> v.accountId.equals(root) || state.hasParent(v, root) ||
                                 state.accountVertices(root).stream().anyMatch(r -> state.hasParent(r, v.accountId)))
                    .collect(Collectors.toSet())
            );
        }

        private Map<String, Attribute> attributes(Vertex v) {
            return Map.of(
                    "label", DefaultAttribute.createAttribute(v.accountId.toString()),
                    "style", DefaultAttribute.createAttribute("filled"),
                    "color", colorOf(v),
                    "fontcolor", DefaultAttribute.createAttribute("black"),
                    "labelfontcolor", DefaultAttribute.createAttribute("black")
            );
        }

        private String hexToDec(String hex) {
            return String.valueOf(Integer.parseInt(hex, 16));
        }

        private Attribute colorOf(Vertex v) {
            return DefaultAttribute.createAttribute(v.accountId.type() == null ? "#000000" :
                    switch (v.accountId.type()) {
                        case SPONSOR_ACCOUNT -> "lightcoral";
                        case REWARD -> "palegreen";
                        case PROJECT -> "mediumpurple1";
                        case PAYMENT -> "salmon1";
                    });
        }

        private String idOf(Vertex v) {
            return v.accountId == ROOT ? String.valueOf(v.hashCode()) : hexToDec(v.accountId.toString().substring(0, 5));
        }

        private Map<String, Attribute> attributes(Edge e) {
            return Map.of("label", DefaultAttribute.createAttribute(e.amount.toString()),
                    "color", DefaultAttribute.createAttribute("#000000"));
        }
    }

    public static <T> Exporter ToDot(String filePath, T root) {
        return new DotExporter(filePath, root == null ? ROOT : AccountId.of(root));
    }
}
