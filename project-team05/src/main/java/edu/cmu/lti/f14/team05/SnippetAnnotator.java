package edu.cmu.lti.f14.team05;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import json.gson.FullDocument;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.TypeFactory;

import com.aliasi.hmm.HiddenMarkovModel;
import com.aliasi.hmm.HmmDecoder;
import com.aliasi.tag.Tagging;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Streams;
import com.google.gson.Gson;

import edu.cmu.lti.oaqa.type.retrieval.*;



public class SnippetAnnotator extends JCasAnnotator_ImplBase {

		
	private static List<String> stopWordList = null;
	private static final String POSModelPath = "src/main/resources/models/pos-en-bio-genia.HiddenMarkovModel";
	private final static TokenizerFactory TOKENIZER_FACTORY = new RegExTokenizerFactory(
			"(-|'|\\d|\\p{L})+|\\S");
	private static  HiddenMarkovModel hmm = null;
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
	
		stopWordList = QueryUtil.readStopWords();
		prepareModel();
		System.out.println("Read Model: HiddenMarkovModel...");
		
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
    
	    FSIterator<TOP> it = aJCas.getJFSIndexRepository().getAllIndexedFS(Document.type);
		
		while(it.hasNext())	
		{
			Document annotation =  (Document)it.next();
			
			String PMID =  annotation.getDocId();
	
			String prefix = "http://metal.lti.cs.cmu.edu:30002/pmc/";
			String fullURL= prefix+PMID;
			
	        String query = annotation.getQueryString();
	        query = preprocess(query);
	        
		    Map<String,Integer> qVector  = createTermFreqVector(query);
			
			//Get full Document Text
			URL url;  
	        String temp;  
	        StringBuffer sb = new StringBuffer();  
	        try {  
	            url = new URL(fullURL);  
	            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "gbk"));
	            while ((temp = in.readLine()) != null) {  
	                sb.append(temp);  
	            }  
	            in.close();  
	        } catch (final MalformedURLException me) {  
	            System.out.println("Wrong URL");  
	            me.getMessage();  
	        } catch (final IOException e) {  
	            e.printStackTrace();  
	        }  
	        
	        
	        String content = sb.toString();
	        
	        
	        if(content.isEmpty())
            {
	           content = annotation.getAbstract();
	           
	           if(content.isEmpty()){
        	      System.out.println ("Document" +PMID+"not available" );
        	      continue;
	           }
	           
	         //  content =preprocess(content);
	           
	           List<String> sentences = tokenize1(content);
	        	
	        	int offsetInBeginSection = 0;
	            
	        	for(int j =0 ; j<sentences.size(); j++)
	        	{
	        		String sentence = sentences.get(j);
	        		String text = sentence;
	        		sentence = preprocess(sentence);
	        		
	        		Map<String,Integer> aVector = createTermFreqVector(sentence);
	        	
	        		double similarity = computeCosineSimilarity(qVector,aVector);
	        		
	        		System.out.println(similarity);
	        		
       			    int offsetBegin = offsetInBeginSection;
       			    int offsetEnd = offsetBegin + text.length()-1;
       			
	        		//Set threshold
	        		
	        		if(similarity > 0.3)
	        		{
	        			String beginSection = "sections.0";
	        			String endSection ="sections.0";
	        			String document =fullURL;
	        		//	String text = sentence;
	        			
	        			Passage snippet = TypeFactory.createPassage(aJCas, document, text, offsetBegin, offsetEnd, beginSection, endSection);
	        			
	        			snippet.addToIndexes();
	        			
	        		}
	        		
	        		offsetInBeginSection = offsetEnd + 2;
	        		
	        	}
	           
	           
            } 
	        
	        
	        else{
	        	
	     //   content =preprocess(content);	
	        //Parse Json data
	        Gson gson =new Gson();
	        FullDocument fd =gson.fromJson(content, FullDocument.class);
	     	        
	        for(int i =0 ; i<fd.getSections().size() ; i++)
	        {
	        	String section = fd.getSections().get(i);
	        	
	        	List<String> sentences = tokenize1(section);
	        	
	        	int offsetInBeginSection = 0;
	            
	        	for(int j =0 ; j<sentences.size(); j++)
	        	{
	        		String sentence = sentences.get(j);
	        		String text = sentence;
	        		sentence = preprocess(sentence);
	        		
	        		Map<String,Integer> aVector = createTermFreqVector(sentence);
	        		
	        		double similarity = computeCosineSimilarity(qVector,aVector);
	        		
	        		System.out.println(similarity);
	        		
        			int offsetBegin = offsetInBeginSection;
        			int offsetEnd = offsetBegin + text.length()-1;
        			
	        		//Set threshold
	        		
	        		if(similarity > 0.25)
	        		{
	        			String beginSection = "sections." +i;
	        			String endSection ="sections." +i;
	        			String document ="http://www.ncbi.nlm.nih.gov/pubmed/"+PMID;
//	        			String text = sentence;
	        			
	        			Passage snippet = TypeFactory.createPassage(aJCas, document, text, offsetBegin, offsetEnd, beginSection, endSection);
	        			
	        			snippet.addToIndexes();
	        			
	        		}
	        		
	        		offsetInBeginSection = offsetEnd + 2;
	        		
	        	}
	        }
	        
	       }
		}
		
			
	}
	
	

	
	/**
	   * A basic white-space tokenizer, it deliberately does not split on punctuation!
	   *
		 * @param doc input text
		 * @return    a list of tokens.
		 */

		List<String> tokenize0(String doc) {
		  List<String> res = new ArrayList<String>();
		  
		  for (String s: doc.split("\\s+"))
		    res.add(s);
		  return res;
		}

		
		/**
		   * A basic period tokenizer, it deliberately does not split on punctuation!
		   *
			 * @param doc input text
			 * @return    a list of tokens.
			 */

			List<String> tokenize1(String doc) {
			  List<String> res = new ArrayList<String>();
			  
			  for (String s: doc.split("\\."))
			    res.add(s);
			  return res;
			}

		
		/**
		 * This methods construct a vector of tokens and update the tokenlist in CAS by
		 * using naive white space tokenizer. 
		 * 
		 * @param String
		 * 
		 */

		private Map<String,Integer> createTermFreqVector(String text) {
			
			List<String> terms = tokenize0(text);
			
			Map<String,Integer> vector = new HashMap();
			
			for (int i =0; i<terms.size();i++)
			{
				String word = terms.get(i);
				
				if (stopWordList.contains(word))
					continue;
				
				if(!vector.containsKey(word))
				{
					vector.put(word, 1);
				}
				else{
					vector.put(word, vector.get(word)+1);
				}
			}
			
		    return vector;
			
		}
		
		/**
		 * This method compute cosine similarity between query  and document
		 * @param Map<String, Integer> queryVector,Map<String, Integer> docVector
		 * @return cosine_similarity
		 */
		private double computeCosineSimilarity(Map<String, Integer> queryVector,
				Map<String, Integer> docVector) {
			double cosine_similarity=0.0;
			double numerator= 0.0, d1 =0.0, d2=0.0;
			int value1, value2;

			// TODO :: compute cosine similarity between two sentences
			Map<String,Integer> query = new HashMap<String, Integer>(queryVector);
			Map<String,Integer> answer = new HashMap<String, Integer>(docVector);
			
			if(queryVector.isEmpty()|| docVector.isEmpty())
				return 0.0;
			
			for (Map.Entry<String, Integer> entry : query.entrySet()) { 
				value1 =entry.getValue();
				if(answer.containsKey(entry.getKey()))
					value2 = answer.get(entry.getKey());
				else
					value2=0;
				
				answer.remove(entry.getKey());
				
				numerator += value1* value2;
				d1 += value1*value1;
				d2 += value2*value2;
			}
			
			for (Map.Entry<String, Integer> entry : answer.entrySet()) { 
				value2 = entry.getValue();
				d2 += value2*value2;
				
			}
			
			cosine_similarity =numerator / (Math.sqrt(d1*d2));
					
			return cosine_similarity ;
		}
		
		public static String preprocess(String oText) {
	
			// 1. only use alphabets
			oText = oText.replaceAll("[^a-zA-Z0-9]+", " ");

			// 2. to lower case
			oText = oText.toLowerCase();
			
			// 3. Only noun
			oText = POS(oText, "NN");
			
			//4. Stemming
			oText = StanfordLemmatizer.stemText(oText);
			
			return oText;
		}
		
		public static String POS(String oText, String type) {
		
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
		
		private static void prepareModel()
		{
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
		}
		
}
