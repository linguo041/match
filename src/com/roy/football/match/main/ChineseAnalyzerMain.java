package com.roy.football.match.main;

import java.io.IOException;
import java.io.StringReader;

import org.wltea.analyzer.IKSegmentation;
import org.wltea.analyzer.Lexeme;

public class ChineseAnalyzerMain {
	
	public static void main (String [] args) throws IOException {
		String text="墨尔本城近10个主场8胜2平保持不败，而其近22个主场仅有2负，成绩为15胜5平2负。";
	    StringReader sr=new StringReader(text);
	    IKSegmentation ik=new IKSegmentation(sr, true);
	    Lexeme lex=null;
	    while((lex=ik.next())!=null){  
	        System.out.print(lex.getLexemeText()+"|");  
	    } 
	}
	
}
