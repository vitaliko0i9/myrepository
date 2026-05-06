package com.example.demo;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import com.example.demo.util.PointsIO;
import com.example.demo.algorithms.DiamondFinderHash;
import com.example.demo.algorithms.DiamondFinderGreedy;
import java.io.IOException;
import java.util.List;

public class TestDiamonds {
    public static void main(String[] args) {
        try {
            System.out.println("Завантаження точок з points.txt...");
            List<Point> points = PointsIO.loadFromFile("points.txt");
            System.out.println("Завантажено точок: " + points.size());
            
            System.out.println("\n=== Hash-Based Algorithm ===");
            DiamondFinderHash hashFinder = new DiamondFinderHash();
            long start = System.nanoTime();
            List<Diamond> hashDiamonds = hashFinder.findDiamonds(points);
            long hashTime = (System.nanoTime() - start) / 1_000_000;
            
            int hashSquares = 0;
            for (Diamond d : hashDiamonds) {
                if (d.isSquare) hashSquares++;
            }
            
            System.out.println("Час: " + hashTime + " мс");
            System.out.println("Знайдено ромбів: " + hashDiamonds.size());
            System.out.println("З них квадратів: " + hashSquares);
            
            System.out.println("\n=== Greedy Algorithm ===");
            DiamondFinderGreedy greedyFinder = new DiamondFinderGreedy();
            start = System.nanoTime();
            List<Diamond> greedyDiamonds = greedyFinder.findDiamonds(points);
            long greedyTime = (System.nanoTime() - start) / 1_000_000;
            
            int greedySquares = 0;
            for (Diamond d : greedyDiamonds) {
                if (d.isSquare) greedySquares++;
            }
            
            System.out.println("Час: " + greedyTime + " мс");
            System.out.println("Знайдено ромбів: " + greedyDiamonds.size());
            System.out.println("З них квадратів: " + greedySquares);
            
            System.out.println("\n=== Результат ===");
            System.out.println("Очікувана кількість ромбів: " + hashDiamonds.size());
            System.out.println("Очікувана кількість квадратів: " + hashSquares);
            
        } catch (IOException e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




