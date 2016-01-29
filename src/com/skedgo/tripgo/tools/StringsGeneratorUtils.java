package com.skedgo.tripgo.tools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skedgo.tools.model.StringDefinition;
import com.skedgo.tools.model.StringsStructure;
import com.skedgo.tools.platform.android.AndroidOutputStrategy;
import com.skedgo.tools.platform.ios.IOSInputStrategy;

public class StringsGeneratorUtils {

	private static StringsGeneratorUtils instance;

	public static final String DEFAULT_LANG = "en";

	private Map<String, List<String>> namesMap = new HashMap<String, List<String>>();

	private StringsGeneratorUtils() {
	}

	public static StringsGeneratorUtils getInstance() {
		if (instance == null) {
			instance = new StringsGeneratorUtils();
		}
		return instance;
	}

	public void transformAllStrings(String androidStringPath, String translationsPath,
			String androidSpecificStringsFile, List<String> iosStringsList, List<String> langs) {

		for (int i = 0; i < iosStringsList.size(); i++) {
			transformAllStrings(iosStringsList.get(i) + ".xml", androidStringPath, translationsPath,
					iosStringsList.get(i), langs);
		}

		// TODO
		// transformAllSpecificStrings(androidStringPath, translationsPath,
		// androidSpecificStringsFile, langs);

	}

	public void transformAllStrings(String androidFileName, String destAndroidStringPath, String translationsPath,
			String iOSStringFileName, List<String> langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {

			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();

				if (skipLang(lang, langs)) {
					continue;
				}

				String androidLangDir = lang.replace("-", "-r").replace("Hans", "CN").replace("Hant", "TW"); // iOS
																												// like
																												// dir

				if (androidLangDir.equals("") || lang.equals(DEFAULT_LANG)) { // default
																				// res
					androidLangDir = "values";
				} else {
					androidLangDir = "values-" + androidLangDir;
				}

				if (namesMap.get(lang) == null) {
					namesMap.put(lang, new ArrayList<String>());
				}

				List<String> stringNames = namesMap.get(lang);

				String input = readFile(translationsPath + "/" + lang + "/" + iOSStringFileName);

				IOSInputStrategy inputStrategy = new IOSInputStrategy();
				AndroidOutputStrategy outputStrategy = new AndroidOutputStrategy();

				StringsStructure structure = inputStrategy.getInputValues(input);
				structure = outputStrategy.preprocessInputNames(structure);

				// remove duplicates
				Map<Integer, StringDefinition> definitionsCopy = new HashMap<>(structure.getDefinitions());

				for (int i = 0; i < structure.getDefinitions().size(); i++) {
					StringDefinition definition = structure.getDefinitions().get(i);

					if (stringNames.contains(definition.getName())) {
						definitionsCopy.remove(i);
					} else {
						stringNames.add(definition.getName());
					}
				}

				structure.setDefinitions(definitionsCopy);

				String output = outputStrategy.generateOutput(structure);

				writeFile(destAndroidStringPath + "/" + androidLangDir + "/" + androidFileName, output);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void transformAllSpecificStrings(String destAndroidStringPath, String translationsPath,
			String androidSpecificStringsFile, List<String> langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {
			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();

				if (skipLang(lang, langs)) {
					continue;
				}

				String androidLangDir = lang.replace("-", "-r").replace("Hans", "CN").replace("Hant", "TW"); // iOS

				if (androidLangDir.equals("") || lang.equals(StringsGeneratorUtils.DEFAULT_LANG)) { // default
					// res
					androidLangDir = "values";
				} else {
					androidLangDir = "values-" + androidLangDir;
				}

				IOStoAndroidUtils.getInstance().reGenerateDefaultAndroidStrings(
						translationsPath + "/" + lang + "/" + androidSpecificStringsFile,
						destAndroidStringPath + "/" + androidLangDir, androidSpecificStringsFile, androidLangDir);

			}
		} catch (IOException ex) {
		}

	}

	private boolean skipLang(String langToCheck, List<String> langs) {

		for (String lang : langs) {
			if (langToCheck.contains(lang)) {
				return false;
			}
		}
		return true;
	}

	private String readFile(String path) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, StandardCharsets.UTF_8);
	}

	private void writeFile(String path, String content) throws IOException {

		Files.write((Paths.get(path)), content.getBytes(StandardCharsets.UTF_8));

	}

	public static class DirectoriesFilter implements Filter<Path> {
		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry);
		}
	}

}
