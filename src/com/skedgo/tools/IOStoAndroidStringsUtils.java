package com.skedgo.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
	
	
	public void transformAllStrings(String iOSStringPath,String iOSStringFileName,
			String destAndroidStringPath, String androidFileName){
		
		
		System.out.println("EXIST "  + Files.exists(Paths.get(iOSStringPath)));
		
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(iOSStringPath))) {
            for (Path path : directoryStream) {
            	
            	String dir = path.toString();
            	String lang =  dir.substring(dir.lastIndexOf("\\") + 1, dir.length());            	
            	
            	
            	System.out.println("LANG DIR "  +iOSStringPath + "/" + lang  + "/" +  iOSStringFileName);
            	
            	if(!Files.exists(Paths.get(iOSStringPath + "/" + lang  + "/" +  iOSStringFileName))){
            		// not a language dir
            		continue;
            	}
            	
            	System.out.println("LANG "  +lang);
            	
            	String androidLangDir = lang.replace("-", "-r")
            								.replace("Hans", "CN")
            								.replace("Hant", "TW")
            								.replace("Base", "")
            								.replace(".lproj", ""); // iOS like dir
            	
            	if(androidLangDir.equals("")){ // default res
            		androidLangDir = "values";
            	}else{
            		androidLangDir = "values-" + androidLangDir;
            	}
        
            	transformIOStoAndroidStrings(iOSStringPath + "/" + lang  + "/" +  iOSStringFileName,
            			destAndroidStringPath + "/" + androidLangDir , androidFileName);
            	
            }
        } catch (IOException ex) {}
		
	}
	
	public void transformIOStoAndroidStrings(String iOSStringPath,String androidStringPath,
			String androidFileName){
		
		try {
			String content = readFile(iOSStringPath);
			
			String patternWords = "[^\"\r\n]+";
			String patternIOS = "\"("+ patternWords + ")\" = \"("+ patternWords + ")\";" ;
			String patternComments = "/\\*(.*?)\\*/" ;
			String patternFullStringDef = "(name=\""+ patternWords + "\")";
			
			
			// Basic clean	
			
			content = content.replaceAll("&", "&amp;")
					 .replaceAll("%1", "PERC1")
					 .replaceAll("%2", "PERC2")
					 .replaceAll("\\$@", "DOLLARAT")
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
				String stringName = matcher.group(1)
						.replace(" ", "_")
						.replace(".", "DOT")
						.replace("!", "EXCLAM")
						.replace("?", "QUESTION")
						.replace("\'", "APOST")
						.replace("/", "SLASH")
						.replace(",", "COMA")
						.replace("(", "START_PARENT")
						.replace(")", "END_PARENT")
						.replace("&amp;", "AMPERSAND")
						.replace("-", "MINUS");
				
				System.out.println(stringName);
				
				if(Character.isDigit(stringName.charAt("name=\"".length()))){ // because of the matching,
  																		      // all strings start with name="
					stringName = stringName.replace("name=\"", "name=\"_");
				}
				
				matcher.appendReplacement(buf, stringName);

			}
			matcher.appendTail(buf);
			
			buf.append("\n</resources>");
			
			if(!Files.exists(Paths.get(androidStringPath))){
				Files.createDirectory(Paths.get(androidStringPath));
			}			
			
			writeFile(androidStringPath + "/" +  androidFileName,buf.toString());
			
			System.out.println("Done!!" );
			
		} catch (IOException e) {
			System.out.println("ERROR "+  e + " ## " + e.getMessage());
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
