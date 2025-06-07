package com.forDece.solver;


import lombok.Builder;

// Node类定义（简化版，仅包含必要字段）
@Builder
public class Node implements Comparable<Node> {
    int nodeId;       // 结点编号
    int parentId;     // 父结点编号
    String content;   // 内容
    String statement;
    int lineNo;

    public static Node BuildNode(String nodeStr) {
        String[] split = nodeStr.split("\t");
        if (split.length != 9) {
            System.out.println("生成的 cfg 表格长度不合法");
            System.exit(-1);
        }

        int nodeId = Integer.parseInt(split[1]);
        int parentId = Integer.parseInt(split[2]);
        int lineNo = Integer.parseInt(split[4]);
        String content = split[8];
        String statement = content.split("@")[0];

        return Node.builder()
                .nodeId(nodeId)
                .parentId(parentId)
                .content(content)
                .statement(statement)
                .lineNo(lineNo)
                .build();
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.lineNo, other.lineNo);
    }
}