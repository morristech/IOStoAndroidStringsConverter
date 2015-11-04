package com.skedgo.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOStoAndroidStringsUtils {

	private static IOStoAndroidStringsUtils instance;
	
	private IOStoAndroidStringsUtils(){}
	
	public static IOStoAndroidStringsUtils getInstance(){
		if (instance == null) {
			instance = new IOStoAndroidStringsUtils();
		}
		return instance;
	}
	
	public void transformIOStoAndroidStrings(String iOSStringPath,String androidStringPath){
		
		try {
			String content = readFile(iOSStringPath);
			
			String patternWords = "[^\"\r\n]+";
			String patternIOS = "\"("+ patternWords + ")\" = \"("+ patternWords + ")\";" ;
			String patternComments = "/\\*(.*?)\\*/" ;
			String patternFullStringDef = "(name=\""+ patternWords + "\")";
			
			// Basic clean
			content = content.replaceAll("&", "&amp;")
							 .replaceAll("%@", "PERCAT")
							 .replaceAll("%ld", "PERCLD")
							 .replace("'", "\\'");
			
			// Strings			
			String convertedAndroid = content.replaceAll(patternIOS, "\t<string name=\"$1\">$2</string>"); 
			
			// Comments			
			String convertedAndroid_noComments = convertedAndroid.replaceAll(patternComments,"\t<!--$1-->");
		
			Pattern patternStringNames = Pattern.compile(patternFullStringDef);
			Matcher matcher = patternStringNames.matcher(convertedAndroid_noComments);
			
			// Names clean up
			StringBuffer buf = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>\n<resources>\n");
			while(matcher.find()){
				matcher.appendReplacement(buf,  matcher.group(1)
													.replace(" ", "_")
													.replace(".", "DOT")
													.replace("!", "EXCLAM")
													.replace("?", "QUESTION")
													.replace("\'", "APOST")
													.replace(",", "COMA")
													.replace("&amp;", "AMPERSAND")
													.replace("-", "SLASH"));

			}
			matcher.appendTail(buf);
			
			buf.append("\n</resources>");
			
			writeFile(androidStringPath,buf.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}
	
	private void writeFile(String path, String content) throws IOException {
		
		Files.write((Paths.get(path)), content.getBytes(StandardCharsets.UTF_8));

	}
	
}
