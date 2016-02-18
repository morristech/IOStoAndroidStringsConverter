package com.skedgo.tripgo.tools.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
		
		// arg[0] android string path
		// arg[1] translations path
		// arg[2] android specific strings file name (android_localizable_strings.xml)
		// arg[3] Languages to generate separated by "#" (for example, en#es#de#fi#zh-Hant#zh-Hans)
		// arg[4] ios file name (TripKit.strings)
		// arg[5] ios file name (Shared.strings)
		// arg[6] ios file name	(Localizable.strings)	
		// ...
		// arg[n] ios file name
		//
		// i+1 strings has more priority than i+2 strings (discarded, not overriden) 
		
		if(args != null && args.length > 4 ){
			String androidStringPath = args[0] ;
			String translationsPath = args[1] ;
			String androidSpecificStringsFile = args[2] ;
			String[] langsArray = args[3].split("#");
			List<String> langs = new ArrayList<>(Arrays.asList(langsArray));
			
			List<String> iosStringsList = new ArrayList<>((args.length-4));
			
			for (int i = 4; i < args.length; i++) {	
				iosStringsList.add(args[i]);
			}
			
			StringsGeneratorUtils.getInstance().transformAllStrings(androidStringPath,translationsPath,
					androidSpecificStringsFile, iosStringsList, langs);			
			
		}else{
			throw new Error("Wrong parameters...");
		}
		
		
		
		
		System.out.println("Strings done! Time: " + (System.currentTimeMillis() - startTime) + "milisecs");
		
		
	}
	

}
