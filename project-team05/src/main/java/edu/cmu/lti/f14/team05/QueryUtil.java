package edu.cmu.lti.f14.team05;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.util.FileUtils;

public class QueryUtil {
	private static List<String> stopWordList = null;
	public QueryUtil() {
	}
	
	private static void readStopWords() {
		File stopWordFile = new File("src/main/resources/stopwords.txt");
		try {
			stopWordList = new ArrayList<String>(Arrays.asList(FileUtils
					.file2String(stopWordFile).split("\n")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String preprocess(String oText) {
		// 0. read stop words
		if (stopWordList == null)
			readStopWords();

		// 1. only use alphabets
		oText = oText.replaceAll("[^a-zA-Z0-9]+", " ");

		// 2. to lower case
		oText = oText.toLowerCase();

		// 3. stemming
		oText = StanfordLemmatizer.stemText(oText);

		// 4. remove stop words
		String[] textList = oText.split("\\s+");
		StringBuilder newStr = new StringBuilder();
		for (int i = 0; i < textList.length; i++) {
			textList[i] = StanfordLemmatizer.stemWord(textList[i]);
			if (!stopWordList.contains(textList[i])) {
				newStr.append(textList[i] + " ");
			}
		}
		oText = newStr.toString().trim();
		return oText;
	}
}
