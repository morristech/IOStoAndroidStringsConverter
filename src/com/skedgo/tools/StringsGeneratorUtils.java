package com.skedgo.tools;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StringsGeneratorUtils {

	private static StringsGeneratorUtils instance;



	public static final String DEFAULT_LANG = "en";

	private StringsGeneratorUtils() {
	}

	public static StringsGeneratorUtils getInstance() {
		if (instance == null) {
			instance = new StringsGeneratorUtils();
		}
		return instance;
	}

	public void transformAllStrings(String androidStringPath, String translationsPath,
			String androidSpecificStringsFile, List<String> iosStringsList, List<String>langs) {

		for (int i = 0; i < iosStringsList.size(); i++) {
			transformAllStrings(iosStringsList.get(i) + ".xml", androidStringPath, translationsPath,
					iosStringsList.get(i), langs);
		}
		
        transformAllSpecificStrings(androidStringPath, translationsPath, androidSpecificStringsFile, langs);

	}

	public void transformAllStrings(String androidFileName, String destAndroidStringPath, String translationsPath,
			String iOSStringFileName, List<String>langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {
			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();
				
				if(skipLang(lang, langs)){
					continue;
				}
				
				String androidLangDir = lang.replace("-", "-r").replace("Hans", "CN").replace("Hant", "TW"); // iOS
																												// like
																											// dir

				if (androidLangDir.equals("") || lang.equals(DEFAULT_LANG)) { // default res
					androidLangDir = "values";
				} else {
					androidLangDir = "values-" + androidLangDir;
				}
				
				IOStoAndroidUtils.getInstance().transformIOStoAndroidStrings(translationsPath + "/" + lang + "/" + iOSStringFileName,
						destAndroidStringPath + "/" + androidLangDir, androidFileName, androidLangDir);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void transformAllSpecificStrings(String destAndroidStringPath, String translationsPath,
			String androidSpecificStringsFile, List<String>langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {
			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();
				
				if(skipLang(lang, langs)){
					continue;
				}
				
				String androidLangDir = lang.replace("-", "-r").replace("Hans", "CN").replace("Hant", "TW"); // iOS
				
		
				if (androidLangDir.equals("") || lang.equals(StringsGeneratorUtils.DEFAULT_LANG)) { // default
																				// res
					androidLangDir = "values";
				} else {
					androidLangDir = "values-" + androidLangDir;
				}
				
				IOStoAndroidUtils.getInstance().reGenerateDefaultAndroidStrings(translationsPath + "/" + lang + "/" + androidSpecificStringsFile,
						destAndroidStringPath + "/" + androidLangDir, androidSpecificStringsFile, androidLangDir);

			}
		} catch (IOException ex) {
		}

	}
	
	private boolean skipLang(String langToCheck, List<String>langs) {
		
		for (String lang:langs) {
			if(langToCheck.contains(lang)){
				return false;
			}			
		}
		return true;
	}

	public static class DirectoriesFilter implements Filter<Path> {
		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry);
		}
	}

}
