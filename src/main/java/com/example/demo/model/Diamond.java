package com.example.demo.model;

public class Diamond {
    public final Point[] vertices; // 4 вершини
    public final boolean isSquare; // true, якщо це квадрат
    public Diamond(Point[] vertices, boolean isSquare) {
        if (vertices.length != 4)
            throw new IllegalArgumentException("Diamond must have 4 vertices");
        this.vertices = vertices;
        this.isSquare = isSquare;
    }
}
