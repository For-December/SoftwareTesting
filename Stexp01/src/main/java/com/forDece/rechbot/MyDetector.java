package com.forDece.rechbot;

import reachbot.tool.BaseDetector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Stack;
public class MyDetector extends BaseDetector {


	@Override
	public String getCondition(String pathFile, String methodName, String id) {
		String methodCode = getMethodCode(pathFile, methodName);
		if (methodCode == null) {
			return "";
		}

		methodCode = removeComments(methodCode);

		String targetLine = "System.out.println(\"" + id + "\");";
		int targetIndex = methodCode.indexOf(targetLine);

		if (targetIndex == -1) {
			return "";
		}

		StringBuilder condition = new StringBuilder();
		String[] lines = methodCode.split("\n");
		Stack<String> conditions = new Stack<>();
		Stack<Integer> blockDepths = new Stack<>();
		int currentDepth = 0;

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].trim();

			// 追踪代码块深度
			if (line.contains("{")) {
				currentDepth++;
			} else if (line.contains("}")) {
				currentDepth--;
				// 当退出一个代码块时，检查是否需要弹出条件
				while (!blockDepths.isEmpty() && blockDepths.peek() > currentDepth) {
					blockDepths.pop();
					if (!conditions.isEmpty()) {
						conditions.pop();
					}
				}
			}

			if (line.startsWith("if")) {
				String ifCondition = extractCondition(line);
				if (!ifCondition.isEmpty()) {
					conditions.push("(" + ifCondition + ")");
					blockDepths.push(currentDepth);
				}
			} else if (line.startsWith("else")) {
				if (!conditions.isEmpty() && !blockDepths.isEmpty() && blockDepths.peek() == currentDepth) {
					String lastCondition = conditions.pop();
					// 移除最外层括号再取反
					if (lastCondition.startsWith("(") && lastCondition.endsWith(")")) {
						lastCondition = lastCondition.substring(1, lastCondition.length() - 1);
					}
					conditions.push("!(" + lastCondition + ")");
				}
			}

			// 当遇到目标行时，停止收集条件
			if (line.contains(targetLine)) {
				break;
			}
		}

		// 构建最终条件表达式
		boolean first = true;
		for (String cond : conditions) {
			if (!first) {
				condition.append(" && ");
			}
			condition.append(cond);
			first = false;
		}

		return condition.toString();
	}
	@Override
	public boolean getReachability(String pathFile, String methodName, String id) {
		String condition = getCondition(pathFile, methodName, id);
		if (condition.isEmpty()) {
			return false;
		}

		// 预处理条件表达式，添加缺失的运算符
		condition = preprocessCondition(condition);

		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return Boolean.parseBoolean(engine.eval(condition).toString());
		} catch (ScriptException e) {
			System.err.println("Error evaluating condition: " + condition);
			e.printStackTrace();
			return false;
		}
	}

	private String extractCondition(String line) {
		// 处理单行if语句
		int startIdx = line.indexOf('(');
		if (startIdx == -1) {
			return "";
		}

		// 计算括号深度，找到匹配的右括号
		int depth = 1;
		int endIdx = -1;

		// 从起始括号之后开始查找
		for (int i = startIdx + 1; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				depth--;
				if (depth == 0) {
					endIdx = i;
					break;
				}
			}
		}

		if (endIdx != -1) {
			return line.substring(startIdx + 1, endIdx).trim();
		}

		// 如果在当前行未找到匹配的右括号，可能是多行条件
		return "";
	}

	private String removeComments(String code) {
		// 移除单行注释
		code = code.replaceAll("//.*$", "");
		// 移除多行注释
		code = code.replaceAll("/\\*.*?\\*/", "");
		return code;
	}

	private String preprocessCondition(String condition) {
		// 替换中文括号为英文括号
		condition = condition.replace('（', '(').replace('）', ')');
		// 替换中文逻辑运算符
		condition = condition.replace("&&", " && ").replace("||", " || ").replace("!", "! ");
		// 处理缺失的空格
		condition = condition.replace("  ", " ");
		return condition;
	}

}
