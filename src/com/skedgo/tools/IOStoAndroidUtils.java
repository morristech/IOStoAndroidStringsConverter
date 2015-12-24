package com.skedgo.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IOStoAndroidUtils {
	
	private Dictionary<String, List<String>> namesDic;
	
	private static IOStoAndroidUtils instance;
	
	private String patternWords = "[^\"\r\n]+";
	private String patternStringIOS = "\"(" + patternWords + ")\" = \"(" + patternWords + ")\";";
	private String patternComments = "/\\*(.*?)\\*/";
	private String patternIOSCommplete = "[" + patternStringIOS +"|"+ patternComments + "]+";
	
	private IOStoAndroidUtils(){}
	
	public static IOStoAndroidUtils getInstance() {
		if(instance==null){
			instance = new IOStoAndroidUtils();
			instance.namesDic = new Hashtable<>();
		}
		return instance;
	}
	
	public void transformIOStoAndroidStrings(String iOSStringPath, String androidStringPath, String androidFileName,
			String mapKey) {
		
		if (namesDic.get(mapKey) == null) {
			namesDic.put(mapKey, new ArrayList<String>());
		}

		try {
			
			String content = readFile(iOSStringPath);
			
			
			Pattern patternStringNames = Pattern.compile(patternIOSCommplete);
			Matcher matcher = patternStringNames.matcher(content);
			
			String match = null;
			String stringDef = null;
			
			StringBuffer buf = new StringBuffer("<?xml version='1.0' encoding='UTF-8'?>\n<resources>\n");
			
			while (matcher.find()) {
				
				match =  matcher.group();
				
				buf.append("\n");
				
				if(match.startsWith("\"")){
					stringDef = processStringDef(match, mapKey);
					if (stringDef!=null)buf.append(stringDef);					
				}else{
					buf.append(processComment(match));
				}				
				
			}
			
			buf.append("\n</resources>");
			
			if (!Files.exists(Paths.get(androidStringPath))) {
				Files.createDirectory(Paths.get(androidStringPath));
			}

			writeFile(androidStringPath + "/" + androidFileName,  buf.toString());

				

		} catch (IOException e) {
			System.out.println("ERROR " + e.getMessage());
		}
		
		
	}
	
	protected String processComment(String string){
		
		return string.replace("/*", "\t<!--").replace("*/", "-->");
		
	}
	
	protected String processStringDef(String string, String mapKey){
		
		Pattern patternStringNames = Pattern.compile(patternStringIOS);
		Matcher matcher = patternStringNames.matcher(string);
		
		String name = null;
		
		if(matcher.find()){	
			
			name = cleanName(matcher.group(1), mapKey);
			
			if(name == null){
				return null;
			}else {
				return "\t<string name=\""+ name +"\">"+ cleanValue(matcher.group(2)) +"</string>";
			}
		};
		
		return "\t<string name=\"string_error\"> string error!!! " + string +" </string>";
		
	}
	
	protected String basicClean(String string){
		return string.replaceAll("&", "&amp;").replaceAll("%1\\$@", "PERCAT").replaceAll("%2\\$@", "PERCAT")
				.replaceAll("%@", "PERCAT").replaceAll("%ld", "PERCAT")
				;
	}
	
	protected String cleanName(String name, String mapKey){
		
		name = basicClean(name);
		
		name = name.replace(" ", "_").replace(".", "_DOT").replace("!", "_EXCLAM").replace("%s", "nps")
				.replace("?", "_QUESTION").replace("\'", "_APOST").replace("/", "_SLASH").replace(",", "_COMA")
				.replace("(", "_START_PARENT").replace(")", "_END_PARENT").replace("{", "_START_QBRAQUET")
				.replace("}", "_END_QBRAQUET").replace("&amp;", "_AMPERSAND").replace("-", "_MINUS")
				.replace("<", "_LESST").replace(">", "_MORET").replace("@", "_AT").replace("=", "_EQUAL")
				.replace("%", "_PERC").replace("₂", "_2").replace("PERCAT", "_pattern");
		
		if (Character.isDigit(name.charAt(0))) { 
			name = "_" + name;
		}
		
		name = name.toLowerCase();
		
		List<String> names = namesDic.get(mapKey);
		if (names.contains(name)) {
			// duplicate!
			return null;
		}

		names.add(name);
		
		return name;
		
	}
	
	protected String cleanValue(String value){
		String cleanedValue = createAndroidPatterns(basicClean(value).replace("'", "\\'"));		
		
		if(cleanedValue.startsWith(" ") || cleanedValue.endsWith(" ")){
			cleanedValue = "\"" + cleanedValue + "\"";
		}
		
		return cleanedValue;
		
	}
	
	protected String createAndroidPatterns(String string){
		int i = 1;
		while(string.contains("PERCAT")){
			string = string.replaceFirst("PERCAT", "%"+ i++ +"\\$s");
		}
		return string;
	}
	

	// TODO: remove this method
	public void reGenerateDefaultAndroidStrings(String androidSpecificStringPath, String androidStringPath,
			String androidFileName, String mapKey) {
		
		 try {

			if (!Files.exists(Paths.get(androidSpecificStringPath))) {
				return;
			}

			String content = readFile(androidSpecificStringPath);

			String patternWords = "[^\"\r\n]+";
			String patternFullStringDef = "(name=\"" + patternWords + "\")";

			Pattern patternStringNames = Pattern.compile(patternFullStringDef);
			Matcher matcher = patternStringNames.matcher(content);

			// Names clean up
			StringBuffer buf = new StringBuffer();
			
			String keyName = null;
			
			while (matcher.find()) {
				String stringName = matcher.group(1);

				List<String> names = namesDic.get(mapKey);

				// transform to low cap (should already be)
				stringName = stringName.toLowerCase();
				
				keyName = stringName.replace("name=\"", "").replace("\"", "");

				if (names.contains(keyName)) {
					// duplicate! Name as default
					stringName = stringName.replace("name=\"", "name=\"default_");
				}

				names.add(keyName);				

				matcher.appendReplacement(buf, stringName);

			}
			matcher.appendTail(buf);

			if (!Files.exists(Paths.get(androidStringPath))) {
				Files.createDirectory(Paths.get(androidStringPath));
			}

			writeFile(androidStringPath + "/" + androidFileName, buf.toString());

		} catch (IOException e) {
			System.out.println("ERROR " + e.getMessage());
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
