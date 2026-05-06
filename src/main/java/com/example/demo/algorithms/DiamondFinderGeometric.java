    package com.example.demo.algorithms;

    import com.example.demo.model.Point;
    import com.example.demo.model.Diamond;
    import java.util.*;

    public class DiamondFinderGeometric {
        private static final double EPSILON = 0.5;

        public List<Diamond> findDiamonds(List<Point> points) {
            if (points.size() < 4) {
                return new ArrayList<>();
            }

            // Крок 1: Знайти всі можливі ромби через геометричні властивості
            List<DiamondCandidate> allCandidates = new ArrayList<>();
            Set<String> processed = new HashSet<>();

            // Для кожної пари точок (потенційна діагональ)
            for (int i = 0; i < points.size(); i++) {
                for (int j = i + 1; j < points.size(); j++) {
                    Point p1 = points.get(i);
                    Point p2 = points.get(j);
                    double diagLen = distance(p1, p2);

                    if (diagLen < EPSILON) continue;

                    // Знаходимо середину діагоналі
                    double midX = (p1.x + p2.x) / 2.0;
                    double midY = (p1.y + p2.y) / 2.0;
                    Point mid = new Point(midX, midY);

                    // Вектор діагоналі
                    double dx = p2.x - p1.x;
                    double dy = p2.y - p1.y;

                    // Перпендикулярний вектор (для пошуку іншої діагоналі)
                    double perpX = -dy;
                    double perpY = dx;

                    // Шукаємо дві інші точки, які лежать на перпендикулярній бісектрисі
                    // і на однаковій відстані від середини
                    Map<String, List<Integer>> distanceFromMid = new HashMap<>();

                    for (int k = 0; k < points.size(); k++) {
                        if (k == i || k == j) continue;

                        Point p3 = points.get(k);
                        double distFromMid = distance(mid, p3);

                        if (distFromMid < EPSILON) continue;

                        // Перевіряємо, чи точка лежить на перпендикулярній бісектрисі
                        // (приблизно, з урахуванням похибки)
                        double distToLine = distanceToPerpendicularLine(p1, p2, p3);

                        if (distToLine < EPSILON * 2) { // більша похибка для геометричного методу
                            String key = String.format("%.1f", distFromMid);
                            distanceFromMid.computeIfAbsent(key, k2 -> new ArrayList<>()).add(k);
                        }
                    }

                    // Шукаємо пари точок на однаковій відстані від середини
                    for (List<Integer> pointIndices : distanceFromMid.values()) {
                        if (pointIndices.size() < 2) continue;

                        for (int idx1 = 0; idx1 < pointIndices.size(); idx1++) {
                            for (int idx2 = idx1 + 1; idx2 < pointIndices.size(); idx2++) {
                                int k = pointIndices.get(idx1);
                                int l = pointIndices.get(idx2);

                                Point p3 = points.get(k);
                                Point p4 = points.get(l);

                                // Перевіряємо, чи утворюється ромб
                                Point[] quad = {p1, p3, p2, p4};
                                String key = createKey(i, j, k, l);
                                if (processed.contains(key)) continue;
                                processed.add(key);

                                Point[] orderedQuad = findValidRhombusOrder(quad);
                                if (orderedQuad != null) {
                                    boolean isSquare = isSquare(orderedQuad);
                                    Set<Integer> indices = new HashSet<>();
                                    indices.add(i);
                                    indices.add(j);
                                    indices.add(k);
                                    indices.add(l);
                                    allCandidates.add(new DiamondCandidate(orderedQuad, isSquare, indices));
                                }
                            }
                        }
                    }
                }
            }

            // Крок 2: Видалити дублікати
            List<DiamondCandidate> uniqueCandidates = removeDuplicates(allCandidates);

            // Крок 3: Вибрати максимальний набір
            return selectMaximumSet(uniqueCandidates);
        }

        // Відстань від точки до перпендикулярної бісектриси діагоналі
        private double distanceToPerpendicularLine(Point p1, Point p2, Point p3) {
            // Середина діагоналі
            double midX = (p1.x + p2.x) / 2.0;
            double midY = (p1.y + p2.y) / 2.0;

            // Вектор діагоналі
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;

            // Вектор від середини до точки
            double vx = p3.x - midX;
            double vy = p3.y - midY;

            double len = Math.hypot(dx, dy);
            if (len < EPSILON) return Double.MAX_VALUE;

            // Відстань до перпендикулярної бісектриси = |(v·d)|/|d|
            return Math.abs(vx * dx + vy * dy) / len;
        }

        private String createKey(int a, int b, int c, int d) {
            List<Integer> indices = Arrays.asList(a, b, c, d);
            Collections.sort(indices);
            return indices.toString();
        }

        private Point[] findValidRhombusOrder(Point[] quad) {
            // Перебираємо всі перестановки, бо стартові точки діагоналей невідомі
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

            // Всі сторони рівні та ненульові
            boolean allEqual = eq(sides[0], sides[1]) && eq(sides[1], sides[2]) && eq(sides[2], sides[3]) && sides[0] > EPSILON;
            if (!allEqual) return false;

            // Діагоналі ненульові і мають спільний центр
                double diag1 = distance(quad[0], quad[2]);
                double diag2 = distance(quad[1], quad[3]);
            if (diag1 <= EPSILON || diag2 <= EPSILON) return false;
            double mid12x = (quad[0].x + quad[2].x) / 2.0;
            double mid12y = (quad[0].y + quad[2].y) / 2.0;
            double mid34x = (quad[1].x + quad[3].x) / 2.0;
            double mid34y = (quad[1].y + quad[3].y) / 2.0;
            if (!eq(mid12x, mid34x) || !eq(mid12y, mid34y)) return false;

            // Опуклість, щоб уникнути «бантиків»
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
    }

