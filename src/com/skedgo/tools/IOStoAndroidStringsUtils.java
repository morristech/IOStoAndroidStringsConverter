package com.skedgo.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOStoAndroidStringsUtils {

	private static IOStoAndroidStringsUtils instance;
	
	private Dictionary<String, List<String>> namesDic;
	
	public static final String DEFAULT_LANG = "en";
	
	private IOStoAndroidStringsUtils(){}
	
	public static IOStoAndroidStringsUtils getInstance(){
		if (instance == null) {
			instance = new IOStoAndroidStringsUtils();
		}
		return instance;
	}
	
	public void transformAllStrings(String androidStringPath, String translationsPath,
			String androidSpecificStringsFile,  List<String> iosStringsList) {

	   	namesDic = new Hashtable<>();
	   	
		for (int i = 0; i < iosStringsList.size(); i++) {
			transformAllStrings(iosStringsList.get(i)+ ".xml" ,androidStringPath,
					translationsPath, iosStringsList.get(i));
		}
		
		transformAllSpecificStrings(androidStringPath,
				translationsPath,androidSpecificStringsFile);
		
		
	}
	
	
	public void transformAllStrings(String androidFileName, String destAndroidStringPath, String translationsPath,
			String iOSStringFileName){		
		
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(translationsPath))) {
            for (Path path : directoryStream) {   
            	
            	String dir = path.toString();
            	String lang =  dir.substring(dir.lastIndexOf("\\") + 1, dir.length());            	
            	
            	if(!Files.exists(Paths.get(translationsPath + "/" + lang  + "/" +  iOSStringFileName))){
            		// not a language dir
            		continue
            		;
            	}
            	String androidLangDir = lang.replace("-", "-r")
            								.replace("Hans", "CN")
            								.replace("Hant", "TW"); // iOS like dir
            	
            	if(androidLangDir.equals("") || lang.equals(DEFAULT_LANG)){ // default res
            		androidLangDir = "values";
            	}else{
            		androidLangDir = "values-" + androidLangDir;
            	}
            	
            	if(namesDic.get(androidLangDir)==null){
            		namesDic.put(androidLangDir, new ArrayList<String>());
            	}
        
            	transformIOStoAndroidStrings(translationsPath + "/" + lang  + "/" +  iOSStringFileName,
            			destAndroidStringPath + "/" + androidLangDir , androidFileName, androidLangDir);            	
                     	
            }
        } catch (IOException ex) {}
		
	}
	
	public void transformAllSpecificStrings(String destAndroidStringPath, String translationsPath,
			String androidSpecificStringsFile){		
		
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(translationsPath))) {
            for (Path path : directoryStream) {   
            	
            	String dir = path.toString();
            	String lang =  dir.substring(dir.lastIndexOf("\\") + 1, dir.length());            	

            	
            	if(!Files.exists(Paths.get(translationsPath + "/" + lang  + "/" +  androidSpecificStringsFile))){
            		// not a language dir
            		continue;
            	}            	

            	
            	String androidLangDir = lang.replace("-", "-r")
            								.replace("Hans", "CN")
            								.replace("Hant", "TW"); // iOS like dir
            	
            	if(androidLangDir.equals("")|| lang.equals(DEFAULT_LANG)){ // default res
            		androidLangDir = "values";
            	}else{
            		androidLangDir = "values-" + androidLangDir;
            	}
            	
            	if(namesDic.get(androidLangDir)==null){
            		namesDic.put(androidLangDir, new ArrayList<String>());
            	}
        
            	reGenerateDefaultAndroidStrings(translationsPath + "/" + lang  + "/" + androidSpecificStringsFile,
            			destAndroidStringPath + "/" + androidLangDir , androidSpecificStringsFile, androidLangDir);
            	
            }
        } catch (IOException ex) {}
		
	}
	
	public void transformIOStoAndroidStrings(String iOSStringPath,String androidStringPath,
			String androidFileName, String mapKey){
		
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
				String stringFullName = matcher.group(1);
				
				// get only the name for cleaning 
				String stringName = stringFullName.substring(("name=\"".length()), stringFullName.length());
				
				stringName = stringName.replace(" ", "_")
						.replace(".", "_DOT")
						.replace("!", "_EXCLAM")
						.replace("?", "_QUESTION")
						.replace("\'", "_APOST")
						.replace("/", "_SLASH")
						.replace(",", "_COMA")
						.replace("(", "_START_PARENT")
						.replace(")", "_END_PARENT")
						.replace("{", "_START_QBRAQUET")
						.replace("}", "_END_QBRAQUET")
						.replace("&amp;", "_AMPERSAND")
						.replace("-", "_MINUS")
						.replace("<", "_LESST")
						.replace(">", "_MORET")
						.replace("@", "_AT")
						.replace("=", "_EQUAL")
						.replace("%", "_PERC")
						.replace("₂", "_2");
				
				// reconstruct name
				stringName = "name=\"" + stringName;
				
				if(Character.isDigit(stringName.charAt("name=\"".length()))){ // because of the matching,
  																		      // all strings start with name="
					stringName = stringName.replace("name=\"", "name=\"_");
				}
				
				List<String>names = namesDic.get(mapKey);
				
				// transform to low cap
				stringName = stringName.toLowerCase();

				if(names.contains(stringName)){
					// duplicate!
					stringName = stringName.replace("name=\"", "name=\"duplicate_");
				}	
				
				names.add(stringName);
				
				matcher.appendReplacement(buf, stringName);

			}
			matcher.appendTail(buf);
			
			buf.append("\n</resources>");
			
			if(!Files.exists(Paths.get(androidStringPath))){
				Files.createDirectory(Paths.get(androidStringPath));
			}			
			
			writeFile(androidStringPath + "/" +  androidFileName,buf.toString());
			
		} catch (IOException e) {
			System.out.println("ERROR "+  e + " ## " + e.getMessage());
		}
		
				
	}
	
	public void reGenerateDefaultAndroidStrings(String androidSpecificStringPath,String androidStringPath,
			String androidFileName, String mapKey){
		
		try {
			
			if(!Files.exists(Paths.get(androidSpecificStringPath))){
				return;
			}
			
			String content = readFile(androidSpecificStringPath);
			
			String patternWords = "[^\"\r\n]+";
			String patternFullStringDef = "(name=\""+ patternWords + "\")";			

			Pattern patternStringNames = Pattern.compile(patternFullStringDef);
			Matcher matcher = patternStringNames.matcher(content);
			
			
			// Names clean up
			StringBuffer buf = new StringBuffer();
			while(matcher.find()){
				String stringName = matcher.group(1);				
				
				
				List<String>names = namesDic.get(mapKey);
				
			
				// transform to low cap (should already be)
				stringName = stringName.toLowerCase();	
				
				
				if(names.contains(stringName)){
					// duplicate! Name as default
					stringName = stringName.replace("name=\"", "name=\"default_");
				}	
				
				names.add(stringName);
				
				if (stringName.contains("sort") || stringName.contains("about")) {
					System.out.println("AND added key " + stringName);
				}
				
				matcher.appendReplacement(buf, stringName);

			}
			matcher.appendTail(buf);
			
			
			if(!Files.exists(Paths.get(androidStringPath))){
				Files.createDirectory(Paths.get(androidStringPath));
			}			
			
			writeFile(androidStringPath + "/" +  androidFileName,buf.toString());

			
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
