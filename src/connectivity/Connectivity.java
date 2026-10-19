package connectivity;

import graph.*;
import java.util.*;

public class Connectivity {
    public static class Result {
        public long examined;
        public boolean cycle;
        public final List<Integer> order = new ArrayList<>();
        public final List<List<Integer>> components = new ArrayList<>();
    }

    public static Result topological(Graph g) {
        if (!g.directed) throw new IllegalArgumentException("Directed graph required");
        Result r = new Result();
        int[] degree = new int[g.n];
        for (Edge e : g.edges) { degree[e.to()]++; r.examined++; }
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < g.n; i++) if (degree[i] == 0) queue.add(i);
        while (!queue.isEmpty()) {
            int v = queue.remove();
            r.order.add(v);
            for (Edge e : g.adj.get(v)) {
                r.examined++;
                if (--degree[e.to()] == 0) queue.add(e.to());
            }
        }
        r.cycle = r.order.size() != g.n;
        if (r.cycle) r.order.clear();
        return r;
    }

    public static Result kosaraju(Graph g) {
        if (!g.directed) throw new IllegalArgumentException("Directed graph required");
        Result r = new Result();
        boolean[] visited = new boolean[g.n];
        List<Integer> finish = new ArrayList<>();
        for (int i = 0; i < g.n; i++) if (!visited[i]) dfs(i, g.adj, visited, finish, r);
        List<List<Edge>> reverse = new ArrayList<>();
        for (int i = 0; i < g.n; i++) reverse.add(new ArrayList<>());
        for (Edge e : g.edges) {
            reverse.get(e.to()).add(new Edge(e.to(), e.from(), e.weight()));
            r.examined++;
        }
        Arrays.fill(visited, false);
        for (int i = finish.size() - 1; i >= 0; i--) {
            int v = finish.get(i);
            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();
                dfs(v, reverse, visited, component, r);
                r.components.add(component);
            }
        }
        return r;
    }

    private static void dfs(int v, List<List<Edge>> adj, boolean[] visited,
                            List<Integer> output, Result r) {
        visited[v] = true;
        for (Edge e : adj.get(v)) {
            r.examined++;
            if (!visited[e.to()]) dfs(e.to(), adj, visited, output, r);
        }
        output.add(v);
    }
}
