package com.skedgo.tools;

public class Main {

	public static void main(String[] args) {
		
		String iOSStringPath = args[0];	
		String androidStringPath = args[1] ;	
		
		System.out.println("GENERATE STRINGS " + iOSStringPath + " " + androidStringPath);
		
		IOStoAndroidStringsUtils.getInstance().transformAllStrings(iOSStringPath, androidStringPath);
		
	}
	

}
