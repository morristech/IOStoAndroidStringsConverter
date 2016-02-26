package com.skedgo.tripgo.tools.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.skedgo.tools.InputCreatorListener;
import com.skedgo.tools.model.StringDefinition;
import com.skedgo.tools.model.StringsStructure;
import com.skedgo.tools.platform.android.AndroidInputStrategy;
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

		transformAllSpecificStrings(androidStringPath, translationsPath, androidSpecificStringsFile, langs);

	}

	public void transformAllStrings(final String androidFileName, final String destAndroidStringPath, String translationsPath,
			String iOSStringFileName, List<String> langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {

			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();

				if (skipLang(lang, langs)) {
					continue;
				}

				final String androidLangDir = getAndroidLangDir(lang);

				if (namesMap.get(lang) == null) {
					namesMap.put(lang, new ArrayList<String>());
				}

				final List<String> stringNames = namesMap.get(lang);

				InputStream input = readFile(translationsPath + "/" + lang + "/" + iOSStringFileName);

				IOSInputStrategy inputStrategy = IOSInputStrategy.getInstance();
				final AndroidOutputStrategy outputStrategy = AndroidOutputStrategy.getInstance();

				inputStrategy.createInputValues(input, new InputCreatorListener() {
					
					@Override
					public void didFinishInputCreation(StringsStructure structure) {
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

						try {
							writeFile(destAndroidStringPath + "/" + androidLangDir + "/", androidFileName, output);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				});
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void transformAllSpecificStrings(final String destAndroidStringPath, String translationsPath,
			final String androidSpecificStringsFile, List<String> langs) {

		try (DirectoryStream<Path> directoryStream = Files
				.newDirectoryStream(FileSystems.getDefault().getPath(translationsPath), new DirectoriesFilter())) {
			for (Path path : directoryStream) {

				String lang = path.getFileName().toString();

				if (skipLang(lang, langs)) {
					continue;
				}

				final String androidLangDir = getAndroidLangDir(lang);

				if (!Files.exists(Paths.get(translationsPath + "/" + lang + "/" + androidSpecificStringsFile))) {
					continue;
				}

				InputStream input = readFile(translationsPath + "/" + lang + "/" + androidSpecificStringsFile);

				AndroidInputStrategy inputStrategy = AndroidInputStrategy.getInstance();
				final AndroidOutputStrategy outputStrategy = AndroidOutputStrategy.getInstance();

				inputStrategy.createInputValues(input, new InputCreatorListener() {
					
					@Override
					public void didFinishInputCreation(StringsStructure structure) {
						structure = outputStrategy.preprocessInputNames(structure);

						String output = outputStrategy.generateOutput(structure);

						try {
							writeFile(destAndroidStringPath + "/" + androidLangDir + "/", androidSpecificStringsFile, output);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();						}

						
					}
				});
				
			}
		} catch (Exception ex) {
			System.out.println("ERROR " + ex);
		}

	}

	private String getAndroidLangDir(String lang) {

		// iOS like dir
		String androidLangDir = lang.replace("-", "-r").replace("Hans", "CN").replace("Hant", "TW");

		if (androidLangDir.equals("") || lang.equals(DEFAULT_LANG)) { // default
			// res
			androidLangDir = "values";
		} else {
			androidLangDir = "values-" + androidLangDir;
		}
		return androidLangDir;
	}

	private boolean skipLang(String langToCheck, List<String> langs) {

		for (String lang : langs) {
			if (langToCheck.contains(lang)) {
				return false;
			}
		}
		return true;
	}

	private InputStream readFile(String path) throws IOException {
		File file = new File(path);
		return new FileInputStream(file);
	}

	private void writeFile(String dirPath, String fileName, String content) throws IOException {

		Path parentDir = Paths.get(dirPath);
		Path filePath = Paths.get(dirPath + fileName);

		if (!Files.exists(parentDir))
			Files.createDirectories(parentDir);

		if (Files.exists(filePath)) {
			new PrintWriter(dirPath + fileName).close();
		}

		Files.write(filePath, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
	}

	public static class DirectoriesFilter implements Filter<Path> {
		@Override
		public boolean accept(Path entry) throws IOException {
			return Files.isDirectory(entry);
		}
	}

}
