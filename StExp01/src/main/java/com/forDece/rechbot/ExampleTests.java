package com.forDece.rechbot;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class ExampleTests {

	////////////
	/** TODO
	 *	Jifeng: Change the path of "pathFile" to your local path.
	 */
	String pathFile = "src/main/java/com/forDece/rechbot/Example.java";
	////////////
	
	String methodName1 = "reach1";
	String methodName2 = "reach2";
	String methodName3 = "reach3";
	String methodName4 = "reach4";
	
	/** TODO
	 * 	Jifeng: Write down the source code in your *OWN* MyDetector. 
	 */
	MyDetector myDetector = new MyDetector();
	
	/**
	 * Jifeng: You do not need to update the following-up code. 
	 * These test cases are used for helping check the implementation of your OWN MyDetector.
	 */
	@Test
	public void testReach1()
	{		
		String conditionA = "(43 > 59 || 25 < 124) && (25 < 24)";
		assertEquals(conditionA, myDetector.getCondition   (pathFile, methodName1, "a"));
		assertEquals(false,      myDetector.getReachability(pathFile, methodName1, "a"));
		
		String conditionB = "(43 > 59 || 25 < 124) && !(25 < 24)";
		assertEquals(conditionB, myDetector.getCondition   (pathFile, methodName1, "b"));
		assertEquals(true,       myDetector.getReachability(pathFile, methodName1, "b"));
	}
	
	@Test
	public void testReach2()
	{	
		// \"abc\" is the writing style of "abc" in a String object. 
		String conditionA = "(43 > 59 || 2 < \"abc\".length()) && (25 < 24)";
		assertEquals(conditionA, myDetector.getCondition   (pathFile, methodName2, "a"));
		assertEquals(false,      myDetector.getReachability(pathFile, methodName2, "a"));
		
		String conditionB = "(43 > 59 || 2 < \"abc\".length()) && !(25 < 24)";
		assertEquals(conditionB, myDetector.getCondition   (pathFile, methodName2, "b"));
		assertEquals(true,       myDetector.getReachability(pathFile, methodName2, "b"));
	}
	
	@Test
	public void testReach3()
	{
		String conditionA = "(45 < 35 * 2) && (21 < 3 && 41 > 5)";
		assertEquals(conditionA, myDetector.getCondition   (pathFile, methodName3, "a"));
		assertEquals(false,      myDetector.getReachability(pathFile, methodName3, "a"));
		
		String conditionB = "(45 < 35 * 2) && !(21 < 3 && 41 > 5) && (14 > 1 || 4 < 9 * 2)";
		assertEquals(conditionB, myDetector.getCondition   (pathFile, methodName3, "b"));
		assertEquals(true,       myDetector.getReachability(pathFile, methodName3, "b"));
	}
	
	@Test
	public void testReach4()
	{
		String conditionA = "(3 * 2 > 5) && (2 * 3 > 4) && (41 > 5 + 6 && 3 > 3 - 2)";
		assertEquals(conditionA, myDetector.getCondition   (pathFile, methodName4, "a"));
		assertEquals(true,       myDetector.getReachability(pathFile, methodName4, "a"));
		
		String conditionB = "(3 * 2 > 5) && !(2 * 3 > 4)";
		assertEquals(conditionB, myDetector.getCondition   (pathFile, methodName4, "b"));
		assertEquals(false,      myDetector.getReachability(pathFile, methodName4, "b"));
	}
}
