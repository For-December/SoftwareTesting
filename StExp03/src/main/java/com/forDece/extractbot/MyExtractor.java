package com.forDece.extractbot;


import cn.edu.whu.cstar.testingcourse.cfgparser.ControlFlowNodeParser;
import com.forDece.solver.CFGGenerator;
import com.forDece.solver.Node;
import extractbot.tool.BaseExtractor;

import java.io.*;
import java.util.*;

public class MyExtractor extends BaseExtractor{


	@Override
	public int[][] getControlFlowGraphInArray(String pathFile, String methodName) {
		List<Node> nodes = CFGGenerator.getNodes(pathFile, methodName);
		return CFGGenerator.buildControlFlowGraph(nodes);
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
