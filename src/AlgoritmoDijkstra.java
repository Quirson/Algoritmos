import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.util.*;

public class AlgoritmoDijkstra extends Application {

    private Canvas canvas;
    private GraphicsContext gc;
    private Graph graph;
    private TextArea logArea;
    private TextArea distancesArea; // Alterado de costLabel
    private Label statusLabel;
    private Button startButton;
    private Button resetButton;
    private Button clearGraphButton;
    private Button removeNodeButton;
    private Button removeEdgeButton;
    private Slider speedSlider;
    private ComboBox<String> startNodeCombo;
    private ToggleButton dragModeButton;
    private ToggleButton addNodeModeButton;
    private ToggleButton addEdgeModeButton;
    private boolean isRunning = false;
    private int animationSpeed = 1000;

    private static final int CANVAS_WIDTH = 810;
    private static final int CANVAS_HEIGHT = 660;
    private static final int NODE_RADIUS = 25;

    // Modos de interação
    private enum InteractionMode { DRAG, ADD_NODE, ADD_EDGE, NONE }
    private InteractionMode currentMode = InteractionMode.NONE;

    // Para drag and drop
    private Node draggedNode = null;
    private double dragOffsetX, dragOffsetY;

    // Para adicionar arestas
    private Node edgeStartNode = null;
    private Node hoveredNode = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Visualizador do Algoritmo de Dijkstra - Caminho Mais Curto");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        StackPane canvasContainer = new StackPane();
        canvasContainer.getChildren().add(canvas);
        canvasContainer.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");
        gc = canvas.getGraphicsContext2D();

        setupCanvasInteraction();
        root.setCenter(canvasContainer);

        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);

        HBox bottomPanel = createBottomPanel();
        root.setBottom(bottomPanel);

        initializeExampleGraph();
        drawGraph();

        Scene scene = new Scene(root, 1200, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createTopPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #4CAF50; -fx-border-width: 2;");

        Label titleLabel = new Label("Algoritmo de Dijkstra - Caminho Mais Curto");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        // Painel de modos de interação
        HBox modesBox = new HBox(10);
        modesBox.setAlignment(Pos.CENTER_LEFT);

        Label modeLabel = new Label("Modo:");
        modeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ToggleGroup modeGroup = new ToggleGroup();

        dragModeButton = new ToggleButton("Arrastar Nós");
        addNodeModeButton = new ToggleButton("Adicionar Nó");
        addEdgeModeButton = new ToggleButton("Adicionar Aresta");

        dragModeButton.setToggleGroup(modeGroup);
        addNodeModeButton.setToggleGroup(modeGroup);
        addEdgeModeButton.setToggleGroup(modeGroup);

        dragModeButton.setOnAction(e -> currentMode = dragModeButton.isSelected() ? InteractionMode.DRAG : InteractionMode.NONE);
        addNodeModeButton.setOnAction(e -> currentMode = addNodeModeButton.isSelected() ? InteractionMode.ADD_NODE : InteractionMode.NONE);
        addEdgeModeButton.setOnAction(e -> {
            currentMode = addEdgeModeButton.isSelected() ? InteractionMode.ADD_EDGE : InteractionMode.NONE;
            edgeStartNode = null;
        });

        modesBox.getChildren().addAll(modeLabel, dragModeButton, addNodeModeButton, addEdgeModeButton);

        // Painel de controles de grafo
        HBox graphControlsBox = new HBox(10);
        graphControlsBox.setAlignment(Pos.CENTER_LEFT);

        clearGraphButton = new Button("Limpar Grafo");
        clearGraphButton.setStyle("-fx-background-color: #9E9E9E; -fx-text-fill: white; -fx-font-weight: bold;");
        clearGraphButton.setOnAction(e -> clearGraph());

        Button loadExampleButton = new Button("Carregar Exemplo");
        loadExampleButton.setOnAction(e -> loadExampleGraph());

        removeNodeButton = new Button("Remover Nó");
        removeNodeButton.setOnAction(e -> removeNodeDialog());

        removeEdgeButton = new Button("Remover Aresta");
        removeEdgeButton.setOnAction(e -> removeEdgeDialog());

        graphControlsBox.getChildren().addAll(clearGraphButton, loadExampleButton, removeNodeButton, removeEdgeButton);

        // Painel de controles do algoritmo
        HBox controlsBox = new HBox(15);
        controlsBox.setAlignment(Pos.CENTER_LEFT);

        startNodeCombo = new ComboBox<>();
        startNodeCombo.setPromptText("Nó inicial");

        startButton = new Button("Iniciar Dijkstra");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setOnAction(e -> runDijkstraAlgorithm());

        resetButton = new Button("Resetar");
        resetButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        resetButton.setOnAction(e -> resetGraph());

        Label speedLabel = new Label("Velocidade:");
        speedSlider = new Slider(100, 2000, 1000);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(500);
        speedSlider.setPrefWidth(200);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            animationSpeed = 2100 - newVal.intValue();
        });

        controlsBox.getChildren().addAll(
                new Label("Nó Inicial:"), startNodeCombo,
                startButton, resetButton,
                speedLabel, speedSlider
        );

        panel.getChildren().addAll(titleLabel, modesBox, graphControlsBox, controlsBox);
        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #4CAF50; -fx-border-width: 2;");

        Label logLabel = new Label("Log de Execução:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(250);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11;");

        Label distancesLabel = new Label("Distâncias Mais Curtas:");
        distancesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        distancesLabel.setStyle("-fx-text-fill: #2196F3;");

        distancesArea = new TextArea("Resultados aparecerão aqui.");
        distancesArea.setEditable(false);
        distancesArea.setPrefHeight(150);
        distancesArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12;");


        Label infoLabel = new Label("Informações:");
        infoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPrefHeight(300);
        infoArea.setWrapText(true);
        infoArea.setText(
                "MODOS DE INTERAÇÃO:\n\n" +
                        "• Arrastar: Clique e arraste os nós\n" +
                        "• Adicionar Nó: Clique no canvas\n" +
                        "• Adicionar Aresta: Clique em dois nós\n\n" +
                        "ALGORITMO DE DIJKSTRA:\n\n" +
                        "1. Defina a distância do nó inicial como 0 e ∞ para os outros\n" +
                        "2. Marque todos os nós como não visitados\n" +
                        "3. Selecione o nó não visitado com a menor distância\n" +
                        "4. Para este nó, calcule a distância para seus vizinhos\n" +
                        "5. Se a nova distância for menor, atualize-a\n" +
                        "6. Repita até visitar todos os nós\n\n" +
                        "LEGENDA:\n" +
                        "• Verde: Nó visitado (caminho finalizado)\n" +
                        "• Azul: Aresta no caminho mais curto\n" +
                        "• Vermelho: Aresta sendo avaliada (relaxamento)\n" +
                        "• Amarelo: Nó atual na fila de prioridade\n" +
                        "• Cinza: Não processado"
        );

        panel.getChildren().addAll(logLabel, logArea, distancesLabel, distancesArea, infoLabel, infoArea);
        return panel;
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #4CAF50; -fx-border-width: 2;");

        statusLabel = new Label("Status: Pronto para iniciar");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        panel.getChildren().add(statusLabel);
        return panel;
    }

    private void setupCanvasInteraction() {
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        canvas.setOnMouseMoved(this::handleMouseMoved);
    }

    private void handleMousePressed(MouseEvent e) {
        if (isRunning) return;

        double x = e.getX();
        double y = e.getY();
        Node clickedNode = getNodeAt(x, y);

        switch (currentMode) {
            case DRAG:
                if (clickedNode != null) {
                    draggedNode = clickedNode;
                    dragOffsetX = x - clickedNode.x;
                    dragOffsetY = y - clickedNode.y;
                }
                break;

            case ADD_NODE:
                if (clickedNode == null) {
                    addNodeAt(x, y);
                }
                break;

            case ADD_EDGE:
                if (clickedNode != null) {
                    if (edgeStartNode == null) {
                        edgeStartNode = clickedNode;
                        clickedNode.isSelected = true;
                        drawGraph();
                        log("Primeiro nó selecionado: " + clickedNode.label);
                    } else if (edgeStartNode != clickedNode) {
                        addEdgeBetweenNodes(edgeStartNode, clickedNode);
                        edgeStartNode.isSelected = false;
                        edgeStartNode = null;
                        drawGraph();
                    }
                }
                break;
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (currentMode == InteractionMode.DRAG && draggedNode != null) {
            draggedNode.x = e.getX() - dragOffsetX;
            draggedNode.y = e.getY() - dragOffsetY;
            draggedNode.x = Math.max(NODE_RADIUS, Math.min(CANVAS_WIDTH - NODE_RADIUS, draggedNode.x));
            draggedNode.y = Math.max(NODE_RADIUS, Math.min(CANVAS_HEIGHT - NODE_RADIUS, draggedNode.y));
            drawGraph();
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        draggedNode = null;
    }

    private void handleMouseMoved(MouseEvent e) {
        Node newHoveredNode = getNodeAt(e.getX(), e.getY());
        if (newHoveredNode != hoveredNode) {
            hoveredNode = newHoveredNode;
            drawGraph();
        }
    }

    private Node getNodeAt(double x, double y) {
        for (Node node : graph.nodes) {
            double dx = node.x - x;
            double dy = node.y - y;
            if (Math.sqrt(dx * dx + dy * dy) <= NODE_RADIUS) {
                return node;
            }
        }
        return null;
    }

    private void addNodeAt(double x, double y) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf((char)('A' + graph.nodes.size())));
        dialog.setTitle("Adicionar Nó");
        dialog.setHeaderText("Digite o nome do nó:");
        dialog.setContentText("Nome:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(label -> {
            if (!label.trim().isEmpty() && graph.getNodeByLabel(label) == null) {
                Node node = new Node(label.trim(), x, y);
                graph.addNode(node);
                updateStartNodeCombo();
                drawGraph();
                log("Nó '" + label + "' adicionado");
                updateStatus();
            } else {
                showAlert("Erro", "Nome inválido ou já existe!");
            }
        });
    }

    private void addEdgeBetweenNodes(Node from, Node to) {
        if (graph.hasEdge(from, to)) {
            showAlert("Aviso", "Aresta já existe entre " + from.label + " e " + to.label);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Adicionar Aresta");
        dialog.setHeaderText("Aresta: " + from.label + " ↔ " + to.label);
        dialog.setContentText("Peso:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(weightStr -> {
            try {
                int weight = Integer.parseInt(weightStr.trim());
                if (weight > 0) {
                    graph.addEdge(from, to, weight);
                    drawGraph();
                    log("Aresta adicionada: " + from.label + "-" + to.label + " (peso: " + weight + ")");
                    updateStatus();
                } else {
                    showAlert("Erro", "O peso deve ser maior que zero!");
                }
            } catch (NumberFormatException e) {
                showAlert("Erro", "Digite um número válido!");
            }
        });
    }

    private void removeNodeDialog() {
        if (graph.nodes.isEmpty()) {
            showAlert("Aviso", "Não há nós para remover!");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                graph.nodes.get(0).label,
                graph.nodes.stream().map(n -> n.label).toArray(String[]::new)
        );
        dialog.setTitle("Remover Nó");
        dialog.setHeaderText("Selecione o nó para remover:");
        dialog.setContentText("Nó:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(label -> {
            Node node = graph.getNodeByLabel(label);
            if (node != null) {
                graph.removeNode(node);
                updateStartNodeCombo();
                drawGraph();
                log("Nó '" + label + "' removido");
                updateStatus();
            }
        });
    }

    private void removeEdgeDialog() {
        if (graph.edges.isEmpty()) {
            showAlert("Aviso", "Não há arestas para remover!");
            return;
        }

        List<String> edgeLabels = new ArrayList<>();
        for (Edge edge : graph.edges) {
            edgeLabels.add(edge.from.label + " - " + edge.to.label + " (peso: " + edge.weight + ")");
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(edgeLabels.get(0), edgeLabels);
        dialog.setTitle("Remover Aresta");
        dialog.setHeaderText("Selecione a aresta para remover:");
        dialog.setContentText("Aresta:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(edgeLabel -> {
            int index = edgeLabels.indexOf(edgeLabel);
            if (index >= 0) {
                Edge edge = graph.edges.get(index);
                graph.edges.remove(edge);
                drawGraph();
                log("Aresta removida: " + edge.from.label + "-" + edge.to.label);
                updateStatus();
            }
        });
    }

    private void clearGraph() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Limpar Grafo");
        alert.setHeaderText("Tem certeza que deseja limpar todo o grafo?");
        alert.setContentText("Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            graph = new Graph();
            updateStartNodeCombo();
            drawGraph();
            logArea.clear();
            distancesArea.setText("Resultados aparecerão aqui.");
            log("Grafo limpo. Você pode criar um novo grafo.");
            updateStatus();
        }
    }

    private void loadExampleGraph() {
        resetGraph(); // Limpa estado anterior antes de carregar
        clearGraph();
        initializeExampleGraph();
        drawGraph();
        log("Grafo de exemplo carregado!");
    }

    private void initializeExampleGraph() {
        graph = new Graph();

        Node nA = new Node("A", 100, 300);
        Node nB = new Node("B", 300, 150);
        Node nC = new Node("C", 300, 450);
        Node nD = new Node("D", 500, 150);
        Node nE = new Node("E", 500, 450);
        Node nF = new Node("F", 700, 300);

        graph.addNode(nA);
        graph.addNode(nB);
        graph.addNode(nC);
        graph.addNode(nD);
        graph.addNode(nE);
        graph.addNode(nF);

        graph.addEdge(nA, nB, 7);
        graph.addEdge(nA, nC, 9);
        graph.addEdge(nA, nF, 14);
        graph.addEdge(nB, nC, 10);
        graph.addEdge(nB, nD, 15);
        graph.addEdge(nC, nE, 11);
        graph.addEdge(nC, nF, 2);
        graph.addEdge(nD, nE, 6);
        graph.addEdge(nE, nF, 9);


        updateStartNodeCombo();
    }

    private void updateStartNodeCombo() {
        startNodeCombo.getItems().clear();
        for (Node node : graph.nodes) {
            startNodeCombo.getItems().add(node.label);
        }
        if (!graph.nodes.isEmpty()) {
            startNodeCombo.getSelectionModel().selectFirst();
        }
    }

    private void resetGraph() {
        isRunning = false;
        graph.reset();
        edgeStartNode = null;
        drawGraph();
        logArea.clear();
        distancesArea.setText("Resultados aparecerão aqui.");
        statusLabel.setText("Status: Grafo resetado");
        startButton.setDisable(false);
    }

    private void runDijkstraAlgorithm() {
        if (isRunning) return;
        if (graph.nodes.isEmpty()) {
            showAlert("Aviso", "Adicione nós ao grafo primeiro!");
            return;
        }

        String startLabel = startNodeCombo.getValue();
        if (startLabel == null) {
            showAlert("Aviso", "Selecione um nó inicial!");
            return;
        }

        resetGraph(); // Garante que o estado está limpo antes de começar
        isRunning = true;
        startButton.setDisable(true);
        logArea.clear();
        log("=== INICIANDO ALGORITMO DE DIJKSTRA ===");
        log("Nó inicial: " + startLabel);
        log("");

        new Thread(() -> {
            try {
                dijkstraAlgorithm(startLabel);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    isRunning = false;
                    startButton.setDisable(false);
                });
            }
        }).start();
    }

    private void dijkstraAlgorithm(String startLabel) throws InterruptedException {
        Node startNode = graph.getNodeByLabel(startLabel);
        if (startNode == null) return;

        // Inicialização
        for (Node node : graph.nodes) {
            node.distance = Integer.MAX_VALUE;
            node.predecessor = null;
        }
        startNode.distance = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        pq.add(startNode);

        while (!pq.isEmpty()) {
            Node currentNode = pq.poll();

            if (currentNode.isVisited) {
                continue;
            }
            currentNode.isVisited = true;
            currentNode.isSelected = true; // Para destacar o nó atual

            Platform.runLater(() -> {
                drawGraph();
                log("Visitando nó: " + currentNode.label + " (Distância: " + currentNode.distance + ")");
                statusLabel.setText("Status: Processando " + currentNode.label);
            });
            Thread.sleep(animationSpeed);

            for (Edge edge : graph.getEdgesOf(currentNode)) {
                Node neighbor = (edge.from == currentNode) ? edge.to : edge.from;

                if (!neighbor.isVisited) {
                    edge.isEvaluating = true;
                    Platform.runLater(this::drawGraph);
                    Thread.sleep(animationSpeed / 2);

                    int newDist = currentNode.distance + edge.weight;
                    if (newDist < neighbor.distance) {
                        neighbor.distance = newDist;
                        neighbor.predecessor = currentNode;
                        pq.add(neighbor); // Adiciona com a nova prioridade

                        final String neighborLabel = neighbor.label;
                        Platform.runLater(() -> {
                            log("  → Relaxando " + neighborLabel + ". Nova distância: " + newDist);
                            drawGraph();
                        });
                        Thread.sleep(animationSpeed / 2);
                    } else {
                        final String neighborLabel = neighbor.label;
                        Platform.runLater(() -> log("  → Aresta para " + neighborLabel + " não melhora o caminho."));
                    }

                    edge.isEvaluating = false;
                    Platform.runLater(this::drawGraph);
                }
            }
            currentNode.isSelected = false; // Desmarcar
        }

        highlightShortestPaths();

        Platform.runLater(() -> {
            drawGraph();
            log("\n=== ALGORITMO CONCLUÍDO ===");
            statusLabel.setText("Status: Concluído!");
            updateDistancesArea();
        });
    }

    private void highlightShortestPaths() {
        for(Node node : graph.nodes){
            if(node.predecessor != null){
                Edge pathEdge = graph.getEdgeBetween(node, node.predecessor);
                if(pathEdge != null){
                    pathEdge.isPath = true;
                }
            }
        }
    }

    private void updateDistancesArea() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Distâncias a partir de '%s':\n", startNodeCombo.getValue()));

        graph.nodes.sort(Comparator.comparing(n -> n.label));

        for (Node node : graph.nodes) {
            String dist = (node.distance == Integer.MAX_VALUE) ? "∞ (Inalcançável)" : String.valueOf(node.distance);
            sb.append(String.format("  - Para %s: %s\n", node.label, dist));
        }
        distancesArea.setText(sb.toString());
    }

    private void drawGraph() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Desenhar arestas
        for (Edge edge : graph.edges) {
            if (edge.isEvaluating) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(4);
            } else if (edge.isPath) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3.5);
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(2);
            }

            gc.strokeLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

            double midX = (edge.from.x + edge.to.x) / 2;
            double midY = (edge.from.y + edge.to.y) / 2;
            gc.setFill(Color.WHITE);
            gc.fillOval(midX - 12, midY - 12, 24, 24);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(midX - 12, midY - 12, 24, 24);
            gc.setFill(edge.isPath ? Color.BLUE : Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(edge.weight), midX, midY + 5);
        }

        // Desenhar nós
        for (Node node : graph.nodes) {
            if (node.isVisited) {
                gc.setFill(Color.LIGHTGREEN);
                gc.setStroke(Color.DARKGREEN);
            } else if (node == hoveredNode || node.isSelected) {
                gc.setFill(Color.YELLOW);
                gc.setStroke(Color.ORANGE);
            } else {
                gc.setFill(Color.LIGHTGRAY);
                gc.setStroke(Color.BLACK);
            }

            gc.fillOval(node.x - NODE_RADIUS, node.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);
            gc.setLineWidth(2);
            gc.strokeOval(node.x - NODE_RADIUS, node.y - NODE_RADIUS, NODE_RADIUS * 2, NODE_RADIUS * 2);

            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(node.label, node.x, node.y + 6);

            // Desenhar distância
            if (node.distance != Integer.MAX_VALUE) {
                gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
                gc.setFill(Color.DARKVIOLET);
                gc.fillText("d=" + node.distance, node.x, node.y + NODE_RADIUS + 12);
            }
        }
    }

    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    private void updateStatus() {
        statusLabel.setText(String.format("Status: %d nós, %d arestas no grafo",
                graph.nodes.size(), graph.edges.size()));
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Classes internas
    class Graph {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        void addNode(Node node) { nodes.add(node); }
        void addEdge(Node from, Node to, int weight) { edges.add(new Edge(from, to, weight)); }
        boolean hasEdge(Node n1, Node n2) { return getEdgeBetween(n1,n2) != null; }

        Node getNodeByLabel(String label) {
            for (Node node : nodes) {
                if (node.label.equals(label)) return node;
            }
            return null;
        }

        List<Edge> getEdgesOf(Node node) {
            List<Edge> incidentEdges = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.from == node || edge.to == node) {
                    incidentEdges.add(edge);
                }
            }
            return incidentEdges;
        }

        Edge getEdgeBetween(Node n1, Node n2){
            for (Edge e : edges) {
                if ((e.from == n1 && e.to == n2) || (e.from == n2 && e.to == n1)) {
                    return e;
                }
            }
            return null;
        }

        void removeNode(Node node) {
            nodes.remove(node);
            edges.removeIf(e -> e.from == node || e.to == node);
        }

        void reset() {
            for (Node node : nodes) {
                node.isVisited = false;
                node.isSelected = false;
                node.distance = Integer.MAX_VALUE;
                node.predecessor = null;
            }
            for (Edge edge : edges) {
                edge.isPath = false;
                edge.isEvaluating = false;
            }
        }
    }

    class Node {
        String label;
        double x, y;
        boolean isVisited = false;
        boolean isSelected = false;

        // Atributos para Dijkstra
        int distance = Integer.MAX_VALUE;
        Node predecessor = null;

        Node(String label, double x, double y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    class Edge {
        Node from, to;
        int weight;
        boolean isPath = false; // Renomeado de inMST
        boolean isEvaluating = false;

        Edge(Node from, Node to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}