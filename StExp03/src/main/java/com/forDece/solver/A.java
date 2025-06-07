package com.forDece.solver;


import java.util.*;
import java.util.stream.Collectors;

public class A {
    // 存储生成的主路径
    private static List<List<Integer>> primePaths = new ArrayList<>();
    // 邻接表表示图
    private static final Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
    private static int[][] matrixCfg;
    // 存储识别出的循环路径（主路径中的环）
    private static List<List<Integer>> cycles = new ArrayList<>();

    public static int[][] buildTestRequirements(int[][] cfg) {
        primePaths.clear();
        adjacencyList.clear();
        cycles.clear();
        matrixCfg = cfg;

        buildAdjacencyList();
        findAllCycles();
        findAllPaths();
        removeSubarrayPaths();
        identifyCyclesFromPrimePaths(); // 从主路径中识别循环

        // 生成测试路径
        List<List<Integer>> testPaths = generateTestPaths();

        return testPaths.stream()
                .map(path -> path.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new);
    }

    // 从主路径中识别循环路径（首尾节点相同的路径）
    private static void identifyCyclesFromPrimePaths() {
        cycles = primePaths.stream()
                .filter(path -> path.size() >= 3 && path.get(0).equals(path.get(path.size() - 1)))
                .collect(Collectors.toList());
    }

    // 生成测试路径（无循环、单循环、多循环）
    private static List<List<Integer>> generateTestPaths() {
        List<List<Integer>> testPaths = new ArrayList<>();
        List<List<Integer>> nonCyclePaths = primePaths.stream()
                .filter(path -> !isCycle(path))
                .collect(Collectors.toList());

        if (nonCyclePaths.isEmpty()) return testPaths;

        // 基础路径（最长非循环路径）
        List<Integer> basePath = nonCyclePaths.stream()
                .max(Comparator.comparingInt(List::size))
                .orElse(new ArrayList<>());

        // i. 无循环路径
        testPaths.add(new ArrayList<>(basePath));

        // ii. 仅包含第一个循环一次
        if (!cycles.isEmpty()) {
            List<Integer> cycle1 = cycles.get(0);
            testPaths.add(insertCycleIntoPath(basePath, cycle1, 1));
        }

        // iii. 仅包含第二个循环一次（若存在）
        if (cycles.size() >= 2) {
            List<Integer> cycle2 = cycles.get(1);
            testPaths.add(insertCycleIntoPath(basePath, cycle2, 1));
        }

        // iiii. 包含两个循环各两次（若存在）
        if (cycles.size() >= 2) {
            List<Integer> pathWithCycles = new ArrayList<>(basePath);
            pathWithCycles = insertCycleIntoPath(pathWithCycles, cycles.get(0), 2);
            pathWithCycles = insertCycleIntoPath(pathWithCycles, cycles.get(1), 2);
            testPaths.add(pathWithCycles);
        }

        return testPaths;
    }

    // 将循环插入到基础路径中
    private static List<Integer> insertCycleIntoPath(List<Integer> basePath, List<Integer> cycle, int loopCount) {
        if (cycle.isEmpty() || basePath.isEmpty()) return basePath;

        int startNode = cycle.get(0);
        int insertIndex = basePath.indexOf(startNode);
        if (insertIndex == -1) return basePath; // 循环起点不在基础路径中

        // 提取循环体（去除首尾节点，如 [4,5,4] 提取为 [5]）
        List<Integer> cycleBody = cycle.subList(1, cycle.size() - 1);
        List<Integer> newPath = new ArrayList<>(basePath);

        for (int i = 0; i < loopCount; i++) {
            newPath.addAll(insertIndex + 1, cycleBody);
        }

        return newPath;
    }

    // 判断是否为循环路径
    private static boolean isCycle(List<Integer> path) {
        return path.size() >= 3 && path.get(0).equals(path.get(path.size() - 1));
    }

    // 以下为原有代码（未修改部分）
    private static boolean isSubarray(List<Integer> sub, List<Integer> array) {
        int m = sub.size();
        int n = array.size();
        if (m > n) return false;

        for (int i = 0; i <= n - m; i++) {
            boolean match = true;
            for (int j = 0; j < m; j++) {
                if (!array.get(i + j).equals(sub.get(j))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }

    private static void removeSubarrayPaths() {
        primePaths.sort((a, b) -> Integer.compare(b.size(), a.size()));
        List<List<Integer>> filteredPaths = new ArrayList<>();
        for (List<Integer> path : primePaths) {
            boolean isSubarray = false;
            for (List<Integer> existing : filteredPaths) {
                if (isSubarray(path, existing)) {
                    isSubarray = true;
                    break;
                }
            }
            if (!isSubarray) {
                filteredPaths.add(path);
            }
        }
        primePaths = filteredPaths;
    }

    private static void buildAdjacencyList() {
        for (int[] edge : matrixCfg) {
            int from = edge[0];
            int to = edge[1];
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }
    }

    private static void findAllCycles() {
        for (int node : adjacencyList.keySet()) {
            Set<Integer> visited = new HashSet<>();
            List<Integer> path = new ArrayList<>();
            path.add(node);
            visited.add(node);
            dfsForCycle(node, node, visited, path);
        }
    }

    private static void dfsForCycle(int current, int start, Set<Integer> visited, List<Integer> path) {
        for (int neighbor : adjacencyList.getOrDefault(current, new ArrayList<>())) {
            if (neighbor == start && path.size() > 1) {
                List<Integer> cycle = new ArrayList<>(path);
                cycle.add(start);
                addPrimePath(cycle);
            } else if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                path.add(neighbor);
                dfsForCycle(neighbor, start, visited, path);
                path.remove(path.size() - 1);
                visited.remove(neighbor);
            }
        }
    }

    private static void findAllPaths() {
        Set<Integer> allNodes = new HashSet<>();
        for (int[] edge : matrixCfg) {
            allNodes.add(edge[0]);
            allNodes.add(edge[1]);
        }

        List<Integer> nodesList = new ArrayList<>(allNodes);
        for (int i = 0; i < nodesList.size(); i++) {
            for (int j = 0; j < nodesList.size(); j++) {
                if (i != j) {
                    int start = nodesList.get(i);
                    int end = nodesList.get(j);
                    Set<Integer> visited = new HashSet<>();
                    List<Integer> path = new ArrayList<>();
                    path.add(start);
                    visited.add(start);
                    dfsForPath(start, end, visited, path);
                }
            }
        }
    }

    private static void dfsForPath(int current, int end, Set<Integer> visited, List<Integer> path) {
        if (current == end && path.size() > 1) {
            addPrimePath(new ArrayList<>(path));
            return;
        }

        for (int neighbor : adjacencyList.getOrDefault(current, new ArrayList<>())) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                path.add(neighbor);
                dfsForPath(neighbor, end, visited, path);
                path.remove(path.size() - 1);
                visited.remove(neighbor);
            }
        }
    }

    private static void addPrimePath(List<Integer> path) {
        for (List<Integer> existingPath : primePaths) {
            if (existingPath.equals(path)) {
                return;
            }
        }
        primePaths.add(path);
    }

    public static void main(String[] args) {
        int[][] matrixCfg = {
                {0, 3}, {3, 4}, {4, 5}, {5, 4}, {4, 1}, {1, 7}, {7, 8}, {8, 9}, {9, 8}, {8, 2}
        };

        PrimePathGenerator generator = new PrimePathGenerator();
        int[][] testPaths = generator.buildTestRequirements(matrixCfg);

        // 输出结果
        Arrays.stream(testPaths).forEach(path -> System.out.println(Arrays.toString(path)));
    }
}