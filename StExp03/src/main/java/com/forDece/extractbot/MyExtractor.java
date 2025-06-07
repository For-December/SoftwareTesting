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
		// 收集所有结点ID
		Set<Integer> allNodeIds = new HashSet<>();
		// 存储结点ID到类型的映射
		Map<Integer, String> nodeIdToType = new HashMap<>();
		// 存储结点ID到高度的映射
		Map<Integer, Integer> nodeIdToHeight = new HashMap<>();

		// 第一步：解析所有结点信息，收集结点ID
		for (String line : cfgNodes) {
			if (line.startsWith("method") || line.trim().isEmpty()) continue;

			String[] parts = line.trim().split("\t");
			if (parts.length < 9) continue;

			try {
				int nodeId = Integer.parseInt(parts[1]);
				String content = parts[8];

				// 提取结点类型
				String nodeType = content.contains("@") ?
						content.substring(0, content.indexOf('@')) : content;

				// 提取高度
				int height = Integer.parseInt(parts[3]);

				// 存储结点信息
				allNodeIds.add(nodeId);
				nodeIdToType.put(nodeId, nodeType);
				nodeIdToHeight.put(nodeId, height);

				// 解析行号信息
				int startLine = Integer.parseInt(parts[5]);
				nodeIdToLineNumber.put(nodeId, startLine);
				lineNumberToNodeId.put(startLine, nodeId);

				// 特殊处理：记录方法起始和结束行
				if (nodeType.contains("first-statement")) {
					methodStartLine = startLine;
				} else if (nodeType.contains("pseudo-return")) {
					methodEndLine = startLine;
				}
			} catch (NumberFormatException | IndexOutOfBoundsException e) {
				e.printStackTrace();
				continue;
			}
		}

		// 第二步：初始化邻接表，确保所有结点ID存在
		Map<Integer, List<Integer>> adjacencyList = new HashMap<>();
		for (int nodeId : allNodeIds) {
			adjacencyList.put(nodeId, new ArrayList<>());
		}

		// 第三步：构建边关系（不修改Map结构，只修改List值）
		for (int nodeId : allNodeIds) {
			String nodeType = nodeIdToType.get(nodeId);
			int height = nodeIdToHeight.get(nodeId);

			// 处理for循环
			if (nodeType.startsWith("for")) {
				int conditionNodeId = nodeId + 1;
				int bodyNodeId = nodeId + 2;
				int updateNodeId = nodeId + 3;

				// 确保目标结点存在（已在第一步初始化）
				if (allNodeIds.contains(conditionNodeId)) {
					adjacencyList.get(nodeId).add(conditionNodeId);
				}

				if (allNodeIds.contains(bodyNodeId)) {
					adjacencyList.get(conditionNodeId).add(bodyNodeId);
				}

				// for-condition → 后续语句 (false分支)
				int nextNodeId = findNextNodeAtSameLevel(nodeId, allNodeIds, nodeIdToHeight);
				if (nextNodeId != -1) {
					adjacencyList.get(conditionNodeId).add(nextNodeId);
				}

				if (allNodeIds.contains(updateNodeId)) {
					adjacencyList.get(bodyNodeId).add(updateNodeId);
					adjacencyList.get(updateNodeId).add(conditionNodeId);
				}
			}
			// 处理普通语句（顺序执行）
			else {
				int nextNodeId = nodeId + 1;

				// 如果下一个结点存在且高度相同（同一层级）
				if (allNodeIds.contains(nextNodeId) &&
						nodeIdToHeight.get(nextNodeId) == height) {
					adjacencyList.get(nodeId).add(nextNodeId);
				}
				// 否则查找同层级的下一个结点
				else {
					nextNodeId = findNextNodeAtSameLevel(nodeId, allNodeIds, nodeIdToHeight);
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
										Set<Integer> allNodeIds,
										Map<Integer, Integer> nodeIdToHeight) {
		int currentHeight = nodeIdToHeight.get(nodeId);
		int nextNodeId = nodeId + 1;

		// 查找下一个同层级的结点
		while (true) {
			if (!allNodeIds.contains(nextNodeId)) {
				break;
			}

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
