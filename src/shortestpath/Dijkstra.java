package shortestpath;

import graph.*;
import java.util.*;

public class Dijkstra {
    private record State(int vertex, long distance) { }
    public static class Result {
        public long[] distance;
        public int[] parent;
        public long examined, comparisons;
        public List<Integer> pathTo(int target) {
            List<Integer> path = new ArrayList<>();
            if (distance[target] == Long.MAX_VALUE) return path;
            for (int v = target; v != -1; v = parent[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, int source) {
        if (source < 0 || source >= g.n) throw new IllegalArgumentException("Invalid source");
        Result r = new Result();
        for (Edge e : g.edges) {
            r.examined++;
            if (e.weight() < 0) throw new IllegalArgumentException("Negative weight");
        }
        r.distance = new long[g.n];
        r.parent = new int[g.n];
        Arrays.fill(r.distance, Long.MAX_VALUE);
        Arrays.fill(r.parent, -1);
        r.distance[source] = 0;
        PriorityQueue<State> queue = new PriorityQueue<>((a, b) -> {
            r.comparisons++;
            return Long.compare(a.distance(), b.distance());
        });
        queue.add(new State(source, 0));
        while (!queue.isEmpty()) {
            State current = queue.poll();
            int v = current.vertex();
            if (current.distance() != r.distance[v]) continue;
            for (Edge e : g.adj.get(v)) {
                r.examined++;
                long next = r.distance[v] + e.weight();
                if (next < r.distance[e.to()]) {
                    r.distance[e.to()] = next;
                    r.parent[e.to()] = v;
                    queue.add(new State(e.to(), next));
                }
            }
        }
        return r;
    }
}
