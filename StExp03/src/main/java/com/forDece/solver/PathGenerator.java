package com.forDece.solver;

import lombok.Setter;

import java.util.*;

public class PathGenerator {
    private final List<List<Integer>> primePaths = new ArrayList<>();
    // 设置每个环的最大迭代次数
    @Setter
    private int maxLoopIterations = 1; // 每个环的最大迭代次数

    public PathGenerator(int[][] matrixCfg, int[][] matrixPrimePath) {
        // 构建控制流图
        for (int[] edge : matrixCfg) {
            Map<Integer, List<Integer>> cfg = new HashMap<>();
            cfg.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
        }

        // 转换基本路径格式
        for (int[] path : matrixPrimePath) {
            List<Integer> listPath = new ArrayList<>();
            for (int node : path) {
                listPath.add(node);
            }
            primePaths.add(listPath);
        }
    }

    // 生成测试路径
    public List<List<Integer>> generateTestPaths(int startNode,int endNode) {
        List<List<Integer>> testPaths = new ArrayList<>();

        // 找到所有以startNode开始的基本路径
        List<List<Integer>> startPaths = new ArrayList<>();
        for (List<Integer> path : primePaths) {
            if (path.getFirst() == startNode) {
                startPaths.add(path);
            }
        }

        // 对每个以startNode开始的基本路径，尝试扩展
        for (List<Integer> startPath : startPaths) {
            if (startPath.getLast() == endNode) {
                testPaths.add(new ArrayList<>(startPath));
                continue;
            }
            // 使用队列进行BFS，每个元素包含路径和环的迭代次数
            Queue<PathState> queue = new LinkedList<>();
            // 初始路径状态
            PathState initialState = new PathState(startPath, countLoopsInPath(startPath));
            queue.add(initialState);

            while (!queue.isEmpty()) {
                PathState currentState = queue.poll();
                List<Integer> currentPath = currentState.path;
                Map<List<Integer>, Integer> loopCounts = currentState.loopCounts;
                int lastNode = currentPath.getLast();

                // 找到所有可以连接的基本路径
                for (List<Integer> primePath : primePaths) {

                    if (primePath.getFirst() != lastNode) continue;
                    // 复制当前路径和环计数
                    List<Integer> newPath = new ArrayList<>(currentPath);
                    Map<List<Integer>, Integer> newLoopCounts = new HashMap<>(loopCounts);

                    // 跳过连接点(第一个节点)
                    for (int i = 1; i < primePath.size(); i++) {
                        newPath.add(primePath.get(i));
                    }

                    // 检查新路径中是否有环，并更新环计数
                    updateLoopCounts(newPath, primePath, newLoopCounts);

                    // 检查环计数是否超过限制
                    if (isLoopCountExceeded(newLoopCounts)) {
                        continue;
                    }

                    // 检查是否到达终点
                    int newLastNode = newPath.getLast();
                    if (newLastNode == endNode) {
                        testPaths.add(newPath);
                    } else {
                        // 添加到队列继续扩展
                        queue.add(new PathState(newPath, newLoopCounts));
                    }
                }
            }

        }

        return testPaths;
    }

    // 检测路径中的环并计数
    private Map<List<Integer>, Integer> countLoopsInPath(List<Integer> path) {
        Map<List<Integer>, Integer> loopCounts = new HashMap<>();

        // 简单环检测：查找形式为 A->B->...->A 的子路径
        for (int i = 0; i < path.size(); i++) {
            extractLoop(path, loopCounts, i);
        }

        return loopCounts;
    }

    private void extractLoop(List<Integer> path, Map<List<Integer>, Integer> loopCounts, int i) {
        int startNode = path.get(i);
        for (int j = i + 2; j < path.size(); j++) {
            if (path.get(j) == startNode) {
                // 提取环 [i, j]
                List<Integer> loop = path.subList(i, j + 1);
                loopCounts.put(loop, loopCounts.getOrDefault(loop, 0) + 1);
            }
        }
    }

    // 更新环计数
    private void updateLoopCounts(List<Integer> newPath, List<Integer> addedPath,
                                  Map<List<Integer>, Integer> loopCounts) {
        // 检查新添加的路径部分是否形成环
        int startIndex = newPath.size() - addedPath.size();

        // 简单环检测：查找形式为 A->B->...->A 的子路径
        for (int i = startIndex; i < newPath.size(); i++) {
            extractLoop(newPath, loopCounts, i);
        }
    }

    // 检查环计数是否超过限制
    private boolean isLoopCountExceeded(Map<List<Integer>, Integer> loopCounts) {
        for (int count : loopCounts.values()) {
            if (count > maxLoopIterations) {
                return true;
            }
        }
        return false;
    }

    // 内部类：表示路径状态（路径和环计数）
    private static class PathState {
        List<Integer> path;
        Map<List<Integer>, Integer> loopCounts;

        public PathState(List<Integer> path, Map<List<Integer>, Integer> loopCounts) {
            this.path = path;
            this.loopCounts = loopCounts;
        }
    }

    public static void main(String[] args) {
        int[][] matrixCfg = new int[][]{
                {0, 3}, {3, 4}, {4, 5}, {5, 4}, {4, 1}, {1, 7}, {7, 8}, {8, 9}, {9, 8}, {8, 2}
        };

        int[][] matrixPrimePath = new int[][]{
                {4, 5, 4},
                {5, 4, 5},
                {8, 9, 8},
                {9, 8, 9},
                {9, 8, 2},
                {5, 4, 1, 7, 8, 2},
                {0, 3, 4, 1, 7, 8, 2},
                {0, 3, 4, 5},
                {5, 4, 1, 7, 8, 9},
                {0, 3, 4, 1, 7, 8, 9}
        };

        PathGenerator generator = new PathGenerator(matrixCfg, matrixPrimePath);
        // 设置每个环的最大迭代次数
        generator.setMaxLoopIterations(1);
        List<List<Integer>> testPaths = generator.generateTestPaths(0,2);

        System.out.println("生成的测试路径:");
        for (List<Integer> path : testPaths) {
            System.out.println(path);
        }
    }
}