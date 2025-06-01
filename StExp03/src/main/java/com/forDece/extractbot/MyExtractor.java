package com.forDece.extractbot;


import cn.edu.whu.cstar.testingcourse.cfgparser.ControlFlowNodeParser;
import extractbot.tool.BaseExtractor;

import java.io.*;
import java.util.*;

public class MyExtractor extends BaseExtractor{



	// 存储结点ID到行号的映射
	private Map<Integer, Integer> nodeIdToLineNumber = new HashMap<>();
	// 存储行号到结点ID的映射
	private Map<Integer, Integer> lineNumberToNodeId = new HashMap<>();
	// 存储方法起始行号
	private int methodStartLine = -1;
	// 存储方法结束行号
	private int methodEndLine = -1;

	@Override
	public int[][] getControlFlowGraphInArray(String pathFile, String methodName) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream originalOut = System.out;

		try {
			// 重定向标准输出到ByteArrayOutputStream
			System.setOut(new PrintStream(baos));

			// 调用API生成控制流图结点信息
			new ControlFlowNodeParser().parseControlFlowNodes(pathFile, methodName);

			// 恢复标准输出
			System.setOut(originalOut);

			// 处理输出结果
			String[] lines = baos.toString().split("\n");
			List<String> cfgNodes = Arrays.asList(lines);

			if (cfgNodes == null || cfgNodes.isEmpty()) {
				return new int[0][0];
			}

			// 解析控制流图结点信息
			Map<Integer, List<Integer>> adjacencyList = parseCFGNodes(cfgNodes);

			// 将邻接表转换为边的二维数组
			List<int[]> edges = new ArrayList<>();
			for (Map.Entry<Integer, List<Integer>> entry : adjacencyList.entrySet()) {
				int source = entry.getKey();
				for (int target : entry.getValue()) {
					edges.add(new int[]{source, target});
				}
			}

			return edges.toArray(new int[0][]);
		} catch (Exception e) {
			// 确保标准输出恢复
			System.setOut(originalOut);
			e.printStackTrace();
			return new int[0][0];
		}
	}

	/**
	 * 解析控制流图结点信息，构建邻接表
	 */
	private Map<Integer, List<Integer>> parseCFGNodes(List<String> cfgNodes) {
		Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
		Map<Integer, Integer> nodeIdToParent = new HashMap<>();
		Map<Integer, String> nodeIdToType = new HashMap<>();
		Map<Integer, Integer> nodeIdToHeight = new HashMap<>();

		// 解析每行结点信息
		for (String line : cfgNodes) {
			if (line.startsWith("method") || line.trim().isEmpty()) continue; // 跳过标题行和空行

			String[] parts = line.trim().split("\t");
			if (parts.length < 10) continue; // 跳过不完整的行

			try {
				int nodeId = Integer.parseInt(parts[1]);
				int parentId = Integer.parseInt(parts[2]);
				int height = Integer.parseInt(parts[3]);
				String content = parts[9];

				// 提取结点类型（content中@前的部分）
				String nodeType = content.contains("@") ?
						content.substring(0, content.indexOf('@')) : content;

				// 存储结点信息
				nodeIdToParent.put(nodeId, parentId);
				nodeIdToType.put(nodeId, nodeType);
				nodeIdToHeight.put(nodeId, height);
				adjacencyList.putIfAbsent(nodeId, new ArrayList<>());

				// 解析行号信息
				int startLine = Integer.parseInt(parts[5]);
				nodeIdToLineNumber.put(nodeId, startLine);
				lineNumberToNodeId.put(startLine, nodeId);

				// 特殊处理：记录方法起始和结束行
				if (nodeType.contains("method-start")) {
					methodStartLine = startLine;
				} else if (nodeType.contains("pseudo-return")) {
					methodEndLine = startLine;
				}
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				e.printStackTrace();
				continue;
			}
		}

		// 构建边关系
		for (int nodeId : adjacencyList.keySet()) {
			String nodeType = nodeIdToType.get(nodeId);
			int parentId = nodeIdToParent.get(nodeId);
			int height = nodeIdToHeight.get(nodeId);

			// 处理for循环
			if (nodeType.startsWith("for")) {
				int conditionNodeId = nodeId + 1; // for-condition
				int bodyNodeId = nodeId + 2;      // for-body
				int updateNodeId = nodeId + 3;    // for-update

				// for语句 → for-condition
				adjacencyList.get(nodeId).add(conditionNodeId);

				// for-condition → for-body (true分支)
				adjacencyList.get(conditionNodeId).add(bodyNodeId);

				// for-condition → 后续语句 (false分支)
				int nextNodeId = findNextNodeAtSameLevel(nodeId, adjacencyList, nodeIdToHeight);
				if (nextNodeId != -1) {
					adjacencyList.get(conditionNodeId).add(nextNodeId);
				}

				// for-body → for-update
				adjacencyList.get(bodyNodeId).add(updateNodeId);

				// for-update → for-condition
				adjacencyList.get(updateNodeId).add(conditionNodeId);
			}
			// 处理普通语句（顺序执行）
			else {
				int nextNodeId = nodeId + 1;

				// 如果下一个结点存在且高度相同（同一层级）
				if (adjacencyList.containsKey(nextNodeId) &&
						nodeIdToHeight.get(nextNodeId) == height) {
					adjacencyList.get(nodeId).add(nextNodeId);
				}
				// 否则查找同层级的下一个结点
				else {
					nextNodeId = findNextNodeAtSameLevel(nodeId, adjacencyList, nodeIdToHeight);
					if (nextNodeId != -1) {
						adjacencyList.get(nodeId).add(nextNodeId);
					}
				}
			}
		}

		return adjacencyList;
	}

	/**
	 * 查找同层级的下一个结点
	 */
	private int findNextNodeAtSameLevel(int nodeId,
										Map<Integer, List<Integer>> adjacencyList,
										Map<Integer, Integer> nodeIdToHeight) {
		int currentHeight = nodeIdToHeight.get(nodeId);
		int nextNodeId = nodeId + 1;

		while (adjacencyList.containsKey(nextNodeId)) {
			if (nodeIdToHeight.get(nextNodeId) == currentHeight) {
				return nextNodeId;
			}
			nextNodeId++;
		}

		return -1; // 未找到同层级结点
	}


	@Override
	public int[][] getTestRequirementsInArray(String pathFile, String methodName)
	{
		/** TODO
		 * 	Jifeng: Write down your source code here.
		 */

		return null;
	}


	@Override
	public int[][] getTestPathsInArray(String pathFile, String methodName)
	{
		/** TODO
		 * 	Jifeng: Write down your source code here.
		 */

		return null;
	}

}
