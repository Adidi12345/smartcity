import graph.Graph;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;
import java.nio.file.*;
import java.io.*;
import java.util.Locale;

public class Benchmark {
    private record Sample(long edges, long comparisons, long finds, long unions) { }
    private static volatile Object sink;

    private static Sample measure(String name, Graph g) {
        switch (name) {
            case "Kruskal", "Prim":
                var m = name.equals("Kruskal") ? MST.kruskal(g) : MST.prim(g);
                sink = m;
                return new Sample(m.examined, m.comparisons, m.finds, m.unions);
            case "Dijkstra":
                var d = Dijkstra.run(g, 0);
                sink = d;
                return new Sample(d.examined, d.comparisons, 0, 0);
            default:
                var c = name.equals("SCC") ? Connectivity.kosaraju(g) : Connectivity.topological(g);
                sink = c;
                return new Sample(c.examined, 0, 0, 0);
        }
    }

    public static void run() throws IOException {
        Files.createDirectories(Path.of("results/tables"));
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        try (PrintWriter raw = new PrintWriter("results/tables/runs.csv");
             PrintWriter avg = new PrintWriter("results/tables/averages.csv")) {
            raw.println("algorithm,V,E,run,nanoseconds,edge_examinations,comparisons,find_calls,union_calls");
            avg.println("algorithm,V,E,average_ns,edge_examinations,comparisons,find_calls,union_calls");
            for (int[] size : sizes) {
                Graph u = Graph.generate(size[0], size[1], "undirected", 42);
                Graph dag = Graph.generate(size[0], size[1], "dag", 42);
                Graph cyclic = Graph.generate(size[0], size[1], "cyclic", 42);
                for (String name : new String[]{"Kruskal", "Prim", "SCC", "Topo-DAG", "Topo-cycle", "Dijkstra"}) {
                    Graph g = name.equals("SCC") || name.equals("Topo-cycle") ? cyclic
                            : name.equals("Topo-DAG") ? dag : u;
                    // Warm up before the five measured runs. Generation is outside timing.
                    for (int w = 0; w < 20; w++) measure(name, g);
                    long total = 0;
                    Sample s = null;
                    for (int run = 1; run <= 5; run++) {
                        long start = System.nanoTime();
                        s = measure(name, g);
                        long elapsed = System.nanoTime() - start;
                        total += elapsed;
                        raw.printf(Locale.US, "%s,%d,%d,%d,%d,%d,%d,%d,%d%n", name, g.n,
                                g.edges.size(), run, elapsed, s.edges, s.comparisons, s.finds, s.unions);
                    }
                    avg.printf(Locale.US, "%s,%d,%d,%.1f,%d,%d,%d,%d%n", name, g.n,
                            g.edges.size(), total / 5.0, s.edges, s.comparisons, s.finds, s.unions);
                }
            }
        }
        System.out.println("Benchmark saved to results/tables/");
    }
}
