package com.example.demo.controller;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import com.example.demo.util.PointsIO;
import com.example.demo.algorithms.DiamondFinderGreedy;
import com.example.demo.algorithms.DiamondFinderHash;
import com.example.demo.algorithms.DiamondFinderOptimal;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Таблиця 1: етап «Реалізація коду» (контролер, що поєднує модель, алгоритми та графічний інтерфейс DiamonFinder)
public class MainController {
    @FXML private TextField pointCountField;
    @FXML private Button generatePointsButton;
    @FXML private Button loadFromFileButton;
    @FXML private Button hashBasedButton;
    @FXML private Button optimalButton;
    @FXML private Canvas drawCanvas;
    @FXML private TextArea outputArea;
    @FXML private Text timeHashText;
    @FXML private Text timeOptimalText;

    private final String pointsFile = "points.txt";
    private final String resultsFile = "results.txt";
    private List<Point> points = new ArrayList<>();
    private List<Diamond> currentDiamonds = new ArrayList<>();
    private DiamondFinderHash hashFinder = new DiamondFinderHash();
    private DiamondFinderOptimal optimalFinder = new DiamondFinderOptimal();

    @FXML
    private void initialize() {
        timeHashText.setText("Час (Hash): -");
        timeOptimalText.setText("Час (Optimal): -");
        // Обробники тепер підключені безпосередньо у FXML через onAction,
        // тому явне setOnAction тут не є обов'язковим.
    }

    @FXML
    private void handleGeneratePoints() {
        String nStr = pointCountField.getText();
        int n;
        try {
            n = Integer.parseInt(nStr.trim());
            if (n < 1 || n > 5000) throw new NumberFormatException();
        } catch (Exception ex) {
            outputArea.setText("Введіть коректну кількість точок (1-5000)");
            return;
        }
        // Генеруємо точки тільки в пам'яті та одразу малюємо їх,
        // щоб генерація працювала навіть у випадку проблем з файловою системою.
        this.points = generateRandomPoints(
                n,
                10,
                (int) drawCanvas.getWidth() - 10,
                10,
                (int) drawCanvas.getHeight() - 10
        );
        // Спроба зберегти точки у файл (не критично для відображення)
        try {
            PointsIO.saveToFile(points, pointsFile);
        } catch (IOException ex) {
            outputArea.appendText("\nПопередження: не вдалося зберегти точки у файл: " + ex.getMessage());
        }
        drawPoints();
        // Вивід координат перших 10 точок
        StringBuilder sb = new StringBuilder();
        sb.append("Згенеровано точок: ").append(points.size()).append("\n");
        for (int i = 0; i < Math.min(10, points.size()); ++i) {
            Point p = points.get(i);
            sb.append(String.format("Point %d: x=%.2f; y=%.2f\n", i+1, p.getX(), p.getY()));
        }
        outputArea.setText(sb.toString());
    }

    private List<Point> generateRandomPoints(int n, int xMin, int xMax, int yMin, int yMax) {
        List<Point> pts = new ArrayList<>(n);
        Random rand = new Random();
        for (int i = 0; i < n; ++i) {
            double x = rand.nextDouble() * (xMax - xMin) + xMin;
            double y = rand.nextDouble() * (yMax - yMin) + yMin;
            pts.add(new Point(x, y));
        }
        return pts;
    }

    @FXML
    private void handleLoadFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Виберіть файл з точками");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Текстові файли", "*.txt")
        );
        
        Stage stage = (Stage) drawCanvas.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            try {
                points = PointsIO.loadFromFile(file.getAbsolutePath());
                drawPoints();
                outputArea.setText("Завантажено точок: " + points.size() + "\n");
                currentDiamonds.clear();
            } catch (IOException ex) {
                outputArea.setText("Помилка завантаження: " + ex.getMessage());
            }
        }
    }
    
    @FXML
    private void handleHashBased() {
        if (points.isEmpty()) {
            outputArea.setText("Спочатку завантажте або згенеруйте точки!");
            return;
        }

        long startTime = System.nanoTime();
        currentDiamonds = hashFinder.findDiamonds(points);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        timeHashText.setText(String.format("Час (Hash): %d мс", durationMs));
        drawDiamonds();
        outputResults(currentDiamonds, durationMs, "Hash-Based");
    }
    
    @FXML
    private void handleOptimal() {
        if (points.isEmpty()) {
            outputArea.setText("Спочатку завантажте або згенеруйте точки!");
            return;
        }

        long startTime = System.nanoTime();
        currentDiamonds = optimalFinder.findDiamonds(points);
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        timeOptimalText.setText(String.format("Час (Optimal): %d мс", durationMs));
        drawDiamonds();
        outputResults(currentDiamonds, durationMs, "Optimal");
    }

    private void drawPoints() {
        GraphicsContext gc = drawCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
        gc.setFill(Color.BLUE);
        for (Point p : points) {
            gc.fillOval(p.getX() - 3, p.getY() - 3, 6, 6);
        }
    }
    
    private void drawDiamonds() {
        GraphicsContext gc = drawCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
        
        // Спочатку малюємо точки
        gc.setFill(Color.BLUE);
        for (Point p : points) {
            gc.fillOval(p.getX() - 3, p.getY() - 3, 6, 6);
        }
        
        // Потім малюємо ромби
        for (Diamond diamond : currentDiamonds) {
            Point[] vertices = diamond.vertices;
            
            // Квадрати малюємо червоним кольором, інші ромби - зеленим
            if (diamond.isSquare) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(2.0);
            } else {
                gc.setStroke(Color.GREEN);
                gc.setLineWidth(2.0);
            }
            
            // Малюємо ромб як замкнений багатокутник
            double[] xPoints = new double[4];
            double[] yPoints = new double[4];
            for (int i = 0; i < 4; i++) {
                xPoints[i] = vertices[i].getX();
                yPoints[i] = vertices[i].getY();
            }
            
            gc.strokePolygon(xPoints, yPoints, 4);
        }
    }
    
    private void outputResults(List<Diamond> diamonds, long durationMs, String algorithm) {
        StringBuilder sb = new StringBuilder();
        sb.append("Алгоритм: ").append(algorithm).append("\n");
        sb.append("Час виконання: ").append(durationMs).append(" мс\n");
        sb.append("Завантажено точок: ").append(points.size()).append("\n");
        sb.append("Знайдено ромбів: ").append(diamonds.size()).append("\n");
        sb.append("З них квадратів: ").append(
            diamonds.stream().mapToInt(d -> d.isSquare ? 1 : 0).sum()
        ).append("\n\n");
        
        // Виводимо координати перших 10 точок
        sb.append("Перші 10 точок:\n");
        for (int i = 0; i < Math.min(10, points.size()); i++) {
            Point p = points.get(i);
            sb.append(String.format("Point %d: x=%.2f; y=%.2f\n", i + 1, p.getX(), p.getY()));
        }
        sb.append("\n");
        
        // Виводимо координати кожної фігури
        for (int i = 0; i < diamonds.size(); i++) {
            Diamond d = diamonds.get(i);
            Point[] v = d.vertices;
            sb.append(String.format("Figure %d: x1=%.0f; y1=%.0f; x2=%.0f; y2=%.0f; x3=%.0f; y3=%.0f; x4=%.0f; y4=%.0f; %s\n",
                i + 1, v[0].getX(), v[0].getY(), v[1].getX(), v[1].getY(),
                v[2].getX(), v[2].getY(), v[3].getX(), v[3].getY(),
                d.isSquare ? "(квадрат)" : "(ромб)"));
        }
        
        outputArea.setText(sb.toString());
        
        // Зберігаємо результати у файл
        try (PrintWriter pw = new PrintWriter(new java.io.FileWriter(resultsFile))) {
            for (int i = 0; i < diamonds.size(); i++) {
                Diamond d = diamonds.get(i);
                Point[] v = d.vertices;
                pw.printf("Figure %d: x1=%.0f; y1=%.0f; x2=%.0f; y2=%.0f; x3=%.0f; y3=%.0f; x4=%.0f; y4=%.0f;%n",
                    i + 1, v[0].getX(), v[0].getY(), v[1].getX(), v[1].getY(),
                    v[2].getX(), v[2].getY(), v[3].getX(), v[3].getY());
            }
        } catch (IOException ex) {
            outputArea.appendText("\nПомилка збереження у файл: " + ex.getMessage());
        }
    }
}
