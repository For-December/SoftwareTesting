package com.forDece;


import cn.edu.whu.cstar.testingcourse.cfgparser.ControlFlowNodeParser;

public class Main {
    public static void main(String[] args) {
        new ControlFlowNodeParser().parseControlFlowNodes("src/main/java/com/forDece/extractbot/Example.java","example1");
    }
}