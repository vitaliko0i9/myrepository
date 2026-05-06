package com.example.demo.model;


    // Таблиця 1: етап «Проєктування – загальна модель» (базовий елемент моделі — точка на площині)
public class Point {
    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Point(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Point)) return false;
        Point p = (Point)obj;
        return Math.abs(p.x - x) < 1e-8 && Math.abs(p.y - y) < 1e-8;
    }

    @Override
    public int hashCode() { return Double.hashCode(x) * 31 + Double.hashCode(y); }
}







