package com.example.demo.algorithms;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import java.util.*;

/**
 * Жадібний алгоритм пошуку ромбів.
 * Спочатку знаходить всі можливі ромби, потім вибирає оптимальний набір
 * жадібно (з пріоритетом квадратів).
 * Складність: O(n²) для побудови пар + сума O(k²) по бакетах
 * Пам'ять: O(n²) для хеш-таблиці
 */
public class DiamondFinderGreedy {
    private static final double EPSILON = 0.5; // похибка до 0.5 пікселя
    
    public List<Diamond> findDiamonds(List<Point> points) {
        if (points.size() < 4) {
            return new ArrayList<>();
        }
        
        // Крок 1: Знайти всі можливі ромби (як Hash-Based)
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
                if (len2 < EPSILON * EPSILON) continue;
                
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
                    
                    // Унікальні індекси
                    if (shareIndex(d1, d2)) continue;
                    
                    // Перевіряємо, чи діагоналі перетинаються в одній точці
                    if (!eq(d1.midX, d2.midX) || !eq(d1.midY, d2.midY)) continue;
                    
                    // Перпендикулярність діагоналей
                    double dot = d1.dx * d2.dx + d1.dy * d2.dy;
                    double len1 = Math.hypot(d1.dx, d1.dy);
                    double len2 = Math.hypot(d2.dx, d2.dy);
                    if (Math.abs(dot) > EPSILON * Math.max(len1 * len2 * 0.1, EPSILON)) continue;
                    
                    int i1 = d1.i, i2 = d1.j, i3 = d2.i, i4 = d2.j;
                    
                    // Ключ для унікальності
                    String uniqKey = uniqueKey(i1, i2, i3, i4);
                    if (seen.contains(uniqKey)) continue;
                    
                    Point[] quad = {
                        points.get(i1),
                        points.get(i2),
                        points.get(i3),
                        points.get(i4)
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
        
        // Крок 2: Жадібний вибір оптимального набору (з пріоритетом квадратів)
        // Детерміноване сортування для однакових результатів
        candidates.sort((a, b) -> {
            if (a.isSquare != b.isSquare) {
                return b.isSquare ? 1 : -1; // квадрати спочатку
            }
            // Для однакового типу сортуємо за мінімальним індексом (для стабільності)
            int minA = a.indices.stream().mapToInt(Integer::intValue).min().orElse(0);
            int minB = b.indices.stream().mapToInt(Integer::intValue).min().orElse(0);
            if (minA != minB) return Integer.compare(minA, minB);
            // Якщо мінімальні індекси однакові, порівнюємо за сумою індексів
            int sumA = a.indices.stream().mapToInt(Integer::intValue).sum();
            int sumB = b.indices.stream().mapToInt(Integer::intValue).sum();
            return Integer.compare(sumA, sumB);
        });
        
        List<Diamond> diamonds = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        
        for (DiamondCandidate candidate : candidates) {
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
    
    private String uniqueKey(int... ids) {
        int[] arr = ids.clone();
        Arrays.sort(arr);
        return Arrays.toString(arr);
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
    
    private String bucketKeyByCenter(double midX, double midY) {
        double mx = quant(midX);
        double my = quant(midY);
        return mx + "|" + my;
    }
    
    private double quant(double v) {
        double step = EPSILON;
        return Math.round(v / step) * step;
    }
    
    private boolean shareIndex(Pair a, Pair b) {
        return a.i == b.i || a.i == b.j || a.j == b.i || a.j == b.j;
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
    
    private Point[] findValidRhombusOrder(Point[] quad) {
        int[] idx = {0, 1, 2, 3};
        do {
            Point[] ordered = {quad[idx[0]], quad[idx[1]], quad[idx[2]], quad[idx[3]]};
            if (isValidRhombusOrder(ordered)) {
                return ordered;
            }
        } while (nextPermutation(idx));
        return null;
    }

    private boolean isValidRhombusOrder(Point[] quad) {
        double[] sides = new double[4];
        for (int i = 0; i < 4; i++) {
            sides[i] = distance(quad[i], quad[(i + 1) % 4]);
        }
        boolean allEqual = eq(sides[0], sides[1]) && eq(sides[1], sides[2]) && eq(sides[2], sides[3]) && sides[0] > EPSILON;
        if (!allEqual) return false;

        double diag1 = distance(quad[0], quad[2]);
        double diag2 = distance(quad[1], quad[3]);
        if (diag1 <= EPSILON || diag2 <= EPSILON) return false;

        // Діагоналі повинні мати спільний центр
        double mid12x = (quad[0].x + quad[2].x) / 2.0;
        double mid12y = (quad[0].y + quad[2].y) / 2.0;
        double mid34x = (quad[1].x + quad[3].x) / 2.0;
        double mid34y = (quad[1].y + quad[3].y) / 2.0;
        if (!eq(mid12x, mid34x) || !eq(mid12y, mid34y)) return false;

        // Перевіряємо опуклість, щоб уникнути самоперетину
        if (!isConvex(quad)) return false;

        return true;
    }

    /**
     * Перевіряє, чи ромб є квадратом (діагоналі рівні)
     */
    private boolean isSquare(Point[] orderedQuad) {
        double diag1 = distance(orderedQuad[0], orderedQuad[2]);
        double diag2 = distance(orderedQuad[1], orderedQuad[3]);
        return eq(diag1, diag2) && diag1 > EPSILON;
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
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
    private double distance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
    
    private boolean eq(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }
    
    /**
     * Допоміжний клас для зберігання пари точок та відстані між ними
     */
    private static class PointPair {
        final int idx1, idx2;
        final double distance;
        
        PointPair(int idx1, int idx2, double distance) {
            this.idx1 = idx1;
            this.idx2 = idx2;
            this.distance = distance;
        }
    }
}



