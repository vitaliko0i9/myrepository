package com.example.demo.algorithms;

import com.example.demo.model.Point;
import com.example.demo.model.Triangle;

import java.util.*;

/**
 * Клас для аналізу трикутників: перевірка вкладення та перетину
 */
public class TriangleAnalyzer {
    
    /**
     * Алгоритм 1: Простий перебір - O(n²) по часу, O(n) по пам'яті
     * Перевіряє всі пари трикутників
     */
    public static AnalysisResult analyzeTrianglesSimple(List<Triangle> triangles) {
        long startTime = System.nanoTime();
        
        Map<Integer, Triangle.TriangleStatus> statusMap = new HashMap<>();
        
        // Ініціалізуємо статус всіх трикутників
        for (Triangle triangle : triangles) {
            statusMap.put(triangle.getId(), Triangle.TriangleStatus.NORMAL);
        }
        
        // Перевіряємо всі пари трикутників
        for (int i = 0; i < triangles.size(); i++) {
            Triangle t1 = triangles.get(i);
            
            for (int j = i + 1; j < triangles.size(); j++) {
                Triangle t2 = triangles.get(j);
                
                boolean t1NestedInT2 = t1.isNestedIn(t2);
                boolean t2NestedInT1 = t2.isNestedIn(t1);
                boolean intersecting = doTrianglesIntersect(t1, t2);
                
                // Оновлюємо статуси
                updateStatus(t1, t1NestedInT2, intersecting, statusMap);
                updateStatus(t2, t2NestedInT1, intersecting, statusMap);
            }
        }
        
        // Застосовуємо статуси до трикутників
        for (Triangle triangle : triangles) {
            triangle.setStatus(statusMap.get(triangle.getId()));
        }
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // конвертуємо в мілісекунди
        
        return new AnalysisResult(duration, triangles);
    }
    
    /**
     * Алгоритм 2: З оптимізацією через bounding box - O(n²) у гіршому випадку, але швидше на практиці
     * Використовує попередню перевірку через bounding box
     */
    public static AnalysisResult analyzeTrianglesOptimized(List<Triangle> triangles) {
        long startTime = System.nanoTime();
        
        Map<Integer, Triangle.TriangleStatus> statusMap = new HashMap<>();
        Map<Integer, TriangleInfo> triangleInfoMap = new HashMap<>();
        
        // Спочатку обчислюємо bounding box для кожного трикутника
        for (Triangle triangle : triangles) {
            statusMap.put(triangle.getId(), Triangle.TriangleStatus.NORMAL);
            triangleInfoMap.put(triangle.getId(), new TriangleInfo(
                triangle.getMinX(), triangle.getMaxX(),
                triangle.getMinY(), triangle.getMaxY()
            ));
        }
        
        // Перевіряємо пари з попередньою перевіркою bounding box
        for (int i = 0; i < triangles.size(); i++) {
            Triangle t1 = triangles.get(i);
            TriangleInfo info1 = triangleInfoMap.get(t1.getId());
            
            for (int j = i + 1; j < triangles.size(); j++) {
                Triangle t2 = triangles.get(j);
                TriangleInfo info2 = triangleInfoMap.get(t2.getId());
                
                // Швидка перевірка: чи взагалі можуть вони взаємодіяти?
                if (!boundingBoxesOverlap(info1, info2)) {
                    continue; // Неможливо перетинатися або бути вкладеними
                }
                
                boolean t1NestedInT2 = t1.isNestedIn(t2);
                boolean t2NestedInT1 = t2.isNestedIn(t1);
                boolean intersecting = doTrianglesIntersect(t1, t2);
                
                updateStatus(t1, t1NestedInT2, intersecting, statusMap);
                updateStatus(t2, t2NestedInT1, intersecting, statusMap);
            }
        }
        
        // Застосовуємо статуси
        for (Triangle triangle : triangles) {
            triangle.setStatus(statusMap.get(triangle.getId()));
        }
        
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;
        
        return new AnalysisResult(duration, triangles);
    }
    
    /**
     * Перевірка, чи перетинаються bounding box'и
     */
    private static boolean boundingBoxesOverlap(TriangleInfo info1, TriangleInfo info2) {
        return !(info1.maxX < info2.minX || info2.maxX < info1.minX ||
                 info1.maxY < info2.minY || info2.maxY < info1.minY);
    }
    
    /**
     * Оновити статус трикутника
     */
    private static void updateStatus(Triangle triangle, boolean isNested, boolean isIntersecting, 
                                    Map<Integer, Triangle.TriangleStatus> statusMap) {
        Triangle.TriangleStatus currentStatus = statusMap.get(triangle.getId());
        
        if (isNested && isIntersecting) {
            statusMap.put(triangle.getId(), Triangle.TriangleStatus.BOTH);
        } else if (isNested) {
            if (currentStatus == Triangle.TriangleStatus.INTERSECTING) {
                statusMap.put(triangle.getId(), Triangle.TriangleStatus.BOTH);
            } else if (currentStatus != Triangle.TriangleStatus.BOTH) {
                statusMap.put(triangle.getId(), Triangle.TriangleStatus.NESTED);
            }
        } else if (isIntersecting) {
            if (currentStatus == Triangle.TriangleStatus.NESTED) {
                statusMap.put(triangle.getId(), Triangle.TriangleStatus.BOTH);
            } else if (currentStatus != Triangle.TriangleStatus.BOTH) {
                statusMap.put(triangle.getId(), Triangle.TriangleStatus.INTERSECTING);
            }
        }
    }
    
    /**
     * Перевірити, чи перетинаються два трикутники
     */
    public static boolean doTrianglesIntersect(Triangle t1, Triangle t2) {
        // Перевіряємо перетин ребер одного трикутника з ребрами іншого
        Point[] edges1 = {t1.getP1(), t1.getP2(), t1.getP3()};
        Point[] edges2 = {t2.getP1(), t2.getP2(), t2.getP3()};
        
        // Перевіряємо всі пари ребер
        for (int i = 0; i < 3; i++) {
            Point p1 = edges1[i];
            Point p2 = edges1[(i + 1) % 3];
            
            for (int j = 0; j < 3; j++) {
                Point p3 = edges2[j];
                Point p4 = edges2[(j + 1) % 3];
                
                if (doLineSegmentsIntersect(p1, p2, p3, p4)) {
                    return true;
                }
            }
        }
        
        // Перевіряємо, чи одна з вершин одного трикутника всередині іншого
        if (t2.containsPoint(t1.getP1()) || t2.containsPoint(t1.getP2()) || t2.containsPoint(t1.getP3()) ||
            t1.containsPoint(t2.getP1()) || t1.containsPoint(t2.getP2()) || t1.containsPoint(t2.getP3())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Перевірити, чи перетинаються два відрізки
     */
    private static boolean doLineSegmentsIntersect(Point p1, Point p2, Point p3, Point p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);
        
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }
        
        if (d1 == 0 && onSegment(p3, p4, p1)) return true;
        if (d2 == 0 && onSegment(p3, p4, p2)) return true;
        if (d3 == 0 && onSegment(p1, p2, p3)) return true;
        if (d4 == 0 && onSegment(p1, p2, p4)) return true;
        
        return false;
    }
    
    /**
     * Обчислити напрямок трьох точок
     */
    private static double direction(Point p1, Point p2, Point p3) {
        return (p3.getX() - p1.getX()) * (p2.getY() - p1.getY()) - 
               (p2.getX() - p1.getX()) * (p3.getY() - p1.getY());
    }
    
    /**
     * Перевірити, чи знаходиться точка на відрізку
     */
    private static boolean onSegment(Point p1, Point p2, Point p) {
        return Math.min(p1.getX(), p2.getX()) <= p.getX() &&
               p.getX() <= Math.max(p1.getX(), p2.getX()) &&
               Math.min(p1.getY(), p2.getY()) <= p.getY() &&
               p.getY() <= Math.max(p1.getY(), p2.getY());
    }
    
    /**
     * Клас для зберігання інформації про bounding box трикутника
     */
    private static class TriangleInfo {
        double minX, maxX, minY, maxY;
        
        TriangleInfo(double minX, double maxX, double minY, double maxY) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
        }
    }
    
    /**
     * Результат аналізу з часом виконання
     */
    public static class AnalysisResult {
        private final long executionTimeMs;
        private final List<Triangle> triangles;
        
        public AnalysisResult(long executionTimeMs, List<Triangle> triangles) {
            this.executionTimeMs = executionTimeMs;
            this.triangles = triangles;
        }
        
        public long getExecutionTimeMs() {
            return executionTimeMs;
        }
        
        public List<Triangle> getTriangles() {
            return triangles;
        }
    }
}
