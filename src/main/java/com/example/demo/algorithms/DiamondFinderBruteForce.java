package com.example.demo.algorithms;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import java.util.*;

/**
 * Алгоритм повного перебору для пошуку ромбів.
 * Складність: O(C(n,4) * 4!) = O(n⁴) по часу
 * Пам'ять: O(n⁴) для зберігання комбінацій
 * 
 * Алгоритм перебирає всі можливі комбінації з 4 точок
 * і перевіряє, чи вони утворюють ромб.
 */
public class DiamondFinderBruteForce {
    public List<Diamond> findDiamonds(List<Point> points) {
        int n = points.size();
        if (n < 4) return new ArrayList<>();
        
        // Крок 1: Знайти всі можливі ромби
        List<DiamondCandidate> allCandidates = new ArrayList<>();
        List<List<Integer>> combs = getCombinations(n, 4);
        
        for (List<Integer> indices : combs) {
            Point[] quad = new Point[4];
            for (int i = 0; i < 4; i++) quad[i] = points.get(indices.get(i));
            
            // Знаходимо правильний порядок вершин для ромба
            Point[] orderedQuad = findValidRhombusOrder(quad);
            if (orderedQuad != null) {
                boolean isSquare = isSquare(orderedQuad);
                allCandidates.add(new DiamondCandidate(orderedQuad, isSquare, new HashSet<>(indices)));
            }
        }
        
        // Крок 2: Вибрати максимальний набір ромбів без спільних вершин (жадібний підхід)
        // Сортуємо: спочатку квадрати (більш "цінні"), потім інші ромби
        allCandidates.sort((a, b) -> {
            if (a.isSquare != b.isSquare) {
                return b.isSquare ? 1 : -1; // квадрати спочатку
            }
            return 0;
        });
        
        List<Diamond> diamonds = new ArrayList<>();
        Set<Integer> used = new HashSet<>();

        for (DiamondCandidate candidate : allCandidates) {
            // Перевіряємо, чи немає спільних вершин
            boolean hasConflict = false;
            for (Integer idx : candidate.indices) {
                if (used.contains(idx)) {
                    hasConflict = true;
                    break;
                }
            }
            
            if (!hasConflict) {
                diamonds.add(new Diamond(candidate.vertices, candidate.isSquare));
                used.addAll(candidate.indices);
            }
        }
        
        return diamonds;
    }
    
    // Допоміжний клас для зберігання кандидатів
    private static class DiamondCandidate {
        final Point[] vertices;
        final boolean isSquare;
        final Set<Integer> indices;
        
        DiamondCandidate(Point[] vertices, boolean isSquare, Set<Integer> indices) {
            this.vertices = vertices;
            this.isSquare = isSquare;
            this.indices = indices;
        }
    }

    // Повертає всі k-комбінації n елементів (поіндексно)
    private List<List<Integer>> getCombinations(int n, int k) {
        List<List<Integer>> res = new ArrayList<>();
        combine(res, new ArrayList<>(), 0, n, k);
        return res;
    }

    private void combine(List<List<Integer>> res, List<Integer> cur, int start, int n, int k) {
        if (k == 0) {
            res.add(new ArrayList<>(cur));
            return;
        }
        for (int i = start; i <= n - k; i++) {
            cur.add(i);
            combine(res, cur, i + 1, n, k - 1);
            cur.remove(cur.size() - 1);
        }
    }

    // Перевіряє, чи 4 точки є ромбом (всі сторони рівні, діагоналі не нульові)
    private boolean isRhombus(Point[] quad) {
        return findValidRhombusOrder(quad) != null;
    }
    
    // Знаходить правильний порядок вершин для ромба, або null якщо не ромб
    private Point[] findValidRhombusOrder(Point[] quad) {
        Point[] result = findValidOrder(quad.clone(), 0);
        return result;
    }
    
    private Point[] findValidOrder(Point[] quad, int start) {
        if (start == 4) {
            if (isValidRhombusOrder(quad)) {
                return quad.clone();
            }
            return null;
        }
        
        for (int i = start; i < 4; i++) {
            Point temp = quad[start];
            quad[start] = quad[i];
            quad[i] = temp;
            
            Point[] result = findValidOrder(quad, start + 1);
            if (result != null) {
                return result;
            }
            
            temp = quad[start];
            quad[start] = quad[i];
            quad[i] = temp;
        }
        return null;
    }
    
    private boolean isValidRhombusOrder(Point[] quad) {
        double[] sides = new double[4];
        for (int i = 0; i < 4; i++) {
            sides[i] = dist(quad[i], quad[(i + 1) % 4]);
        }

        // Всі сторони рівні та ненульові
        boolean allEqual = eq(sides[0], sides[1]) && eq(sides[1], sides[2]) && eq(sides[2], sides[3]) && sides[0] > 0.5;
        if (!allEqual) return false;

        // Діагоналі ненульові і мають спільний центр (щоб уникнути самоперетинів)
        double diag1 = dist(quad[0], quad[2]);
        double diag2 = dist(quad[1], quad[3]);
        if (diag1 <= 0.5 || diag2 <= 0.5) return false;

        double mid12x = (quad[0].x + quad[2].x) / 2.0;
        double mid12y = (quad[0].y + quad[2].y) / 2.0;
        double mid34x = (quad[1].x + quad[3].x) / 2.0;
        double mid34y = (quad[1].y + quad[3].y) / 2.0;
        if (!eq(mid12x, mid34x) || !eq(mid12y, mid34y)) return false;

        // Перевіряємо опуклість (щоб виключити «бантик»)
        if (!isConvex(quad)) return false;

        return true;
            }

    private boolean isConvex(Point[] quad) {
        double prev = 0;
        for (int i = 0; i < 4; i++) {
            Point a = quad[i];
            Point b = quad[(i + 1) % 4];
            Point c = quad[(i + 2) % 4];
            double cross = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
            if (i == 0) {
                prev = cross;
            } else {
                if (prev * cross < -1e-9) return false; // різні знаки — не опуклий
            }
        }
        return true;
    }

    // Перевіряє, чи 4 точки утворюють квадрат (ромб з рівними діагоналями)
    private boolean isSquare(Point[] quad) {
        // Спочатку знаходимо правильний порядок для ромба
        Point[] ordered = findValidRhombusOrder(quad);
        if (ordered == null) {
        return false;
    }

        // Для квадрата діагоналі повинні бути рівні
        double diag1 = dist(ordered[0], ordered[2]);
        double diag2 = dist(ordered[1], ordered[3]);
        return eq(diag1, diag2) && diag1 > 0.5;
    }

    private double dist(Point a, Point b) {
        double dx = a.x - b.x, dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
    private boolean eq(double a, double b) {
        return Math.abs(a - b) < 0.5; // похибка до 0.5 пікселя
    }
}

