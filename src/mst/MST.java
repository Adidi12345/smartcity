package mst;

import graph.*;
import dsu.DSU;
import java.util.*;

public class MST {
    public static class Result {
        public long weight, examined, comparisons, finds, unions;
        public int components;
        public final List<Edge> edges = new ArrayList<>();
        public boolean connected() { return components == 1; }
    }

    public static Result kruskal(Graph g) {
        if (g.directed) throw new IllegalArgumentException("MST needs an undirected graph");
        Result r = new Result();
        r.components = g.n;
        List<Edge> sorted = new ArrayList<>(g.edges);
        sorted.sort((a, b) -> { r.comparisons++; return Integer.compare(a.weight(), b.weight()); });
        DSU dsu = new DSU(g.n);
        for (Edge e : sorted) {
            r.examined++;
            if (dsu.union(e.from(), e.to())) {
                r.edges.add(e);
                r.weight += e.weight();
                r.components--;
                if (r.components == 1) break;
            }
        }
        r.finds = dsu.finds;
        r.unions = dsu.unions;
        return r;
    }

    public static Result prim(Graph g) {
        if (g.directed) throw new IllegalArgumentException("MST needs an undirected graph");
        Result r = new Result();
        boolean[] visited = new boolean[g.n];
        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            r.comparisons++;
            return Integer.compare(a.weight(), b.weight());
        });
        // Restart for each component so disconnected input returns a spanning forest.
        for (int start = 0; start < g.n; start++) {
            if (visited[start]) continue;
            r.components++;
            queue.add(new Edge(-1, start, 0));
            while (!queue.isEmpty()) {
                Edge current = queue.poll();
                int v = current.to();
                if (visited[v]) continue;
                visited[v] = true;
                r.weight += current.weight();
                if (current.from() != -1) r.edges.add(current);
                for (Edge e : g.adj.get(v)) {
                    r.examined++;
                    if (!visited[e.to()]) queue.add(e);
                }
            }
        }
        return r;
    }
}
