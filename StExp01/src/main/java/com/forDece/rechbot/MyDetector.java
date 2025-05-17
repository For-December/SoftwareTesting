package com.forDece.rechbot;

import reachbot.tool.BaseDetector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Stack;
import java.util.StringJoiner;

/**
 * 自己的代码路径条件检测器，重写了老师提供的 BaseDetector<br/>
 * 用于分析指定代码中的执行路径条件并判断目标输出语句的可达性。
 * <p>
 * 通过解析方法代码中的控制流语句（如if/else），构建路径条件表达式，并使用脚本引擎进行可达性判断。
 * </p>
 */
public class MyDetector extends BaseDetector {


	/**
	 * 获取指定方法中目标输出语句的路径条件表达式
	 *
	 * @param pathFile   源代码文件路径
	 * @param methodName 需要分析的方法名称
	 * @param id         目标输出语句的唯一标识符（println内容）
	 * @return 组合后的逻辑条件表达式字符串，格式为"condition1 && condition2..."，
	 *         如果找不到目标输出语句或没有条件则返回空字符串
	 */
	@Override
	public String getCondition(String pathFile, String methodName, String id) {
		// 获取方法并移除注释
		String methodCode = getMethodCode(pathFile, methodName);
		if (methodCode == null) {
			return "";
		}
		methodCode = removeComments(methodCode);

		// 定位输出语句
		String targetLine = "System.out.println(\"" + id + "\");";
		int targetIndex = methodCode.indexOf(targetLine);

		if (targetIndex == -1) {
			return "";
		}

		String[] lines = methodCode.split("\n");
		// 存储条件和代码块深度
		Stack<String> conditions = new Stack<>();
		Stack<Integer> blockDepths = new Stack<>();
		int currentDepth = 0;

		// 遍历代码，提取路径条件
        for (String s : lines) {
            String line = s.trim();
			// 当遇到目标行时，停止收集条件
			if (line.contains(targetLine)) {
				break;
			}

            // 追踪代码块深度
            if (line.contains("{")) {
                currentDepth++;
            } else if (line.contains("}")) {
                currentDepth--;
                // 当退出一个代码块时，检查是否需要弹出条件语句
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
        }

		// 构建最终条件表达式
		return String.join(" && ", conditions);
	}

	/**
	 * 判断目标输出语句是否可达
	 *
	 * @param pathFile   源代码文件路径
	 * @param methodName 需要分析的方法名称
	 * @param id         目标输出语句的唯一标识符
	 * @return true 表示该输出语句可达，false 表示不可达或发生错误
	 * @throws SecurityException 如果脚本引擎执行权限受限时可能抛出异常
	 */
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

	/**
	 * 从if语句中提取条件表达式
	 *
	 * @param line 包含if语句的代码行（需已去除注释和前后空格）
	 * @return 提取的条件表达式（不含外层括号），如果格式无效则返回空字符串
	 * @see #removeComments(String) 建议先使用该方法清理代码
	 */
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


	/**
	 * 清理代码中的注释内容
	 *
	 * @param code 原始代码字符串
	 * @return 去除所有单行注释（//）和多行注释（/* ... *\/）后的代码
	 */
	private String removeComments(String code) {
		// 移除单行注释
		code = code.replaceAll("//.*$", "");
		// 移除多行注释
		code = code.replaceAll("/\\*.*?\\*/", "");
		return code;
	}
	/**
	 * 预处理条件表达式使其符合JavaScript语法要求
	 *
	 * @param condition 原始条件表达式
	 * @return 处理后的表达式，包括：
	 *         - 替换中文括号为英文括号
	 *         - 规范逻辑运算符格式（添加空格分隔）
	 *         - 压缩多余空格
	 */
	private String preprocessCondition(String condition) {
		// 替换中文括号为英文括号
		condition = condition
				.replace('（', '(')
				.replace('）', ')');
		// 替换中文逻辑运算符
		condition = condition
				.replace("&&", " && ")
				.replace("||", " || ")
				.replace("!", "! ");
		// 处理缺失的空格
		condition = condition.replace("  ", " ");
		return condition;
	}

}
