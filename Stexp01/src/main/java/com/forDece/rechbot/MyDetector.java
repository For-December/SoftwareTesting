package com.forDece.rechbot;

import reachbot.tool.BaseDetector;

public class MyDetector extends BaseDetector {

	@Override
	public String getCondition(String pathFile, String methodName, String id)
	{
		String methodCode = getMethodCode(pathFile, methodName);
		System.out.println(methodCode);
		return "Test";

//		return super.getCondition(pathFile, methodName, id);
	}

	@Override
	public boolean getReachability(String pathFile, String methodName, String id)
	{
		String methodCode = getMethodCode(pathFile, methodName);
		System.out.println(methodCode);
		return false;

//		return super.getReachability(pathFile, methodName, id);
	}

}
