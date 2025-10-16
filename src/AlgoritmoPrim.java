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
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import java.util.*;

public class AlgoritmoPrim extends Application {

    private Canvas canvas;
    private GraphicsContext gc;
    private Graph graph;
    private TextArea logArea;
    private Label statusLabel;
    private Label costLabel;
    private Button startButton;
    private Button resetButton;
    private Button clearGraphButton;
    private Button addNodeButton;
    private Button addEdgeButton;
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
        primaryStage.setTitle("Visualizador do Algoritmo de Prim - Árvore Geradora Mínima");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topPanel = createTopPanel();
        root.setTop(topPanel);

        canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        StackPane canvasContainer = new StackPane();
        canvasContainer.getChildren().add(canvas);
        canvasContainer.setStyle("-fx-border-color: #FFA500; -fx-border-width: 2;");
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
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #FFA500; -fx-border-width: 2;");

        Label titleLabel = new Label("Algoritmo de Prim - Árvore Geradora Mínima");
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

        startButton = new Button("Iniciar Prim");
        startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        startButton.setOnAction(e -> runPrimAlgorithm());

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
        panel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #FFA500; -fx-border-width: 2;");

        Label logLabel = new Label("Log de Execução:");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(400);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 11;");

        costLabel = new Label("Custo Total da MST: 0");
        costLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        costLabel.setStyle("-fx-text-fill: #2196F3;");

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
                        "ALGORITMO DE PRIM:\n\n" +
                        "1. Escolha um nó inicial\n" +
                        "2. Adicione-o à MST\n" +
                        "3. Encontre a aresta de menor peso\n" +
                        "   conectando a MST a um nó externo\n" +
                        "4. Adicione essa aresta e nó à MST\n" +
                        "5. Repita até incluir todos os nós\n\n" +
                        "LEGENDA:\n" +
                        "• Verde: Nós na MST\n" +
                        "• Azul: Aresta na MST\n" +
                        "• Vermelho: Aresta sendo avaliada\n" +
                        "• Amarelo: Nó selecionado\n" +
                        "• Cinza: Não processado"
        );

        panel.getChildren().addAll(logLabel, logArea, costLabel, infoLabel, infoArea);
        return panel;
    }

    private HBox createBottomPanel() {
        HBox panel = new HBox(10);
        panel.setPadding(new Insets(10));
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #FFA500; -fx-border-width: 2;");

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

            // Manter dentro dos limites
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
        dialog.setHeaderText("Aresta: " + from.label + " → " + to.label);
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
                showAlert("Erro", "Digite um número válido! Aqui");
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
            costLabel.setText("Custo Total da MST: 0");
            log("Grafo limpo. Você pode criar um novo grafo.");
            updateStatus();
        }
    }

    private void loadExampleGraph() {
        clearGraph();
        initializeExampleGraph();
        drawGraph();
        log("Grafo de exemplo carregado!");
    }

    private void initializeExampleGraph() {
        graph = new Graph();

        Node n0 = new Node("A", 150, 150);
        Node n1 = new Node("B", 350, 100);
        Node n2 = new Node("C", 550, 150);
        Node n3 = new Node("D", 150, 350);
        Node n4 = new Node("E", 350, 300);
        Node n5 = new Node("F", 550, 350);
        Node n6 = new Node("G", 350, 500);

        graph.addNode(n0);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);
        graph.addNode(n4);
        graph.addNode(n5);
        graph.addNode(n6);

        graph.addEdge(n0, n1, 4);
        graph.addEdge(n0, n3, 3);
        graph.addEdge(n1, n2, 5);
        graph.addEdge(n1, n4, 2);
        graph.addEdge(n2, n5, 6);
        graph.addEdge(n3, n4, 4);
        graph.addEdge(n3, n6, 7);
        graph.addEdge(n4, n5, 3);
        graph.addEdge(n4, n6, 5);
        graph.addEdge(n5, n6, 8);

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
        costLabel.setText("Custo Total da MST: 0");
        statusLabel.setText("Status: Grafo resetado");
        startButton.setDisable(false);
    }

    private void runPrimAlgorithm() {
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

        isRunning = true;
        startButton.setDisable(true);
        logArea.clear();
        log("=== INICIANDO ALGORITMO DE PRIM ===");
        log("Nó inicial: " + startLabel);
        log("");

        new Thread(() -> {
            try {
                primAlgorithm(startLabel);
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

    private void primAlgorithm(String startLabel) throws InterruptedException {
        Node startNode = graph.getNodeByLabel(startLabel);
        if (startNode == null) return;

        Set<Node> inMST = new HashSet<>();
        Set<Edge> mstEdges = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        int totalCost = 0;

        inMST.add(startNode);
        startNode.inMST = true;
        Platform.runLater(() -> {
            drawGraph();
            log("Adicionado à MST: " + startNode.label);
            statusLabel.setText("Status: Processando nó " + startNode.label);
        });
        Thread.sleep(animationSpeed);

        for (Edge edge : graph.edges) {
            if (edge.from == startNode || edge.to == startNode) {
                pq.offer(edge);
            }
        }

        while (inMST.size() < graph.nodes.size() && !pq.isEmpty()) {
            Edge minEdge = pq.poll();

            Platform.runLater(() -> {
                minEdge.isEvaluating = true;
                drawGraph();
                log("Avaliando aresta: " + minEdge.from.label + "-" + minEdge.to.label + " (peso: " + minEdge.weight + ")");
            });
            Thread.sleep(animationSpeed / 2);

            Node nextNode = null;
            if (inMST.contains(minEdge.from) && !inMST.contains(minEdge.to)) {
                nextNode = minEdge.to;
            } else if (inMST.contains(minEdge.to) && !inMST.contains(minEdge.from)) {
                nextNode = minEdge.from;
            }

            if (nextNode != null) {
                inMST.add(nextNode);
                mstEdges.add(minEdge);
                nextNode.inMST = true;
                minEdge.inMST = true;
                totalCost += minEdge.weight;

                final int currentCost = totalCost;
                final Node finalNextNode = nextNode;
                Platform.runLater(() -> {
                    minEdge.isEvaluating = false;
                    drawGraph();
                    log("✓ Aresta ACEITA: " + minEdge.from.label + "-" + minEdge.to.label + " (peso: " + minEdge.weight + ")");
                    log("  Nó " + finalNextNode.label + " adicionado à MST");
                    log("  Custo acumulado: " + currentCost);
                    log("");
                    costLabel.setText("Custo Total da MST: " + currentCost);
                    statusLabel.setText("Status: MST com " + inMST.size() + " nós");
                });
                Thread.sleep(animationSpeed);

                for (Edge edge : graph.edges) {
                    if ((edge.from == nextNode && !inMST.contains(edge.to)) ||
                            (edge.to == nextNode && !inMST.contains(edge.from))) {
                        pq.offer(edge);
                    }
                }
            } else {
                Platform.runLater(() -> {
                    minEdge.isEvaluating = false;
                    drawGraph();
                    log("✗ Aresta REJEITADA: " + minEdge.from.label + "-" + minEdge.to.label + " (formaria ciclo)");
                    log("");
                });
                Thread.sleep(animationSpeed / 2);
            }
        }

        final int finalCost = totalCost;
        Platform.runLater(() -> {
            log("=== ALGORITMO CONCLUÍDO ===");
            log("Custo total da MST: " + finalCost);
            log("Arestas na MST: " + mstEdges.size());
            log("Nós na MST: " + inMST.size());
            statusLabel.setText("Status: Concluído! Custo total: " + finalCost);
        });
    }

    private void drawGraph() {
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Desenhar arestas
        for (Edge edge : graph.edges) {
            if (edge.isEvaluating) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(4);
            } else if (edge.inMST) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(2);
            }

            gc.strokeLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y);

            // Desenhar peso da aresta
            double midX = (edge.from.x + edge.to.x) / 2;
            double midY = (edge.from.y + edge.to.y) / 2;
            gc.setFill(Color.WHITE);
            gc.fillOval(midX - 12, midY - 12, 24, 24);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeOval(midX - 12, midY - 12, 24, 24);
            gc.setFill(edge.inMST ? Color.BLUE : Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            gc.fillText(String.valueOf(edge.weight), midX - 5, midY + 5);
        }

        // Desenhar nós
        for (Node node : graph.nodes) {
            if (node.inMST) {
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
            gc.fillText(node.label, node.x - 7, node.y + 5);
        }
    }

    private void log(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
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

        void addNode(Node node) {
            nodes.add(node);
        }

        void addEdge(Node from, Node to, int weight) {
            edges.add(new Edge(from, to, weight));
        }

        boolean hasEdge(Node n1, Node n2) {
            for (Edge e : edges) {
                if ((e.from == n1 && e.to == n2) || (e.from == n2 && e.to == n1)) {
                    return true;
                }
            }
            return false;
        }

        Node getNodeByLabel(String label) {
            for (Node node : nodes) {
                if (node.label.equals(label)) return node;
            }
            return null;
        }

        void removeNode(Node node) {
            nodes.remove(node);
            edges.removeIf(e -> e.from == node || e.to == node);
        }

        void reset() {
            for (Node node : nodes) {
                node.inMST = false;
                node.isSelected = false;
            }
            for (Edge edge : edges) {
                edge.inMST = false;
                edge.isEvaluating = false;
            }
        }
    }

    class Node {
        String label;
        double x, y;
        boolean inMST = false;
        boolean isSelected = false;

        Node(String label, double x, double y) {
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    class Edge {
        Node from, to;
        int weight;
        boolean inMST = false;
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