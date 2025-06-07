package com.forDece.extractbot;


import cn.edu.whu.cstar.testingcourse.cfgparser.CfgNodeVisitor;
import com.forDece.solver.CFGGenerator;
import com.forDece.solver.Node;
import com.forDece.solver.PrimePathGenerator;
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
		int [][] cfg = getControlFlowGraphInArray(pathFile, methodName);
		return PrimePathGenerator.buildTestRequirements(cfg);
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
