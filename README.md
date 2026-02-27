<!--
████████████████████████████████████████████████████████████████████████████
  QUIRSON FERNANDO NGALE — GRAPH ALGORITHMS
  Trabalho Escolar · ISCTEM · Computer Engineering
  Kruskal · Prim · Dijkstra · BFS · DFS · Bellman-Ford · Floyd-Warshall
████████████████████████████████████████████████████████████████████████████
-->

<!-- HEADER WAVE -->
<img src="https://capsule-render.vercel.app/api?type=waving&color=0:04080f,40:001a33,100:00c8ff&height=200&section=header&text=Graph%20Algorithms&fontSize=58&fontColor=ffffff&fontAlignY=38&desc=Kruskal%20%C2%B7%20Prim%20%C2%B7%20Dijkstra%20%C2%B7%20BFS%20%C2%B7%20DFS%20%C2%B7%20Bellman-Ford&descSize=16&descAlignY=62&animation=fadeIn" width="100%"/>

<div align="center">

[![Language](https://img.shields.io/badge/Language-Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white&labelColor=04080f)](https://www.java.com)
[![IDE](https://img.shields.io/badge/IDE-IntelliJ_IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white&labelColor=04080f)](https://www.jetbrains.com/idea/)
[![Subject](https://img.shields.io/badge/Subject-Graph_Theory-00c8ff?style=for-the-badge&labelColor=04080f)]()
[![Institution](https://img.shields.io/badge/University-ISCTEM-00f5a0?style=for-the-badge&labelColor=04080f)]()
[![Author](https://img.shields.io/badge/Author-Quirson_Ngale-ffffff?style=for-the-badge&labelColor=04080f)](https://github.com/Quirson)

</div>

---

## `$ tree ./algoritmos`

```
Algoritmos/
├── src/
│   ├── kruskal/          # Minimum Spanning Tree — greedy edge sorting
│   ├── prim/             # Minimum Spanning Tree — greedy vertex expansion
│   ├── dijkstra/         # Shortest path — single source, non-negative weights
│   ├── bfs/              # Breadth-First Search — level-order traversal
│   ├── dfs/              # Depth-First Search — recursive/stack traversal
│   ├── bellman_ford/     # Shortest path — handles negative weights
│   ├── floyd_warshall/   # All-pairs shortest path — dynamic programming
│   └── utils/            # Graph representation, Union-Find, Priority Queue
├── .idea/
├── Algoritmos.iml
└── README.md
```

---

## `$ man algorithms`

> Algoritmos de **Teoria dos Grafos** implementados em Java como trabalho académico da cadeira de Estruturas de Dados e Algoritmos no **ISCTEM — Instituto Superior de Ciências e Tecnologia de Moçambique**.
>
> Cada algoritmo inclui implementação completa, análise de complexidade e casos de teste.

---

## `$ cat ALGORITHMS.md`

---

### 🌿 Kruskal — *Minimum Spanning Tree*

```
Estratégia: Greedy — ordenar todas as arestas por peso e adicionar
            as mais baratas que não formem ciclo (Union-Find)
```

```
Grafo de exemplo:          MST resultado (Kruskal):

  A ---4--- B               A ---4--- B
  |  \      |               |         |
  7    2    5       →            2    
  |      \  |                      \  
  D ---9--- C               D       C
                                \
                              1 aresta mais barata
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(E log E)` — dominado pela ordenação das arestas |
| 💾 Espaço | `O(V + E)` |
| ✅ Melhor para | Grafos esparsos (poucas arestas) |

```java
// Kruskal — núcleo do algoritmo
Collections.sort(edges, (a, b) -> a.weight - b.weight); // sort by weight
for (Edge edge : edges) {
    int rootU = find(parent, edge.u);
    int rootV = find(parent, edge.v);
    if (rootU != rootV) {          // não forma ciclo?
        mst.add(edge);             // inclui na MST
        union(parent, rank, rootU, rootV);
    }
}
```

---

### 🌱 Prim — *Minimum Spanning Tree*

```
Estratégia: Greedy — começar num vértice, sempre expandir para
            o vizinho mais barato ainda não visitado (Priority Queue)
```

```
Iteração 1: Start A       Iteração 2: Add C(2)     MST Final:
  [A]                       [A]—2—[C]              A—2—C—5—B
   |                         |                      |
   expand cheapest            expand cheapest        4—D
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(E log V)` com Priority Queue |
| 💾 Espaço | `O(V)` |
| ✅ Melhor para | Grafos densos (muitas arestas) |

```java
// Prim — núcleo do algoritmo
PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[1]-b[1]);
pq.add(new int[]{start, 0});
while (!pq.isEmpty()) {
    int[] curr = pq.poll();
    if (visited[curr[0]]) continue;
    visited[curr[0]] = true;
    mstCost += curr[1];
    for (int[] neighbor : graph[curr[0]])
        if (!visited[neighbor[0]])
            pq.add(neighbor);       // adiciona vizinhos
}
```

---

### 🗺️ Dijkstra — *Shortest Path (Single Source)*

```
Estratégia: Greedy — relaxamento de arestas, sempre processa
            o vértice com menor distância acumulada
            ⚠️ Não funciona com pesos negativos!
```

```
          2         Distâncias desde A:
    A ——————— B      A = 0
    |    \    |      B = 2
    6     4   1  →  C = 4 (via A→B→C)
    |        \|      D = 6 (via A→D)
    D ————————C      
          3
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O((V + E) log V)` com heap |
| 💾 Espaço | `O(V)` |
| ✅ Melhor para | Menor caminho com pesos não-negativos |

```java
// Dijkstra — relaxamento de arestas
int[] dist = new int[V];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;
PriorityQueue<int[]> pq = new PriorityQueue<>((a,b) -> a[1]-b[1]);
pq.add(new int[]{src, 0});
while (!pq.isEmpty()) {
    int u = pq.poll()[0];
    for (int[] v : adj[u])
        if (dist[u] + v[1] < dist[v[0]]) {
            dist[v[0]] = dist[u] + v[1];  // relaxa
            pq.add(new int[]{v[0], dist[v[0]]});
        }
}
```

---

### 🔵 BFS — *Breadth-First Search*

```
Estratégia: Explorar nível a nível usando uma fila (Queue)
            Garante o caminho mais curto em grafos não-ponderados
```

```
Grafo:           Ordem BFS desde A:
   A             Nível 0: A
  / \            Nível 1: B, C
 B   C     →    Nível 2: D, E, F
/ \   \
D  E   F
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(V + E)` |
| 💾 Espaço | `O(V)` |
| ✅ Melhor para | Menor nº de saltos, componentes conectados |

```java
// BFS
Queue<Integer> queue = new LinkedList<>();
boolean[] visited = new boolean[V];
queue.add(start);
visited[start] = true;
while (!queue.isEmpty()) {
    int node = queue.poll();
    for (int neighbor : adj[node])
        if (!visited[neighbor]) {
            visited[neighbor] = true;
            queue.add(neighbor);
        }
}
```

---

### 🔴 DFS — *Depth-First Search*

```
Estratégia: Explorar o mais fundo possível antes de retroceder
            Implementação recursiva ou com Stack
```

```
Grafo:           Ordem DFS desde A:
   A             A → B → D → E → C → F
  / \            (vai fundo antes de voltar)
 B   C     →    
/ \   \         Útil para: ciclos, componentes, topological sort
D  E   F
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(V + E)` |
| 💾 Espaço | `O(V)` — stack de recursão |
| ✅ Melhor para | Deteção de ciclos, ordenação topológica |

```java
// DFS recursivo
void dfs(int node, boolean[] visited, List<List<Integer>> adj) {
    visited[node] = true;
    for (int neighbor : adj.get(node))
        if (!visited[neighbor])
            dfs(neighbor, visited, adj);
}
```

---

### ⚡ Bellman-Ford — *Shortest Path com Pesos Negativos*

```
Estratégia: Relaxar TODAS as arestas V-1 vezes
            Detecta ciclos negativos (impossível em Dijkstra)
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(V × E)` — mais lento que Dijkstra |
| 💾 Espaço | `O(V)` |
| ✅ Melhor para | Grafos com pesos negativos, deteção de ciclos negativos |

```java
// Bellman-Ford — V-1 iterações de relaxamento
int[] dist = new int[V];
Arrays.fill(dist, Integer.MAX_VALUE);
dist[src] = 0;
for (int i = 0; i < V - 1; i++)
    for (Edge e : edges)
        if (dist[e.u] != Integer.MAX_VALUE && dist[e.u] + e.w < dist[e.v])
            dist[e.v] = dist[e.u] + e.w;
// Verificar ciclo negativo
for (Edge e : edges)
    if (dist[e.u] + e.w < dist[e.v])
        System.out.println("Ciclo negativo detetado!");
```

---

### 🔷 Floyd-Warshall — *All-Pairs Shortest Path*

```
Estratégia: Programação Dinâmica — para cada par (i,j),
            testar se passar pelo vértice k melhora o caminho
```

| | Complexidade |
|---|---|
| ⏱️ Tempo | `O(V³)` — triplicado, mas simples |
| 💾 Espaço | `O(V²)` — matriz de distâncias |
| ✅ Melhor para | Menor caminho entre TODOS os pares de vértices |

```java
// Floyd-Warshall
for (int k = 0; k < V; k++)         // vértice intermediário
    for (int i = 0; i < V; i++)     // origem
        for (int j = 0; j < V; j++) // destino
            if (dist[i][k] + dist[k][j] < dist[i][j])
                dist[i][j] = dist[i][k] + dist[k][j];
```

---

## `$ compare --all-algorithms`

<div align="center">

| Algoritmo | Tipo | Tempo | Espaço | Pesos Neg. | Uso Principal |
|-----------|------|-------|--------|-----------|---------------|
| **Kruskal** | MST | `O(E log E)` | `O(V+E)` | ❌ | Árvore geradora mínima — grafo esparso |
| **Prim** | MST | `O(E log V)` | `O(V)` | ❌ | Árvore geradora mínima — grafo denso |
| **Dijkstra** | Caminho | `O((V+E) log V)` | `O(V)` | ❌ | Menor caminho de fonte única |
| **BFS** | Traversal | `O(V+E)` | `O(V)` | — | Caminho mais curto (não-ponderado) |
| **DFS** | Traversal | `O(V+E)` | `O(V)` | — | Ciclos, componentes, topo. sort |
| **Bellman-Ford** | Caminho | `O(V×E)` | `O(V)` | ✅ | Menor caminho com pesos negativos |
| **Floyd-Warshall** | All-Pairs | `O(V³)` | `O(V²)` | ✅ | Todos os pares de menores caminhos |

</div>

---

## `$ run --requirements`

```bash
# Requisitos
Java 11+
IntelliJ IDEA (recomendado)

# Clonar e executar
git clone https://github.com/Quirson/Algoritmos.git
cd Algoritmos
# Abrir com IntelliJ IDEA ou compilar manualmente:
javac src/**/*.java
java src.Main
```

---

## `$ git log --author`

<div align="center">

**Desenvolvido por [Quirson Fernando Ngale](https://github.com/Quirson)**

[![Portfolio](https://img.shields.io/badge/Portfolio-quirsonngale.dev-00c8ff?style=for-the-badge&labelColor=04080f)](https://www.quirsonngale.dev)
[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white&labelColor=04080f)](https://www.linkedin.com/in/quirson-fernando-ngale)
[![GitHub](https://img.shields.io/badge/GitHub-@Quirson-ffffff?style=for-the-badge&logo=github&logoColor=white&labelColor=04080f)](https://github.com/Quirson)

*Trabalho académico — Engenharia Informática · ISCTEM · Maputo, Moçambique 🇲🇿*

</div>

<!-- FOOTER WAVE -->
<img src="https://capsule-render.vercel.app/api?type=waving&color=0:00c8ff,50:001a33,100:04080f&height=120&section=footer" width="100%"/>
