package com.example.demo.util;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import com.example.demo.algorithms.DiamondFinderHash;
import com.example.demo.algorithms.DiamondFinderGreedy;
import java.io.IOException;
import java.util.*;

/**
 * Точний підрахунок ромбів та квадратів з використанням існуючих алгоритмів.
 * Використовує обидва алгоритми для перевірки та порівняння результатів.
 * Таблиця 1: етапи «Функціональне тестування», «Внутрішнє тестування», «Зовнішнє тестування» та «Супроводження».
 */
public class ExactDiamondCounter {
    private static final double EPS = 0.5;
    
    // Таблиця 1: функціональне тестування, внутрішнє тестування, зовнішнє тестування та супроводження програмного продукту
    public static void main(String[] args) {
        try {
            System.out.println("=== Точний підрахунок ромбів та квадратів ===\n");
            
            // Завантажуємо точки
            List<Point> points = PointsIO.loadFromFile("points.txt");
            System.out.println("Завантажено точок: " + points.size());
            
            // Використовуємо Hash-Based алгоритм (найточніший)
            System.out.println("\n--- Hash-Based Algorithm (основний) ---");
            DiamondFinderHash hashFinder = new DiamondFinderHash();
            long start = System.nanoTime();
            List<Diamond> hashDiamonds = hashFinder.findDiamonds(points);
            long hashTime = (System.nanoTime() - start) / 1_000_000;
            
            int hashSquares = 0;
            int hashRhombuses = 0;
            for (Diamond d : hashDiamonds) {
                if (d.isSquare) {
                    hashSquares++;
                } else {
                    hashRhombuses++;
                }
            }
            
            System.out.println("Час виконання: " + hashTime + " мс");
            System.out.println("Всього знайдено фігур: " + hashDiamonds.size());
            System.out.println("  - Квадратів: " + hashSquares);
            System.out.println("  - Ромбів: " + hashRhombuses);
            
            // Використовуємо Greedy алгоритм для порівняння
            System.out.println("\n--- Greedy Algorithm (для порівняння) ---");
            DiamondFinderGreedy greedyFinder = new DiamondFinderGreedy();
            start = System.nanoTime();
            List<Diamond> greedyDiamonds = greedyFinder.findDiamonds(points);
            long greedyTime = (System.nanoTime() - start) / 1_000_000;
            
            int greedySquares = 0;
            int greedyRhombuses = 0;
            for (Diamond d : greedyDiamonds) {
                if (d.isSquare) {
                    greedySquares++;
                } else {
                    greedyRhombuses++;
                }
            }
            
            System.out.println("Час виконання: " + greedyTime + " мс");
            System.out.println("Всього знайдено фігур: " + greedyDiamonds.size());
            System.out.println("  - Квадратів: " + greedySquares);
            System.out.println("  - Ромбів: " + greedyRhombuses);
            
            // Порівняння результатів
            System.out.println("\n=== Порівняння результатів ===");
            if (hashDiamonds.size() == greedyDiamonds.size() && 
                hashSquares == greedySquares) {
                System.out.println("✓ Результати збігаються!");
            } else {
                System.out.println("⚠ Результати відрізняються:");
                System.out.println("  Hash-Based: " + hashDiamonds.size() + " фігур (" + 
                                 hashSquares + " квадратів)");
                System.out.println("  Greedy: " + greedyDiamonds.size() + " фігур (" + 
                                 greedySquares + " квадратів)");
            }
            
            // Фінальний результат (використовуємо Hash-Based як еталон)
            System.out.println("\n=== ФІНАЛЬНИЙ РЕЗУЛЬТАТ ===");
            System.out.println("Точна кількість КВАДРАТІВ: " + hashSquares);
            System.out.println("Точна кількість РОМБІВ: " + hashRhombuses);
            System.out.println("Всього фігур: " + hashDiamonds.size());
            
            // Виводимо детальну інформацію про перші 10 фігур
            System.out.println("\n--- Перші 10 знайдених фігур ---");
            for (int i = 0; i < Math.min(10, hashDiamonds.size()); i++) {
                Diamond d = hashDiamonds.get(i);
                Point[] v = d.vertices;
                System.out.printf("Figure %d: %s (x1=%.0f, y1=%.0f; x2=%.0f, y2=%.0f; x3=%.0f, y3=%.0f; x4=%.0f, y4=%.0f)%n",
                    i + 1, d.isSquare ? "КВАДРАТ" : "РОМБ",
                    v[0].getX(), v[0].getY(), v[1].getX(), v[1].getY(),
                    v[2].getX(), v[2].getY(), v[3].getX(), v[3].getY());
            }
            
        } catch (IOException e) {
            System.err.println("Помилка завантаження файлу: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Помилка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}




