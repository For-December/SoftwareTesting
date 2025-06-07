package com.forDece.solver;

import java.util.*;

import java.util.*;

public class PrimePathGenerator {
    // CFG矩阵表示的边
    private static int[][] matrixCfg = new int[][] {
            {0, 3}, {3, 4}, {4, 5}, {5, 4}, {4, 1}, {1, 7}, {7, 8}, {8, 9}, {9, 8}, {8, 2}
    };

    // 存储生成的主路径
    private static List<List<Integer>> primePaths = new ArrayList<>();

    // 邻接表表示图
    private static Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

    // 判断是否为连续子数组
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
    // 移除作为子数组的短路径
    private static void removeSubarrayPaths() {
        // 按路径长度降序排序，长路径优先保留
        primePaths.sort((a, b) -> Integer.compare(b.size(), a.size()));

        List<List<Integer>> filteredPaths = new ArrayList<>();
        for (List<Integer> path : primePaths) {
            // 检查是否已被更长路径包含
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
    public static void main(String[] args) {
        // 构建邻接表
        buildAdjacencyList();

        // 寻找所有环作为主路径
        findAllCycles();

        // 寻找所有可能的路径作为主路径
        findAllPaths();

        removeSubarrayPaths();

        // 输出主路径矩阵
        printPrimePaths();
    }

    // 构建邻接表
    private static void buildAdjacencyList() {
        for (int[] edge : matrixCfg) {
            int from = edge[0];
            int to = edge[1];
            adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        }
    }

    // 寻找所有环
    private static void findAllCycles() {
        for (int node : adjacencyList.keySet()) {
            Set<Integer> visited = new HashSet<>();
            List<Integer> path = new ArrayList<>();
            path.add(node);
            visited.add(node);
            dfsForCycle(node, node, visited, path);
        }
    }

    // 深度优先搜索寻找环
    private static void dfsForCycle(int current, int start, Set<Integer> visited, List<Integer> path) {
        for (int neighbor : adjacencyList.getOrDefault(current, new ArrayList<>())) {
            if (neighbor == start && path.size() > 1) {
                // 找到环
                List<Integer> cycle = new ArrayList<>(path);
                cycle.add(start);
                addPrimePath(cycle);
            } else if (!visited.contains(neighbor)) {
                // 继续搜索
                visited.add(neighbor);
                path.add(neighbor);
                dfsForCycle(neighbor, start, visited, path);
                path.removeLast();
                visited.remove(neighbor);
            }
        }
    }

    // 寻找所有可能的路径
    private static void findAllPaths() {
        // 找出所有可能的起点和终点
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

    // 深度优先搜索寻找路径
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
                path.removeLast();
                visited.remove(neighbor);
            }
        }
    }

    // 添加主路径
    private static void addPrimePath(List<Integer> path) {
        // 检查是否已经存在相同的路径
        for (List<Integer> existingPath : primePaths) {
            if (existingPath.equals(path)) {
                return;
            }
        }

        // 添加新路径
        primePaths.add(path);
    }

    // 输出主路径矩阵
    private static void printPrimePaths() {
        System.out.println("private int[][] matrixPrimePath = new int[][] {");
        for (List<Integer> path : primePaths) {
            System.out.print("    {");
            for (int i = 0; i < path.size(); i++) {
                System.out.print(path.get(i));
                if (i < path.size() - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println("},");
        }
        System.out.println("};");
    }
}