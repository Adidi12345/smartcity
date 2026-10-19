import graph.*;
import dsu.DSU;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.util.*;

public class Tests {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static void checkMst(Graph g, long expected, int components) {
        var k = MST.kruskal(g);
        var p = MST.prim(g);
        check(k.weight == expected && p.weight == expected, "MST weight");
        check(k.components == components && p.components == components, "Components");
        check(k.edges.size() == g.n - components && p.edges.size() == g.n - components, "Forest size");
        check(k.connected() == (components == 1) && p.connected() == (components == 1), "Connected flag");
        for (var result : List.of(k, p)) {
            DSU dsu = new DSU(g.n);
            for (Edge e : result.edges) check(dsu.union(e.from(), e.to()), "Forest has no cycle");
        }
    }

    private static void checkDirected(Graph g) {
        boolean[][] reach = new boolean[g.n][g.n];
        for (int i = 0; i < g.n; i++) reach[i][i] = true;
        for (Edge e : g.edges) reach[e.from()][e.to()] = true;
        for (int k = 0; k < g.n; k++)
            for (int i = 0; i < g.n; i++)
                for (int j = 0; j < g.n; j++) reach[i][j] |= reach[i][k] && reach[k][j];
        int[] component = new int[g.n];
        Arrays.fill(component, -1);
        var scc = Connectivity.kosaraju(g);
        for (int c = 0; c < scc.components.size(); c++) {
            for (int v : scc.components.get(c)) {
                check(component[v] == -1, "Vertex in only one SCC");
                component[v] = c;
            }
        }
        for (int i = 0; i < g.n; i++) {
            check(component[i] != -1, "Every vertex belongs to an SCC");
            for (int j = 0; j < g.n; j++)
                check((component[i] == component[j]) == (reach[i][j] && reach[j][i]), "SCC reachability");
        }
        boolean cycle = false;
        for (Edge e : g.edges) if (reach[e.to()][e.from()]) cycle = true;
        var topo = Connectivity.topological(g);
        check(topo.cycle == cycle, "Cycle detection");
        if (!cycle) {
            check(topo.order.size() == g.n && new HashSet<>(topo.order).size() == g.n, "Topological permutation");
            int[] pos = new int[g.n];
            for (int i = 0; i < g.n; i++) pos[topo.order.get(i)] = i;
            for (Edge e : g.edges) check(pos[e.from()] < pos[e.to()], "Topological edge order");
        } else check(topo.order.isEmpty(), "No order for a cyclic graph");
    }

    private static void checkPaths(Graph g, int source) {
        long inf = Long.MAX_VALUE / 4;
        long[][] expected = new long[g.n][g.n];
        for (int i = 0; i < g.n; i++) {
            Arrays.fill(expected[i], inf);
            expected[i][i] = 0;
            for (Edge e : g.adj.get(i)) expected[i][e.to()] = Math.min(expected[i][e.to()], e.weight());
        }
        for (int k = 0; k < g.n; k++)
            for (int i = 0; i < g.n; i++)
                for (int j = 0; j < g.n; j++)
                    expected[i][j] = Math.min(expected[i][j], expected[i][k] + expected[k][j]);
        var result = Dijkstra.run(g, source);
        for (int v = 0; v < g.n; v++) {
            long distance = expected[source][v] == inf ? Long.MAX_VALUE : expected[source][v];
            check(result.distance[v] == distance, "Distance matches Floyd-Warshall");
            var path = result.pathTo(v);
            if (distance == Long.MAX_VALUE) check(path.isEmpty(), "Unreachable path");
            else {
                check(path.get(0) == source && path.get(path.size() - 1) == v, "Path endpoints");
                long weight = 0;
                for (int i = 1; i < path.size(); i++) {
                    int a = path.get(i - 1), b = path.get(i), best = Integer.MAX_VALUE;
                    for (Edge e : g.adj.get(a)) if (e.to() == b) best = Math.min(best, e.weight());
                    check(best != Integer.MAX_VALUE, "Path edge exists");
                    weight += best;
                }
                check(weight == distance, "Path weight");
            }
        }
    }

    public static void main(String[] args) {
        DSU dsu = new DSU(4);
        check(dsu.union(0, 1) && dsu.union(2, 3) && dsu.union(1, 3), "DSU merges");
        check(!dsu.union(0, 2) && dsu.find(0) == dsu.find(3), "DSU repeated merge");
        Graph equal = new Graph(4, false);
        equal.addEdge(0, 1, 1); equal.addEdge(1, 2, 1);
        equal.addEdge(2, 3, 1); equal.addEdge(3, 0, 1); equal.addEdge(0, 2, 1);
        checkMst(equal, 3, 1);
        Graph disconnected = new Graph(4, false);
        disconnected.addEdge(0, 1, 2); disconnected.addEdge(2, 3, 3);
        checkMst(disconnected, 5, 2);
        checkPaths(disconnected, 0);
        checkMst(new Graph(1, false), 0, 1);
        checkPaths(new Graph(1, false), 0);
        checkDirected(new Graph(1, true));
        Graph self = new Graph(1, true); self.addEdge(0, 0, 0); checkDirected(self);
        Graph known = new Graph(5, true);
        known.addEdge(0, 1, 2); known.addEdge(1, 0, 2);
        known.addEdge(1, 2, 0); known.addEdge(2, 3, 1); known.addEdge(3, 2, 1);
        checkDirected(known); checkPaths(known, 0);
        Graph negative = new Graph(2, true); negative.addEdge(0, 1, -1);
        boolean rejected = false;
        try { Dijkstra.run(negative, 0); } catch (IllegalArgumentException e) { rejected = true; }
        check(rejected, "Negative weights rejected");
        Random random = new Random(17);
        for (int trial = 0; trial < 30; trial++) {
            Graph g = new Graph(8, true);
            for (int a = 0; a < 8; a++) for (int b = 0; b < 8; b++)
                if (a != b && random.nextInt(5) == 0) g.addEdge(a, b, random.nextInt(10));
            checkDirected(g); checkPaths(g, trial % 8);
            checkDirected(Graph.generate(10, 30, "dag", trial));
            Graph u = Graph.generate(10, 30, "undirected", trial);
            checkMst(u, MST.kruskal(u).weight, 1);
            checkPaths(u, 0);
        }
        System.out.println("PASS: " + checks + " correctness checks");
    }
}
