import java.util.*;

class Aresta implements Comparable<Aresta> {
    int origem, destino, peso;

    public Aresta(int origem, int destino, int peso) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
    }

    @Override
    public int compareTo(Aresta outra) {
        return Integer.compare(this.peso, outra.peso);
    }
}