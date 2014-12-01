package edu.cmu.lti.f14.team05;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.uima.util.FileUtils;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.TagLattice;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;

public class QueryUtil {
	private static final String POSModelPath = "src/main/resources/models/pos-en-bio-genia.HiddenMarkovModel";
	private static final int MAX_N_BEST_CHUNKS = 5;
	private static List<String> stopWordList = null;

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
		
		// 3. Only noun
		oText = POS(oText, "NN");
		
		// 4. stemming
		
		//oText = StanfordLemmatizer.stemText(oText);

		// 5. remove stop words
		String[] textList = oText.split("\\s+");
		StringBuilder newStr = new StringBuilder();
		for (int i = 0; i < textList.length; i++) {
			//textList[i] = StanfordLemmatizer.stemWord(textList[i]);
			if (!stopWordList.contains(textList[i])) {
				String stemStr = StanfordLemmatizer.stemWord(textList[i]);
				if (!stemStr.equals(textList[i])) {
					newStr.append("(" + stemStr + " OR " + textList[i] + ")  ");
				} else {
					newStr.append(textList[i] + "  ");
				}
			}
		}
		oText = newStr.toString().trim();
		return oText;
	}

	final static TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory(
			"(-|'|\\d|\\p{L})+|\\S");

	public static void NER(String oText) {
		File modelFile = new File(
				"src/main/resources/models/ne-en-bio-genetag.HmmChunker");

		Chunker chunker = null;
		try {
			chunker = (Chunker) AbstractExternalizable.readObject(modelFile);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Chunking chunking = chunker.chunk(oText);
		System.out.println("Chunking=" + chunking);
	}

	public static String POS(String oText, String type) {

		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(POSModelPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectInputStream objIn = null;
		try {
			objIn = new ObjectInputStream(fileIn);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		HiddenMarkovModel hmm = null;
		try {
			hmm = (HiddenMarkovModel) objIn.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Streams.closeQuietly(objIn);
		HmmDecoder decoder = new HmmDecoder(hmm);
		Tokenizer tokenizer = TOKENIZER_FACTORY.tokenizer(oText.toCharArray(),
				0, oText.length());
		String[] tokens = tokenizer.tokenize();
		List<String> tokenList = Arrays.asList(tokens);
		Tagging<String> tagging = decoder.tag(tokenList);
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < tagging.size(); ++i) {
			if (tagging.tag(i).indexOf(type) > -1)
				result.append(tagging.token(i) + " ");
		}
		return result.toString();
	}
}
