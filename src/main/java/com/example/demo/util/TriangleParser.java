package com.example.demo.util;

import com.example.demo.model.Point;
import com.example.demo.model.Triangle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Клас для парсингу трикутників з текстового файлу
 */
public class TriangleParser {
    private static final Pattern TRIANGLE_PATTERN = Pattern.compile(
        "Triangle\\s+(\\d+)\\s*:\\s*x1=(\\d+);\\s*y1=(\\d+);\\s*x2=(\\d+);\\s*y2=(\\d+);\\s*x3=(\\d+);\\s*y3=(\\d+);"
    );

    /**
     * Прочитати трикутники з файлу
     */
    public static List<Triangle> parseTriangles(String filename) throws IOException {
        List<Triangle> triangles = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue; // Пропускаємо порожні рядки та коментарі
                }
                
                Matcher matcher = TRIANGLE_PATTERN.matcher(line);
                if (matcher.find()) {
                    int id = Integer.parseInt(matcher.group(1));
                    double x1 = Double.parseDouble(matcher.group(2));
                    double y1 = Double.parseDouble(matcher.group(3));
                    double x2 = Double.parseDouble(matcher.group(4));
                    double y2 = Double.parseDouble(matcher.group(5));
                    double x3 = Double.parseDouble(matcher.group(6));
                    double y3 = Double.parseDouble(matcher.group(7));
                    
                    Point p1 = new Point(x1, y1);
                    Point p2 = new Point(x2, y2);
                    Point p3 = new Point(x3, y3);
                    
                    triangles.add(new Triangle(id, p1, p2, p3));
                } else {
                    System.err.println("Warning: Could not parse line " + lineNumber + ": " + line);
                }
            }
        }
        
        return triangles;
    }
}







