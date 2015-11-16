package com.skedgo.tools;

import java.util.ArrayList;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		
		String androidStringPath ;	
		
		// arg[0] android string path
		// ...
		// n = 1, 3, ...
		// ...
		// arg[n] ios path containing lang dirs
		// arg[n+1] ios file name
		//
		// n+1 strings has more priority than n+3 strings (discarded, not overriden) 
		
		List<List<String>> iosStringsInfo;
		
		
		if(args != null && args.length > 0 && args.length%2 != 0){
			androidStringPath = args[0] ;
			
			iosStringsInfo = new ArrayList<>((args.length-1)/2);
			
			for (int i = 1; i < args.length; i+=2) {
				List<String> iosDirFile = new ArrayList<String>(2);
				System.out.println(args[i]);
				System.out.println(args[i+1]);
				iosDirFile.add(0, args[i]);
				iosDirFile.add(1, args[i+1]);
				iosStringsInfo.add(iosDirFile);
			}
			
			IOStoAndroidStringsUtils.getInstance().transformAllStrings(androidStringPath,iosStringsInfo);
			
		}else{
			throw new Error("Wrong parameters...");
		}
		
		
		
		
		
	}
	

}
