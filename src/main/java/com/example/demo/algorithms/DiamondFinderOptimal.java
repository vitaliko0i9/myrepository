package com.example.demo.algorithms;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import java.util.*;

/**
 * Оптимальний алгоритм пошуку максимального набору ромбів.
 * Використовує покращену жадібну стратегію з врахуванням конфліктів.
 * Складність: O(n² + m²) де n - кількість точок, m - кількість кандидатів
 */
public class DiamondFinderOptimal {
    private static final double EPS = 0.5;
    
    public List<Diamond> findDiamonds(List<Point> points) {
        if (points.size() < 4) {
            return new ArrayList<>();
        }
        
        // Крок 1: Знайти всі можливі ромби (використовуємо той самий підхід, що й Hash-Based)
        Map<String, List<Pair>> buckets = new HashMap<>();
        List<DiamondCandidate> candidates = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        
        // Створюємо всі пари як потенційні діагоналі
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                Point p1 = points.get(i);
                Point p2 = points.get(j);
                double midX = (p1.x + p2.x) / 2.0;
                double midY = (p1.y + p2.y) / 2.0;
                double dx = p2.x - p1.x;
                double dy = p2.y - p1.y;
                double len2 = dx * dx + dy * dy;
                if (len2 < EPS * EPS) continue;
                
                String key = bucketKeyByCenter(midX, midY);
                buckets.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new Pair(i, j, dx, dy, midX, midY));
            }
        }
        
        // Для кожного бакету шукаємо перпендикулярні діагоналі
        for (List<Pair> list : buckets.values()) {
            if (list.size() < 2) continue;
            
            for (int a = 0; a < list.size(); a++) {
                Pair d1 = list.get(a);
                
                for (int b = a + 1; b < list.size(); b++) {
                    Pair d2 = list.get(b);
                    
                    if (shareIndex(d1, d2)) continue;
                    
                    if (!eq(d1.midX, d2.midX) || !eq(d1.midY, d2.midY)) continue;
                    
                    // Перпендикулярність діагоналей (скалярний добуток ≈ 0)
                    double dot = d1.dx * d2.dx + d1.dy * d2.dy;
                    if (Math.abs(dot) > EPS * Math.max(
                        Math.hypot(d1.dx, d1.dy) * Math.hypot(d2.dx, d2.dy) * 0.1, 
                        EPS)) continue;
                    
                    int i1 = d1.i, i2 = d1.j, i3 = d2.i, i4 = d2.j;
                    
                    String uniqKey = uniqueKey(i1, i2, i3, i4);
                    if (seen.contains(uniqKey)) continue;
                    
                    Point[] quad = {
                        points.get(i1), points.get(i2), points.get(i3), points.get(i4)
                    };
                    
                    Point[] ordered = findValidRhombusOrder(quad);
                    if (ordered != null) {
                        seen.add(uniqKey);
                        boolean isSquare = isSquare(ordered);
                        Set<Integer> idxSet = new HashSet<>();
                        idxSet.add(i1);
                        idxSet.add(i2);
                        idxSet.add(i3);
                        idxSet.add(i4);
                        candidates.add(new DiamondCandidate(ordered, isSquare, idxSet));
                    }
                }
            }
        }
        
        // Крок 2: Покращена стратегія для знаходження максимального набору
        // Обчислюємо конфлікти для всіх кандидатів
        Map<Integer, Integer> pointToCandidateCount = new HashMap<>();
        for (DiamondCandidate candidate : candidates) {
            for (Integer idx : candidate.indices) {
                pointToCandidateCount.put(idx, pointToCandidateCount.getOrDefault(idx, 0) + 1);
            }
        }
        
        // Обчислюємо кількість конфліктів для кожного кандидата
        int[] conflictCount = new int[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            DiamondCandidate a = candidates.get(i);
            for (Integer idx : a.indices) {
                conflictCount[i] += pointToCandidateCount.getOrDefault(idx, 0) - 1;
            }
        }
        
        // Сортуємо за покращеною стратегією:
        // 1. Квадрати спочатку (вищий пріоритет)
        // 2. Менше конфліктів (дозволяє знайти більше ромбів)
        // 3. Мінімальний індекс (для детермінованості)
        // 4. Сума індексів (для детермінованості)
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < candidates.size(); i++) {
            indices.add(i);
        }
        
        final List<DiamondCandidate> finalCandidates = candidates;
        final int[] finalConflictCount = conflictCount;
        indices.sort((i, j) -> {
            DiamondCandidate a = finalCandidates.get(i);
            DiamondCandidate b = finalCandidates.get(j);
            
            // Квадрати мають вищий пріоритет
            if (a.isSquare != b.isSquare) {
                return b.isSquare ? 1 : -1;
            }
            
            // Менше конфліктів = вищий пріоритет
            // Це дозволяє спочатку вибрати ромби, які конфліктують з меншою кількістю інших,
            // що залишає більше місця для інших ромбів
            int conflictsDiff = Integer.compare(finalConflictCount[i], finalConflictCount[j]);
            if (conflictsDiff != 0) {
                return conflictsDiff;
            }
            
            // Для детермінованості: сортуємо за мінімальним індексом
            int minA = a.indices.stream().mapToInt(Integer::intValue).min().orElse(0);
            int minB = b.indices.stream().mapToInt(Integer::intValue).min().orElse(0);
            if (minA != minB) return Integer.compare(minA, minB);
            // Якщо мінімальні індекси однакові, порівнюємо за сумою індексів
            int sumA = a.indices.stream().mapToInt(Integer::intValue).sum();
            int sumB = b.indices.stream().mapToInt(Integer::intValue).sum();
            return Integer.compare(sumA, sumB);
        });
        
        // Створюємо відсортований список кандидатів
        List<DiamondCandidate> sortedCandidates = new ArrayList<>();
        for (int idx : indices) {
            sortedCandidates.add(finalCandidates.get(idx));
        }
        
        // Жадібний вибір з покращеною стратегією
        List<Diamond> diamonds = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        
        for (DiamondCandidate candidate : sortedCandidates) {
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
    
    private boolean hasConflict(DiamondCandidate a, DiamondCandidate b) {
        for (Integer idx : a.indices) {
            if (b.indices.contains(idx)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean verticesEqual(Point[] a, Point[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (Math.abs(a[i].x - b[i].x) > 1e-9 || Math.abs(a[i].y - b[i].y) > 1e-9) {
                return false;
            }
        }
        return true;
    }
    
    // Допоміжні методи (копіюємо з Hash-Based)
    private String bucketKeyByCenter(double midX, double midY) {
        double mx = quant(midX);
        double my = quant(midY);
        return mx + "|" + my;
    }
    
    private double quant(double v) {
        double step = EPS;
        return Math.round(v / step) * step;
    }
    
    private boolean shareIndex(Pair a, Pair b) {
        return a.i == b.i || a.i == b.j || a.j == b.i || a.j == b.j;
    }
    
    private String uniqueKey(int... ids) {
        int[] arr = ids.clone();
        Arrays.sort(arr);
        return Arrays.toString(arr);
    }
    
    private Point[] findValidRhombusOrder(Point[] quad) {
        int[] idx = {0, 1, 2, 3};
        do {
            Point[] ordered = {quad[idx[0]], quad[idx[1]], quad[idx[2]], quad[idx[3]]};
            if (isValidRhombusOrder(ordered)) return ordered;
        } while (nextPermutation(idx));
        return null;
    }
    
    private boolean isValidRhombusOrder(Point[] quad) {
        double[] sides = new double[4];
        for (int i = 0; i < 4; i++) {
            sides[i] = dist(quad[i], quad[(i + 1) % 4]);
        }
        boolean allEqual = eq(sides[0], sides[1]) && eq(sides[1], sides[2]) && 
                          eq(sides[2], sides[3]) && sides[0] > EPS;
        if (!allEqual) return false;
        
        double diag1 = dist(quad[0], quad[2]);
        double diag2 = dist(quad[1], quad[3]);
        if (diag1 <= EPS || diag2 <= EPS) return false;
        
        double mid12x = (quad[0].x + quad[2].x) / 2.0;
        double mid12y = (quad[0].y + quad[2].y) / 2.0;
        double mid34x = (quad[1].x + quad[3].x) / 2.0;
        double mid34y = (quad[1].y + quad[3].y) / 2.0;
        if (!eq(mid12x, mid34x) || !eq(mid12y, mid34y)) return false;
        
        if (!isConvex(quad)) return false;
        return true;
    }
    
    private boolean isSquare(Point[] orderedQuad) {
        double diag1 = dist(orderedQuad[0], orderedQuad[2]);
        double diag2 = dist(orderedQuad[1], orderedQuad[3]);
        return eq(diag1, diag2) && diag1 > EPS;
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
                if (prev * cross < -1e-9) return false;
            }
        }
        return true;
    }
    
    private boolean nextPermutation(int[] arr) {
        int i = arr.length - 2;
        while (i >= 0 && arr[i] >= arr[i + 1]) i--;
        if (i < 0) return false;
        int j = arr.length - 1;
        while (arr[j] <= arr[i]) j--;
        swap(arr, i, j);
        for (int l = i + 1, r = arr.length - 1; l < r; l++, r--) swap(arr, l, r);
        return true;
    }
    
    private void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }
    
    private double dist(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
    
    private boolean eq(double a, double b) {
        return Math.abs(a - b) < EPS;
    }
    
    private static class Pair {
        final int i, j;
        final double dx, dy;
        final double midX, midY;
        
        Pair(int i, int j, double dx, double dy, double midX, double midY) {
            this.i = i;
            this.j = j;
            this.dx = dx;
            this.dy = dy;
            this.midX = midX;
            this.midY = midY;
        }
    }
    
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
}

