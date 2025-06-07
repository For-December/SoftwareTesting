package com.forDece.solver;

import java.util.*;

public class TestPathGenerator {
    // 控制流图边矩阵
    private static List<List<Integer>> edges = Arrays.asList(
        Arrays.asList(0, 3), Arrays.asList(3, 4), Arrays.asList(4, 5),
        Arrays.asList(5, 4), Arrays.asList(4, 1), Arrays.asList(1, 7),
        Arrays.asList(7, 8), Arrays.asList(8, 9), Arrays.asList(9, 8),
        Arrays.asList(8, 2)
    );

    // 邻接表
    private static Map<Integer, List<Integer>> adj = new HashMap<>();

    static {
        edges.forEach(edge -> {
            int from = edge.get(0);
            int to = edge.get(1);
            adj.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        });
    }

    public static void main(String[] args) {
        // 生成4条测试路径
        System.out.println("i. " + generatePath(0, 2, 0, 0)); // 无环
        System.out.println("ii. " + generatePath(0, 2, 0, 1)); // 8→9环1次
        System.out.println("iii. " + generatePath(0, 2, 1, 0)); // 4→5环1次
        System.out.println("iiii. " + generatePath(0, 2, 2, 2)); // 4→5环2次，8→9环2次
    }

    /**
     * 生成测试路径
     * @param start 起点
     * @param end 终点
     * @param loop45 4→5环展开次数
     * @param loop89 8→9环展开次数
     * @return 测试路径
     */
    private static List<Integer> generatePath(int start, int end, int loop45, int loop89) {
        List<Integer> path = new ArrayList<>();
        dfs(start, end, path, loop45, loop89, new HashSet<>());
        return path;
    }

    private static boolean dfs(int current, int end, List<Integer> path, int loop45, int loop89, Set<Integer> visited) {
        if (current == end) return true;
        if (visited.contains(current)) return false;
        
        visited.add(current);
        path.add(current);
        
        // 处理当前节点的边
        for (int neighbor : adj.getOrDefault(current, new ArrayList<>())) {
            // 优先处理非环边，除非需要展开环
            if (current == 4 && neighbor == 5 && loop45 > 0) {
                // 展开4→5环
                for (int i = 0; i < loop45; i++) {
                    path.add(5);
                    if (dfs(4, end, path, 0, loop89, new HashSet<>(visited))) return true; // 环内递归
                }
            } else if (current == 8 && neighbor == 9 && loop89 > 0) {
                // 展开8→9环
                for (int i = 0; i < loop89; i++) {
                    path.add(9);
                    if (dfs(8, end, path, loop45, 0, new HashSet<>(visited))) return true; // 环内递归
                }
            } else {
                if (dfs(neighbor, end, path, loop45, loop89, new HashSet<>(visited))) return true;
            }
        }
        
        visited.remove(current);
        path.remove(path.size() - 1);
        return false;
    }
}