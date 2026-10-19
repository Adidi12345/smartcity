package dsu;

public class DSU {
    private final int[] parent, rank;
    public long finds, unions;

    public DSU(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        finds++;
        if (parent[x] != x) parent[x] = find(parent[x]); // Path compression
        return parent[x];
    }

    public boolean union(int a, int b) {
        unions++;
        int x = find(a), y = find(b);
        if (x == y) return false;
        if (rank[x] < rank[y]) parent[x] = y;
        else {
            parent[y] = x;
            if (rank[x] == rank[y]) rank[x]++;
        }
        return true;
    }
}
