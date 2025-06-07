package com.forDece.solver;

import java.util.*;

public class PathGenerator {
    // 控制流图的邻接表表示
    private Map<Integer, List<Integer>> cfg = new HashMap<>();
    // 基本路径列表
    private List<List<Integer>> primePaths = new ArrayList<>();
    // 起点和终点
    private final int startNode = 0;
    private final int endNode = 2;
    
    public PathGenerator(int[][] matrixCfg, int[][] matrixPrimePath) {
        // 构建控制流图
        for (int[] edge : matrixCfg) {
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
    public List<List<Integer>> generateTestPaths() {
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
                // 如果路径直接到达endNode，直接添加
                testPaths.add(new ArrayList<>(startPath));
            } else {
                // 否则，尝试扩展路径
                Set<List<Integer>> visited = new HashSet<>();
                Queue<List<Integer>> queue = new LinkedList<>();
                queue.add(startPath);
                
                while (!queue.isEmpty()) {
                    List<Integer> currentPath = queue.poll();
                    int lastNode = currentPath.getLast();
                    
                    // 找到所有可以连接的基本路径
                    for (List<Integer> primePath : primePaths) {
                        if (primePath.getFirst() == lastNode) {
                            // 避免自环导致无限循环
                            List<Integer> newPath = new ArrayList<>(currentPath);
                            // 跳过连接点(第一个节点)
                            for (int i = 1; i < primePath.size(); i++) {
                                newPath.add(primePath.get(i));
                            }
                            
                            // 检查是否到达终点
                            int newLastNode = newPath.getLast();
                            if (newLastNode == endNode) {
                                testPaths.add(newPath);
                            } else if (newPath.size() < 20) { // 限制路径长度防止无限循环
                                if (!visited.contains(newPath)) {
                                    visited.add(newPath);
                                    queue.add(newPath);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return testPaths;
    }
    
    public static void main(String[] args) {
        int[][] matrixCfg = new int[][] {
            {0, 3}, {3, 4}, {4, 5}, {5, 4}, {4, 1}, {1, 7}, {7, 8}, {8, 9}, {9, 8}, {8, 2}
        };
        
        int[][] matrixPrimePath = new int[][] {
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
        List<List<Integer>> testPaths = generator.generateTestPaths();
        
        System.out.println("生成的测试路径:");
        for (List<Integer> path : testPaths) {
            System.out.println(path);
        }
    }
}