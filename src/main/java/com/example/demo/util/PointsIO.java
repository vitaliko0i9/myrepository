package com.example.demo.util;

import com.example.demo.model.Point;
import java.io.*;
import java.util.*;

// Таблиця 1: етап «Підтримка проєктної команди» (спільна робота з наборами точок через збереження та завантаження файлів)
public class PointsIO {
    public static void saveToFile(List<Point> points, String path) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (int i = 0; i < points.size(); ++i) {
                Point p = points.get(i);
                pw.printf("Point %d: x=%.2f; y=%.2f;%n", i+1, p.x, p.y);
            }
        }
    }
    public static List<Point> loadFromFile(String path) throws IOException {
        List<Point> points = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                double x = 0, y = 0;
                try {
                    String[] tokens = line.split("[=:;]");
                    for (int i = 0; i < tokens.length - 1; i++) {
                        String t = tokens[i].trim();
                        if (t.equals("x")) x = Double.parseDouble(tokens[i + 1].trim());
                        if (t.equals("y")) y = Double.parseDouble(tokens[i + 1].trim());
                    }
                    points.add(new Point(x, y));
                } catch (Exception e) {
                    // Якщо рядок погано розпарсено — пропускаємо
                }
            }
        }
        return points;
    }
}
