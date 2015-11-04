package com.skedgo.tools;

public class Main {

	public static void main(String[] args) {
		
		String iOSStringPath = args[0] + "/Localizable.strings";	
		String androidStringPath = args[0] + "/converted_strings.xml";	
		
		IOStoAndroidStringsUtils.getInstance().transformIOStoAndroidStrings(iOSStringPath, androidStringPath);
		
	}
	

}
