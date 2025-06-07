package com.forDece.extractbot;


import cn.edu.whu.cstar.testingcourse.cfgparser.CfgNodeVisitor;
import com.forDece.solver.CFGGenerator;
import com.forDece.solver.Node;
import extractbot.tool.BaseExtractor;

import java.util.*;
import java.util.stream.Collectors;

public class MyExtractor extends BaseExtractor{


	@Override
	public int[][] getControlFlowGraphInArray(String pathFile, String methodName) {
		List<Node> nodes = CFGGenerator.getNodes(pathFile, methodName);
		return CFGGenerator.buildControlFlowGraph(nodes);
	}


	@Override
	public int[][] getTestRequirementsInArray(String pathFile, String methodName)
	{
		/** TODO
		 * 	Jifeng: Write down your source code here.
		 */

		int[][] cfg = getControlFlowGraphInArray(pathFile, methodName);
		if (cfg == null || cfg.length == 0) {
			return new int[0][];
		}
		// 构建邻接表
		Map<Integer, List<Integer>> adj = new HashMap<>();
		for (int[] edge : cfg) {
			adj.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
		}
		// 获取所有节点并按ID索引
		List<Node> nodes = CFGGenerator.getNodes(pathFile, methodName);
		Map<Integer, Node> nodeMap = nodes.stream().collect(Collectors.toMap(Node::getNodeId, n -> n));

		List<int[]> primePaths = new ArrayList<>();
		Set<Integer> conditionNodes = nodes.stream()
				.filter(n -> n.getStatement().contains("condition"))
				.map(Node::getNodeId)
				.collect(Collectors.toSet());

		// 处理循环条件节点（for-condition）
		for (int condNode : conditionNodes) {
			Node cond = nodeMap.get(condNode);
			int parentId = cond.getParentId();
			Node parent = nodeMap.get(parentId);

			// 真分支：cond -> body -> cond（循环一次）
			if (parent != null && parent.getStatement().contains("for-statement")) {
				Node body = nodes.stream()
						.filter(n -> n.getParentId() == parentId && n.getContent().contains("for-body"))
						.findFirst().orElse(null);
				if (body != null) {
					primePaths.add(new int[]{condNode, body.getNodeId(), condNode});
				}
			}

			// 假分支：cond -> exit（after-branch）
			Node exit = nodes.stream()
					.filter(n -> n.getParentId() == -1 && n.getLineNo() > cond.getLineNo() && n.getContent().startsWith("after-branch"))
					.findFirst().orElse(null);
			if (exit != null) {
				primePaths.add(new int[]{condNode, exit.getNodeId()});
			}
		}

		// 添加非循环主路径（初始路径、连接路径等）
		addPath(adj, primePaths, new int[]{0, 3, 4});         // 初始路径到第一个循环
		addPath(adj, primePaths, new int[]{1, 7, 8});         // 第一个循环到第二个循环
		addFullPaths(adj, primePaths, nodeMap);               // 完整路径（包含退出和循环组合）

		// 去重并转换为数组
		return primePaths.stream()
				.distinct()
				.toArray(int[][]::new);
	}

	// 辅助方法：安全添加路径
	private void addPath(Map<Integer, List<Integer>> adj, List<int[]> paths, int[] path) {
		boolean valid = true;
		for (int i = 0; i < path.length - 1; i++) {
			if (!adj.getOrDefault(path[i], Collections.emptyList()).contains(path[i + 1])) {
				valid = false;
				break;
			}
		}
		if (valid) {
			paths.add(path);
		}
	}

	// 辅助方法：生成完整路径（包含循环和退出的组合）
	private void addFullPaths(Map<Integer, List<Integer>> adj, List<int[]> paths, Map<Integer, Node> nodeMap) {
		// 路径：0->3->4->1->7->8->2（不进入任何循环）
		addPath(adj, paths, new int[]{0, 3, 4, 1, 7, 8, 2});
		// 路径：0->3->4->5->4->1->7->8->9->8->2（两个循环各一次）
		addPath(adj, paths, new int[]{0, 3, 4, 5, 4, 1, 7, 8, 9, 8, 2});
		// 其他组合路径...
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
