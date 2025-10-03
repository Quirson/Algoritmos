import java.util.*;
public class Kruskal {

    public static List<Aresta> kruskalMST(List<Aresta> arestas, int numVertices) {
        Collections.sort(arestas);

        // +1 para aceitar vértices de 1 a numVertices
        UnionFind uf = new UnionFind(numVertices + 1);
        List<Aresta> mst = new ArrayList<>();

        for (Aresta aresta : arestas) {
            if (uf.union(aresta.origem, aresta.destino)) {
                mst.add(aresta);

                if (mst.size() == numVertices - 1) {
                    break;
                }
            }
        }

        return mst;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Digite o número de vértices: ");
        int numVertices = sc.nextInt();

        System.out.print("Digite o número de arestas: ");
        int numArestas = sc.nextInt();

        List<Aresta> grafo = new ArrayList<>();

        System.out.println("Digite as arestas no formato: origem destino peso");
        System.out.println("(Vértices podem ser numerados de 1 até " + numVertices + ")");
        for (int i = 0; i < numArestas; i++) {
            System.out.printf("Aresta %d: ", i + 1);
            int origem = sc.nextInt();
            int destino = sc.nextInt();
            int peso = sc.nextInt();

            // Validação básica
            if (origem < 1 || origem > numVertices || destino < 1 || destino > numVertices) {
                System.out.println("ERRO: Vértices devem estar entre 1 e " + numVertices);
                i--; // Repetir esta entrada
                continue;
            }

            grafo.add(new Aresta(origem, destino, peso));
        }

        List<Aresta> mst = kruskalMST(grafo, numVertices);

        System.out.println("\n=== ÁRVORE GERADORA MÍNIMA ===");
        if (mst.size() == numVertices - 1) {
            int pesoTotal = 0;
            for (Aresta aresta : mst) {
                System.out.printf("Vértice %d -- Vértice %d : Peso %d%n",
                        aresta.origem, aresta.destino, aresta.peso);
                pesoTotal += aresta.peso;
            }
            System.out.println("Peso total da MST: " + pesoTotal);
        } else {
            System.out.println("Grafo desconexo! Não é possível formar uma árvore geradora.");
        }

        sc.close();
    }
}