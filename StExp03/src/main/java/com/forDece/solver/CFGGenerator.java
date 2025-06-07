package com.forDece.solver;

import cn.edu.whu.cstar.testingcourse.cfgparser.ControlFlowNodeParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class CFGGenerator {
    public static List<Node> getNodes(String pathFile, String methodName) {
        ByteArrayOutputStream boStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            // 重定向标准输出到ByteArrayOutputStream
            System.setOut(new PrintStream(boStream));

            // 调用API生成控制流图结点信息
            new ControlFlowNodeParser().parseControlFlowNodes(pathFile, methodName);

            // 恢复标准输出
            System.setOut(originalOut);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        // 处理输出结果
        String[] lines = boStream.toString().split("\n");
        List<String> cfgNodes = Arrays.asList(lines);
        if (cfgNodes.isEmpty()) {
            System.out.println("未成功解析文件，请确认您的工作目录为 StExp03 或 文件相对路径是否设置正确");
            System.exit(-1);
        }
        return cfgNodes.stream()
                .skip(1)
                .map(Node::BuildNode).collect(Collectors.toList());

    }

    public static int[][] buildControlFlowGraph(List<Node> nodes) {
        List<List<Integer>> edges = new ArrayList<>();
        nodes.sort(Comparator.comparing(t -> t.lineNo));

        // 遍历每个结点，生成边
        for (Node node : nodes) {
            int currentId = node.nodeId;
            int parentId = node.parentId;
            String content = node.content;
            int currentLineNo = node.lineNo;

            switch (node.statement) {
                case "for-statement":
                    // for-statement指向for-condition（子结点中type为for-condition的结点）
                    Node conditionNode = getChildByType(nodes, currentId, "for-condition");
                    if (conditionNode != null) {
                        addEdge(edges, currentId, conditionNode.nodeId);
                    }
                    break;
                case "for-condition":
                    // for-condition的true分支指向for-body，false分支指向after-branch（parent为-1且id大于当前for-statement的结点）
                    Node bodyNode = getChildByType(nodes, parentId, "for-body");
                    Node exitNode = getNextAfterFor(nodes, currentLineNo); // 获取for循环后的分支结点（after-branch）
                    if (bodyNode != null) {
                        addEdge(edges, currentId, bodyNode.nodeId);
                        addEdge(edges, bodyNode.nodeId, currentId);
                    } // true分支
                    if (exitNode != null) addEdge(edges, currentId, exitNode.nodeId); // false分支
                    break;
                case "after-branch":
                    Node afterBranchNextNode = getNextNode(nodes, currentLineNo);
                    if (afterBranchNextNode != null && !afterBranchNextNode.statement.equals("pseudo-return")) {
                        addEdge(edges, currentId, afterBranchNextNode.nodeId);
                    }
                    break;
                case "first-statement":
                    // 入口结点指向下一个非分支结点（此处为第一个for-statement）
                    Node nextNode = getNextNode(nodes, currentLineNo);
                    if (nextNode != null && nextNode.statement.equals("for-statement")) {
                        addEdge(edges, currentId, nextNode.nodeId);
                    }
                    break;
                default:
                    // 跳过非for循环相关的结点
            }

        }

        // 去重（可选，根据文档是否允许重复边）
        edges = edges.stream()
                .distinct()
                .collect(Collectors.toList());

        // 转换为二维数组
        return edges.stream()
                .map(list -> list.stream().mapToInt(Integer::intValue).toArray())
                .toArray(int[][]::new);
    }

    // ------------------------- 辅助方法 ------------------------- //

    // 根据父结点ID和类型获取子结点
    private static Node getChildByType(List<Node> nodes, int parentId, String typeKeyword) {
        return nodes.stream()
                .filter(n -> n.parentId == parentId && n.content.contains(typeKeyword))
                .findFirst()
                .orElse(null);
    }

    // 获取for循环后的分支结点（after-branch，parentId=-1且id大于for-statement的id）
    private static Node getNextAfterFor(List<Node> nodes, int currentLineNo) {
        return nodes.stream()
                .filter(n -> n.lineNo >= currentLineNo)
                .filter(n -> n.parentId == -1)
                .filter(n -> n.content.startsWith("after-branch"))
                .findFirst()
                .orElse(null);
    }

    // 获取下一个顺序结点
    private static Node getNextNode(List<Node> nodes, int currentLineNo) {
        return nodes.stream()
                .filter(n -> n.lineNo > currentLineNo)
                .findFirst()
                .orElse(null);
    }

    // 安全添加边（避免自环和重复）
    private static void addEdge(List<List<Integer>> edges, int from, int to) {
        // ArrayList 重写了contains和equals
        if (from != to && !edges.contains(Arrays.asList(from, to))) {
            edges.add(Arrays.asList(from, to));
        }
    }
}