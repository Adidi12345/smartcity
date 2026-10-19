import graph.Graph;
import mst.MST;
import connectivity.Connectivity;
import shortestpath.Dijkstra;

public class Main {
    public static void main(String[] args) throws Exception {
        Graph city = new Graph(5, false);
        city.addEdge(0, 1, 4);
        city.addEdge(0, 2, 2);
        city.addEdge(1, 2, 1);
        city.addEdge(1, 3, 5);
        city.addEdge(2, 3, 8);
        city.addEdge(3, 4, 3);
        System.out.println("Districts: 0=Centre, 1=North, 2=West, 3=East, 4=South");
        var k = MST.kruskal(city);
        var p = MST.prim(city);
        System.out.println("Kruskal cost: " + k.weight + ", edges: " + k.edges);
        System.out.println("Prim cost: " + p.weight + ", connected: " + p.connected());
        var paths = Dijkstra.run(city, 0);
        for (int i = 0; i < city.n; i++) {
            System.out.println("To " + i + ": " + (paths.distance[i] == Long.MAX_VALUE
                    ? "unreachable" : paths.distance[i] + " " + paths.pathTo(i)));
        }
        Graph dag = Graph.generate(5, 6, "dag", 42);
        System.out.println("Topological order: " + Connectivity.topological(dag).order);
        Graph roads = Graph.generate(5, 8, "cyclic", 42);
        System.out.println("Cycle detected: " + Connectivity.topological(roads).cycle);
        System.out.println("SCC: " + Connectivity.kosaraju(roads).components);
        if (args.length > 0 && args[0].equals("benchmark")) Benchmark.run();
    }
}
