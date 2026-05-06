package com.example.demo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Клас для представлення трикутника
 */
public class Triangle {
    private Point p1;
    private Point p2;
    private Point p3;
    private int id;
    private TriangleStatus status;
    
    public enum TriangleStatus {
        NORMAL,        // Звичайний трикутник
        NESTED,        // Вкладений трикутник
        INTERSECTING,  // Трикутник, що перетинається
        BOTH           // І вкладений, і перетинається
    }

    public Triangle(int id, Point p1, Point p2, Point p3) {
        this.id = id;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        this.status = TriangleStatus.NORMAL;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public Point getP3() {
        return p3;
    }

    public int getId() {
        return id;
    }

    public TriangleStatus getStatus() {
        return status;
    }

    public void setStatus(TriangleStatus status) {
        this.status = status;
    }

    /**
     * Отримати всі точки трикутника
     */
    public List<Point> getPoints() {
        List<Point> points = new ArrayList<>();
        points.add(p1);
        points.add(p2);
        points.add(p3);
        return points;
    }

    /**
     * Обчислити площу трикутника
     */
    public double getArea() {
        return Math.abs((p1.getX() * (p2.getY() - p3.getY()) +
                        p2.getX() * (p3.getY() - p1.getY()) +
                        p3.getX() * (p1.getY() - p2.getY())) / 2.0);
    }

    /**
     * Перевірити, чи знаходиться точка всередині трикутника
     */
    public boolean containsPoint(Point point) {
        double area1 = triangleArea(point, p1, p2);
        double area2 = triangleArea(point, p2, p3);
        double area3 = triangleArea(point, p3, p1);
        double area = getArea();
        
        return Math.abs(area - (area1 + area2 + area3)) < 0.001;
    }

    /**
     * Обчислити площу трикутника за трьома точками
     */
    private double triangleArea(Point a, Point b, Point c) {
        return Math.abs((a.getX() * (b.getY() - c.getY()) +
                        b.getX() * (c.getY() - a.getY()) +
                        c.getX() * (a.getY() - b.getY())) / 2.0);
    }

    /**
     * Перевірити, чи знаходиться цей трикутник всередині іншого
     */
    public boolean isNestedIn(Triangle other) {
        return other.containsPoint(this.p1) &&
               other.containsPoint(this.p2) &&
               other.containsPoint(this.p3);
    }

    /**
     * Отримати мінімальні та максимальні координати для bounding box
     */
    public double getMinX() {
        return Math.min(Math.min(p1.getX(), p2.getX()), p3.getX());
    }

    public double getMaxX() {
        return Math.max(Math.max(p1.getX(), p2.getX()), p3.getX());
    }

    public double getMinY() {
        return Math.min(Math.min(p1.getY(), p2.getY()), p3.getY());
    }

    public double getMaxY() {
        return Math.max(Math.max(p1.getY(), p2.getY()), p3.getY());
    }

    @Override
    public String toString() {
        return "Triangle " + id + ": p1=" + p1 + ", p2=" + p2 + ", p3=" + p3;
    }
}







