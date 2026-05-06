package com.example.demo.algorithms;

import com.example.demo.model.Point;
import com.example.demo.model.Diamond;
import java.util.*;

/**
 * Алгоритм на основі хеш-таблиці для пошуку ромбів.
 * Складність: O(n²) в середньому за часом
 * Пам'ять: O(n²) для хеш-таблиці відстаней
 * 
 * Алгоритм групує точки за відстанями між ними,
 * що дозволяє швидко знаходити ромби.
 */
public class DiamondFinderHashBased {
    private static final double EPSILON = 0.5;
    
    public List<Diamond> findDiamonds(List<Point> points) {
        if (points.size() < 4) {
            return new ArrayList<>();
        }
        
        // Крок 1: Групуємо пари точок за спільним центром (потенційні діагоналі)
        // Для ромба діагоналі мають спільну середину; це дозволяє швидко шукати пари перпендикулярних діагоналей
        Map<String, List<DiagInfo>> midPointMap = buildMidPointMap(points);
        
        // Крок 2: Знайти всі можливі ромби через пари перпендикулярних діагоналей з однаковим центром
        List<DiamondCandidate> allCandidates = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (List<DiagInfo> diagsAtMid : midPointMap.values()) {
            for (int i = 0; i < diagsAtMid.size(); i++) {
                DiagInfo d1 = diagsAtMid.get(i);
                for (int j = i + 1; j < diagsAtMid.size(); j++) {
                    DiagInfo d2 = diagsAtMid.get(j);
                    
                    // Уникаємо спільних вершин
                    if (d1.sharesVertexWith(d2)) continue;
                    
                    // Перевіряємо перпендикулярність діагоналей: v1 · v2 ≈ 0
                    double dot = d1.dx * d2.dx + d1.dy * d2.dy;
                    double len1 = Math.hypot(d1.dx, d1.dy);
                    double len2 = Math.hypot(d2.dx, d2.dy);
                    if (len1 < EPSILON || len2 < EPSILON) continue;
                    double normDot = Math.abs(dot) / (len1 * len2);
                    if (normDot > 1e-3) continue; // майже перпендикулярні
                    
                    // Побудова ромба: точки по обидва боки від центру по кожній діагоналі
                    Point[] quad = {
                        points.get(d1.idx1), // mid + v1
                        points.get(d2.idx1), // mid + v2
                        points.get(d1.idx2), // mid - v1
                        points.get(d2.idx2)  // mid - v2
                    };
                    
                    String key = createKey(d1.idx1, d1.idx2, d2.idx1, d2.idx2);
                    if (processed.contains(key)) continue;
                    processed.add(key);
                    
                    Point[] orderedQuad = findValidRhombusOrder(quad);
                    if (orderedQuad != null) {
                        boolean isSquare = isSquare(orderedQuad);
                        Set<Integer> indices = new HashSet<>();
                        indices.add(d1.idx1);
                        indices.add(d1.idx2);
                        indices.add(d2.idx1);
                        indices.add(d2.idx2);
                        allCandidates.add(new DiamondCandidate(orderedQuad, isSquare, indices));
                    }
                }
            }
        }
        
        // Крок 3: Видалити дублікати
        List<DiamondCandidate> uniqueCandidates = removeDuplicates(allCandidates);
        
        // Крок 4: Вибрати максимальний набір
        return selectMaximumSet(uniqueCandidates);
    }
    
    private Map<String, List<DiagInfo>> buildMidPointMap(List<Point> points) {
        Map<String, List<DiagInfo>> map = new HashMap<>();
        
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                Point p1 = points.get(i);
                Point p2 = points.get(j);
                double midX = (p1.x + p2.x) / 2.0;
                double midY = (p1.y + p2.y) / 2.0;
                double dx = p1.x - midX;
                double dy = p1.y - midY; // вектор від центру до p1, p2 = -v
                
                String key = midKey(midX, midY);
                map.computeIfAbsent(key, k -> new ArrayList<>())
                   .add(new DiagInfo(i, j, dx, dy));
            }
        }
        return map;
    }

    private String midKey(double x, double y) {
        // Невелике округлення, щоб згрупувати майже однакові центри
        return String.format("%.2f:%.2f", x, y);
    }
    
    private String createKey(int a, int b, int c, int d) {
        List<Integer> indices = Arrays.asList(a, b, c, d);
        Collections.sort(indices);
        return indices.toString();
    }
    
    private Point[] findValidRhombusOrder(Point[] quad) {
        int[] idx = {0, 1, 2, 3};
        do {
            Point[] ordered = {
                quad[idx[0]], quad[idx[1]], quad[idx[2]], quad[idx[3]]
            };
            if (isValidRhombusOrder(ordered)) {
                return ordered;
            }
        } while (nextPermutation(idx));
        return null;
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
    
    private boolean isValidRhombusOrder(Point[] quad) {
        double[] sides = new double[4];
        for (int i = 0; i < 4; i++) {
            sides[i] = distance(quad[i], quad[(i + 1) % 4]);
        }
        
        if (eq(sides[0], sides[1]) && eq(sides[1], sides[2]) && eq(sides[2], sides[3]) && sides[0] > EPSILON) {
            double diag1 = distance(quad[0], quad[2]);
            double diag2 = distance(quad[1], quad[3]);
            if (diag1 > EPSILON && diag2 > EPSILON) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSquare(Point[] quad) {
        Point[] ordered = findValidRhombusOrder(quad);
        if (ordered == null) return false;
        
        double diag1 = distance(ordered[0], ordered[2]);
        double diag2 = distance(ordered[1], ordered[3]);
        return eq(diag1, diag2) && diag1 > EPSILON;
    }
    
    private List<DiamondCandidate> removeDuplicates(List<DiamondCandidate> candidates) {
        Map<String, DiamondCandidate> unique = new HashMap<>();
        for (DiamondCandidate candidate : candidates) {
            List<Integer> sortedIndices = new ArrayList<>(candidate.indices);
            Collections.sort(sortedIndices);
            String key = sortedIndices.toString();
            
            if (!unique.containsKey(key) || candidate.isSquare) {
                unique.put(key, candidate);
            }
        }
        return new ArrayList<>(unique.values());
    }
    
    private List<Diamond> selectMaximumSet(List<DiamondCandidate> candidates) {
        candidates.sort((a, b) -> {
            if (a.isSquare != b.isSquare) {
                return b.isSquare ? 1 : -1;
            }
            return 0;
        });
        
        List<Diamond> diamonds = new ArrayList<>();
        Set<Integer> used = new HashSet<>();
        
        for (DiamondCandidate candidate : candidates) {
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
    
    private double distance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }
    
    private boolean eq(double a, double b) {
        return Math.abs(a - b) < EPSILON;
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

    private static class DiagInfo {
        final int idx1, idx2;
        final double dx, dy; // вектор від центру до idx1 (idx2 = -v)

        DiagInfo(int idx1, int idx2, double dx, double dy) {
            this.idx1 = idx1;
            this.idx2 = idx2;
            this.dx = dx;
            this.dy = dy;
        }

        boolean sharesVertexWith(DiagInfo other) {
            return idx1 == other.idx1 || idx1 == other.idx2 || idx2 == other.idx1 || idx2 == other.idx2;
        }
    }
}

