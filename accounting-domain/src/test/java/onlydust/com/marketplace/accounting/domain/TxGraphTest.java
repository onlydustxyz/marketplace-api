package onlydust.com.marketplace.accounting.domain;

import onlydust.com.marketplace.accounting.domain.model.TxEdge;
import onlydust.com.marketplace.accounting.domain.model.TxVertex;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TxGraphTest {

    private static final String SPONSOR1 = "Sponsor1";
    private static final String SPONSOR2 = "Sponsor2";
    private static final String COMMITTEE1 = "Committee1";
    private static final String COMMITTEE2 = "Committee2";
    private static final String PROJECT1 = "Project1";
    private static final String PROJECT2 = "Project2";
    private static final String CONTRIBUTOR2 = "Contributor2";


    @Test
    void should_create_a_tx_graph() {
        Graph<TxVertex, TxEdge> g = new SimpleDirectedWeightedGraph<>(TxEdge.class);

        final var root = new TxVertex("ROOT");
        final var txS1 = new TxVertex(SPONSOR1);
        final var txS2 = new TxVertex(SPONSOR2);
        final var txC1 = new TxVertex(COMMITTEE1);
        final var txC2 = new TxVertex(COMMITTEE1);
        final var txC3 = new TxVertex(COMMITTEE2);
        final var txC4 = new TxVertex(COMMITTEE2);
        final var txC5 = new TxVertex(COMMITTEE2);
        final var txP1 = new TxVertex(PROJECT1);
        final var txP2 = new TxVertex(PROJECT1);
        final var txP3 = new TxVertex(PROJECT2);
        final var txP4 = new TxVertex(PROJECT2);
        final var txP5 = new TxVertex(PROJECT2);
        final var txP6 = new TxVertex(PROJECT2);
        final var txP7 = new TxVertex(PROJECT2);
        final var txU1 = new TxVertex(CONTRIBUTOR2);
        final var txU2 = new TxVertex(CONTRIBUTOR2);
        final var txU3 = new TxVertex(CONTRIBUTOR2);
        final var txU4 = new TxVertex(CONTRIBUTOR2);
        g.addVertex(root);
        g.addVertex(txS1);
        g.addVertex(txS2);
        g.addVertex(txC1);
        g.addVertex(txC2);
        g.addVertex(txC3);
        g.addVertex(txC4);
        g.addVertex(txC5);
        g.addVertex(txP1);
        g.addVertex(txP2);
        g.addVertex(txP3);
        g.addVertex(txP4);
        g.addVertex(txP5);
        g.addVertex(txP6);
        g.addVertex(txP7);
        g.addVertex(txU1);
        g.addVertex(txU2);
        g.addVertex(txU3);
        g.addVertex(txU4);

        // root to sponsors
        g.addEdge(root, txS1, new TxEdge(10000L, Instant.now()));
        g.addEdge(root, txS2, new TxEdge(20000L, Instant.now()));

        // sponsors to committees
        g.addEdge(txS1, txC1, new TxEdge(8000L, Instant.now()));
        g.addEdge(txS2, txC2, new TxEdge(15000L, Instant.now()));
        g.addEdge(txS1, txC3, new TxEdge(1000L, Instant.now()));
        g.addEdge(txS2, txC4, new TxEdge(500L, Instant.now()));
        g.addEdge(txS2, txC5, new TxEdge(4500L, Instant.now()));

        // committees to projects
        g.addEdge(txC1, txP1, new TxEdge(8000L, Instant.now()));
        g.addEdge(txC2, txP2, new TxEdge(12000L, Instant.now()));
        g.addEdge(txC2, txP3, new TxEdge(2000L, Instant.now()));
        g.addEdge(txC3, txP4, new TxEdge(1000L, Instant.now()));
        g.addEdge(txC4, txP5, new TxEdge(500L, Instant.now()));
        g.addEdge(txC5, txP6, new TxEdge(500L, Instant.now()));
        g.addEdge(txC5, txP7, new TxEdge(1000L, Instant.now()));

        // projects to users
        g.addEdge(txP3, txU1, new TxEdge(2000L, Instant.now()));
        g.addEdge(txP4, txU2, new TxEdge(1000L, Instant.now()));
        g.addEdge(txP5, txU3, new TxEdge(500L, Instant.now()));
        g.addEdge(txP6, txU4, new TxEdge(100L, Instant.now()));

        // get committee 2 balance
        final long balance = g.vertexSet().stream().filter(v -> v.accountId().equals(COMMITTEE2)).map(
                v -> {
                    final var inputAmount = g.incomingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    final var outputAmount = g.outgoingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    return inputAmount - outputAmount;
                }
        ).reduce(0L, Long::sum);

        assertThat(balance).isEqualTo(3000L);

        // how much sponsor 2 gave to contributor 2?
        final long totalGaveBySponsor2ToContributor2 = g.vertexSet().stream().filter(v -> v.accountId().equals(SPONSOR2)).map(start -> {
            long subtotalGaveBySponsor2ToContributor2 = 0L;
            Iterator<TxVertex> iterator = new DepthFirstIterator<>(g, start);
            while (iterator.hasNext()) {
                final TxVertex v = iterator.next();
                if (v.accountId().equals(CONTRIBUTOR2)) {
                    final var inputAmount = g.incomingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    subtotalGaveBySponsor2ToContributor2 += inputAmount;
                }
            }
            return subtotalGaveBySponsor2ToContributor2;
        }).reduce(0L, Long::sum);

        assertThat(totalGaveBySponsor2ToContributor2).isEqualTo(2600L);
    }


    void addTx(Map<String, Set<TxVertex>> txVerticesPerRecipient, TxVertex tx) {
        if (txVerticesPerRecipient.containsKey(tx.accountId())) {
            txVerticesPerRecipient.get(tx.accountId()).add(tx);
        } else {
            txVerticesPerRecipient.put(tx.accountId(), new HashSet<>(new ArrayList<>(List.of(tx))));
        }
    }

    @Test
    void benchmark() throws InterruptedException {
        Instant startTime = Instant.now();
        Graph<TxVertex, TxEdge> g = new SimpleDirectedWeightedGraph<>(TxEdge.class);

        final var root = new TxVertex("ROOT");
        g.addVertex(root);
        final Map<String, Set<TxVertex>> txVerticesPerRecipient = new HashMap<>();

        final var sponsorCount = 50;
        final var sponsorTxCount = sponsorCount * 100;
        final List<TxVertex> sponsorTxs = new ArrayList<>();
        for (var i = 0; i < sponsorTxCount; i++) {
            final var txVertex = new TxVertex("Sponsor" + (i % sponsorCount));
            sponsorTxs.add(txVertex);
            addTx(txVerticesPerRecipient, txVertex);
            g.addVertex(txVertex);
            g.addEdge(root, txVertex, new TxEdge(1000000L, Instant.now()));
        }

        final var committeeCount = 40;
        final var committeeTxCount = committeeCount * 100;
        final List<TxVertex> committeeTxs = new ArrayList<>();

        for (var i = 0; i < committeeTxCount; i++) {
            final var txVertex = new TxVertex("Committee" + (i % committeeCount));
            committeeTxs.add(txVertex);
            addTx(txVerticesPerRecipient, txVertex);
            g.addVertex(txVertex);
            g.addEdge(sponsorTxs.get(i % sponsorTxCount), txVertex, new TxEdge(90000L,
                    Instant.now()));
        }

        final var projectCount = 2000;
        final var projectTxCount = projectCount * 100;
        final List<TxVertex> projectTxs = new ArrayList<>();
        for (var i = 0; i < projectTxCount; i++) {
            final var txVertex = new TxVertex("Project" + (i % projectCount));
            projectTxs.add(txVertex);
            addTx(txVerticesPerRecipient, txVertex);
            g.addVertex(txVertex);
            g.addEdge(committeeTxs.get(i % committeeTxCount), txVertex, new TxEdge(8000L,
                    Instant.now()));
        }

        final var contributorCount = 5000;
        final var contributorTxCount = contributorCount * 50;
        for (var i = 0; i < contributorTxCount; i++) {
            final var txVertex = new TxVertex("Contributor" + (i % contributorCount));
            g.addVertex(txVertex);
            addTx(txVerticesPerRecipient, txVertex);
            g.addEdge(projectTxs.get(i % projectTxCount), txVertex, new TxEdge(7L,
                    Instant.now()));
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(startTime, finish).toMillis();
        System.out.println("Graph created in: " + timeElapsed + "ms");

        startTime = Instant.now();

        // get committee 2 balance
        final long balance = txVerticesPerRecipient.getOrDefault("Committee2", Set.of()).stream().map(
                v -> {
                    final var inputAmount = g.incomingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    final var outputAmount = g.outgoingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    return inputAmount - outputAmount;
                }
        ).reduce(0L, Long::sum);

        finish = Instant.now();
        timeElapsed = Duration.between(startTime, finish).toMillis();
        System.out.println("Balance computed in: " + timeElapsed + "ms");
        System.out.println("Balance: " + balance);

        // how much sponsor 20 gave to contributor 420?
        final long totalGaveBySponsor2ToContributor2 = txVerticesPerRecipient.getOrDefault("Sponsor20", Set.of()).stream().map(start -> {
            long subtotalGaveBySponsor2ToContributor2 = 0L;
            Iterator<TxVertex> iterator = new DepthFirstIterator<>(g, start);
            while (iterator.hasNext()) {
                final TxVertex v = iterator.next();
                if (v.accountId().equals("Contributor420")) {
                    final var inputAmount = g.incomingEdgesOf(v).stream().map(TxEdge::amount).reduce(0L, Long::sum);
                    subtotalGaveBySponsor2ToContributor2 += inputAmount;
                }
            }
            return subtotalGaveBySponsor2ToContributor2;
        }).reduce(0L, Long::sum);

        finish = Instant.now();
        timeElapsed = Duration.between(startTime, finish).toMillis();
        System.out.println("totalGaveBySponsor2ToContributor2 computed in: " + timeElapsed + "ms");
        System.out.println("totalGaveBySponsor2ToContributor2: " + totalGaveBySponsor2ToContributor2);

        Thread.sleep(1000000);
    }
}
