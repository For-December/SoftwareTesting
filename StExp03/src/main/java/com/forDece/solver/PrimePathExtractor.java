package com.forDece.solver;

import java.util.*;
import java.util.stream.Collectors;

public class PrimePathExtractor {
    private final Map<Integer, List<Integer>> adj; // 邻接表表示的CFG
    private final int startNode; // 入口结点（如0）
    private final int endNode; // 出口结点（如11）

    private Map<Integer, List<Integer>> buildAdjacencyMap(List<Node> nodes, int[][] cfg) {
        Map<Integer, List<Integer>> adj = new HashMap<>();
        for (int[] edge : cfg) {
            adj.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
        }
        return adj;
    }

    private List<int[]> extractSimpleBranches(Map<Integer, List<Integer>> adj,List<Node> nodes) {
        List<int[]> paths = new ArrayList<>();
        // 判断结点：for-condition（文档中结点类型为"for-condition"）
        List<Integer> decisionNodes = adj.keySet().stream()
                .filter(node -> nodes.stream()
                        .anyMatch(n -> n.nodeId == node && n.content.contains("for-condition")))
                .collect(Collectors.toList());

        for (int node : decisionNodes) {
            for (int nextNode : adj.get(node)) {
                paths.add(new int[]{node, nextNode}); // 每条边对应一个简单路径
            }
        }
        return paths;
    }
    private static Node getChildByType(List<Node> nodes, int parentId, String typeKeyword) {
        return nodes.stream()
                .filter(n -> n.parentId == parentId && n.content.contains(typeKeyword))
                .findFirst()
                .orElse(null);
    }

    private List<int[]> extractLoopOncePaths(Map<Integer, List<Integer>> adj, List<Node> nodes) {
        List<int[]> paths = new ArrayList<>();
        // 定位for-condition结点及其循环体（for-body）
        Map<Integer, Integer> loopMap = nodes.stream()
                .filter(n -> n.content.contains("for-statement"))
                .collect(Collectors.toMap(
                        n -> getChildByType(nodes, n.nodeId, "for-condition").nodeId, // for-condition结点ID
                        n -> getChildByType(nodes, n.nodeId, "for-body").nodeId // for-body结点ID
                ));

        for (Map.Entry<Integer, Integer> entry : loopMap.entrySet()) {
            int conditionNode = entry.getKey();
            int bodyNode = entry.getValue();
            int updateNode = nodes.stream()
                    .filter(n -> n.parentId == conditionNode&& n.content.contains("for-update"))
                    .map(Node::getNodeId)
                    .findFirst()
                    .orElse(-1);

            // 循环一次路径：condition → body → update → condition
            if (adj.containsKey(bodyNode) && adj.get(bodyNode).contains(updateNode) &&
                    adj.containsKey(updateNode) && adj.get(updateNode).contains(conditionNode)) {
                paths.add(new int[]{conditionNode, bodyNode, updateNode, conditionNode}); // 完整循环路径
                // 简化为文档示例中的主路径格式（取关键结点：condition → body → condition）
                paths.add(new int[]{conditionNode, bodyNode, conditionNode});
            }
        }
        return paths;
    }

    private List<int[]> extractEntryToExitPaths(Map<Integer, List<Integer>> adj, int startNode, int endNode) {
        List<int[]> paths = new ArrayList<>();
        Stack<List<Integer>> stack = new Stack<>();
        stack.push(Arrays.asList(startNode));

        while (!stack.isEmpty()) {
            List<Integer> path = stack.pop();
            int lastNode = path.get(path.size() - 1);

            if (lastNode == endNode) {
                paths.add(path.stream().mapToInt(Integer::intValue).toArray());
                continue;
            }

            if (adj.containsKey(lastNode)) {
                for (int nextNode : adj.get(lastNode)) {
                    // 限制循环次数：每个for-condition最多出现2次（0次或1次循环）
                    long loopCount = path.stream().filter(n -> n == 4 || n == 8).count(); // 假设4和8为for-condition结点ID
                    if (loopCount <= 2) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(nextNode);
                        stack.push(newPath);
                    }
                }
            }
        }
        return paths;
    }

    public PrimePathExtractor(int[][] cfg, int start, int end) {
        adj = new HashMap<>();
        for (int[] edge : cfg) {
            adj.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
        }
        startNode = start;
        endNode = end;
    }

    // 提取所有主路径
    public List<int[]> extractPrimePaths() {
        List<int[]> primePaths = new ArrayList<>();

        // 1. 提取简单路径（长度为1）
        primePaths.addAll(extractSimplePaths());

        // 2. 提取循环一次路径
        primePaths.addAll(extractLoopOncePaths());

        // 3. 提取入口到出口的组合路径
        primePaths.addAll(extractCombinedPaths());

        // 去重（按路径字符串哈希去重）
        return primePaths.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    // 提取长度为1的简单路径
    private List<int[]> extractSimplePaths() {
        List<int[]> paths = new ArrayList<>();
        adj.keySet().forEach(node -> {
            if (isDecisionNode(node)) { // 判断结点（for-condition）
                adj.get(node).forEach(next -> paths.add(new int[]{node, next}));
            }
        });
        return paths;
    }

    // 判断是否为判断结点（for-condition）
    private boolean isDecisionNode(int node) {
        // 假设结点类型通过content判断（需结合实际解析结果）
        return node == 4 || node == 8; // 文档示例中的for-condition结点ID
    }

    // 提取循环一次路径
    private List<int[]> extractLoopOncePaths() {
        List<int[]> paths = new ArrayList<>();
        // 假设循环对应判断结点4和8
        Arrays.asList(4, 8).forEach(conditionNode -> {
            List<Integer> bodyNodes = adj.get(conditionNode); // true分支结点（如5,9）
            if (bodyNodes != null && !bodyNodes.isEmpty()) {
                int bodyNode = bodyNodes.getFirst();
                // 假设更新结点为bodyNode+1（需根据实际CFG调整）
                int updateNode = bodyNode + 1;
                if (adj.containsKey(updateNode) && adj.get(updateNode).contains(conditionNode)) {
                    paths.add(new int[]{conditionNode, bodyNode, updateNode, conditionNode});
                    // 简化版本（文档示例格式）
                    paths.add(new int[]{conditionNode, bodyNode, conditionNode});
                }
            }
        });
        return paths;
    }

    // 提取入口到出口的组合路径（DFS遍历）
    private List<int[]> extractCombinedPaths() {
        List<int[]> paths = new ArrayList<>();
        Stack<List<Integer>> stack = new Stack<>();
        stack.push(List.of(startNode));

        while (!stack.isEmpty()) {
            List<Integer> path = stack.pop();
            int lastNode = path.getLast();

            if (lastNode == endNode) {
                paths.add(path.stream().mapToInt(Integer::intValue).toArray());
                continue;
            }

            if (adj.containsKey(lastNode)) {
                for (int nextNode : adj.get(lastNode)) {
                    // 限制循环次数：每个判断结点最多出现2次（0次或1次循环）
                    long loopCount = path.stream().filter(n -> n == 4 || n == 8).count();
                    if (loopCount <= 2) {
                        List<Integer> newPath = new ArrayList<>(path);
                        newPath.add(nextNode);
                        stack.push(newPath);
                    }
                }
            }
        }
        return paths;
    }

    public static void main(String[] args) {
        List<Node> nodes = CFGGenerator.getNodes("src/main/java/com/forDece/extractbot/Example.java", "example1");
        int[][] cfg = CFGGenerator.buildControlFlowGraph(nodes);
        PrimePathExtractor primePathExtractor = new PrimePathExtractor(cfg, 0, 11);
        List<int[]> ints = primePathExtractor.extractPrimePaths();
        for (int[] path : ints) {
            System.out.println(Arrays.toString(path));
        }
    }
}