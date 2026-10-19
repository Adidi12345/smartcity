package graph;

import java.util.*;

public class Graph {
    public final int n;
    public final boolean directed;
    public final List<Edge> edges = new ArrayList<>();
    public final List<List<Edge>> adj = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 1) throw new IllegalArgumentException("At least one vertex required");
        this.n = n;
        this.directed = directed;
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int from, int to, int weight) {
        if (from < 0 || from >= n || to < 0 || to >= n)
            throw new IllegalArgumentException("Invalid vertex");
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adj.get(from).add(edge);
        if (!directed) adj.get(to).add(new Edge(to, from, weight));
    }

    public static Graph generate(int n, int m, String type, long seed) {
        Graph g = new Graph(n, !type.equals("undirected"));
        long max = type.equals("cyclic") ? (long)n * (n - 1) : (long)n * (n - 1) / 2;
        int min = type.equals("undirected") ? n - 1 : type.equals("cyclic") ? n : 0;
        if (m < min || m > max || (type.equals("cyclic") && n < 2))
            throw new IllegalArgumentException("Invalid graph size");
        Random random = new Random(seed);
        Set<Long> used = new HashSet<>();
        // A chain guarantees connectivity; a directed ring guarantees a cycle.
        if (type.equals("undirected")) {
            for (int i = 1; i < n; i++) addUnique(g, used, i - 1, i, random);
        } else if (type.equals("cyclic")) {
            for (int i = 0; i < n; i++) addUnique(g, used, i, (i + 1) % n, random);
        }
        while (g.edges.size() < m) {
            int a = random.nextInt(n), b = random.nextInt(n);
            if (a == b) continue;
            if (!type.equals("cyclic") && a > b) { int t = a; a = b; b = t; }
            addUnique(g, used, a, b, random);
        }
        return g;
    }

    private static void addUnique(Graph g, Set<Long> used, int a, int b, Random random) {
        if (used.add((long)a * g.n + b)) g.addEdge(a, b, 1 + random.nextInt(100));
    }
}
