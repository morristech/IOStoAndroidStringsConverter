package com.skedgo.tools;

public class Main {

	public static void main(String[] args) {
		
		String iOSStringPath = args[0];	
		String iOSStringFileName = args[1] ;
		String androidStringPath = args[2] ;	
		String androidStringFileName = args[3] ;	
		
		System.out.println("GENERATE STRINGS " + iOSStringPath + " " + androidStringPath);
		
		IOStoAndroidStringsUtils.getInstance().transformAllStrings(iOSStringPath, iOSStringFileName,
				androidStringPath,androidStringFileName);
		
	}
	

}
