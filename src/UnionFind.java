class UnionFind {
    private int[] pai, rank;

    public UnionFind(int n) {
        pai = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            pai[i] = i;
            rank[i] = 0;
        }
    }

    public int find(int x) {
        if (pai[x] != x) {
            pai[x] = find(pai[x]); // Compressão de caminho
        }
        return pai[x];
    }

    public boolean union(int x, int y) {
        int raizX = find(x);
        int raizY = find(y);

        if (raizX == raizY) return false;

        // União por rank
        if (rank[raizX] < rank[raizY]) {
            pai[raizX] = raizY;
        } else if (rank[raizX] > rank[raizY]) {
            pai[raizY] = raizX;
        } else {
            pai[raizY] = raizX;
            rank[raizX]++;
        }
        return true;
    }
}