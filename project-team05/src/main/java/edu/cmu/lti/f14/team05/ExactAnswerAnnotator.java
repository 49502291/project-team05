package edu.cmu.lti.f14.team05;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.TypeFactory;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Passage;



public class ExactAnswerAnnotator extends JCasAnnotator_ImplBase {
	
	
	private static ConfidenceChunker chunker = null;
	public static final String Modelfile = "Directory";
	public static final int MaxRetrivalNum =100;
	
	/**
	 * @author Yichen Cai
	 * 
	 * Description: Initialize method is used to load trained model of LingPipe
	 * 
	 * @param UimaContext
	 *
	 */
	
	public void initialize(UimaContext context) throws ResourceInitializationException {
		

		String file = (String) context.getConfigParameterValue(Modelfile);
		
		try {
			chunker = (ConfidenceChunker) AbstractExternalizable.readResourceObject(ExactAnswerAnnotator.class, file);
			System.out.println("Reading chunker from file=" + file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

		FSIterator<Annotation> itQuestion = aJCas.getAnnotationIndex(Question.type).iterator();
		Question question = (Question) itQuestion.next();
		String type = question.getQuestionType();
		String questionText = question.getText();
		questionText = preprocess(questionText);
		
		if(type != "LIST")
			return;
		
		
		
		FSIterator<TOP> itConcept = aJCas.getJFSIndexRepository().getAllIndexedFS(ConceptSearchResult.type);
		int conceptLabelCounter = 0; 
		String conceptLabel = new String();

		
		// Get concept label with max Score retrieved from Mesh service
		while(itConcept.hasNext())
		{
			ConceptSearchResult concept = (ConceptSearchResult) itConcept.next();
			
			if(concept.getServiceType() == "mesh" && conceptLabelCounter < 2)
			{
;
			   conceptLabel += concept.getConcept().getName();
			   conceptLabel += " ";
			   conceptLabelCounter++;
			}
			
			if(conceptLabelCounter == 2)
				break;
			
		}
		
		//Get exact answer
		
		double threshold = 0.1;
		Chunking chunking =null;
		int AnswerRetrievalNum = 0;		
		FSIterator<TOP> itPassage = aJCas.getJFSIndexRepository().getAllIndexedFS(Passage.type);

		
		if(conceptLabel != null)
		{
			conceptLabel = preprocess(conceptLabel);
			
			List<String> conceptList = tokenize0(conceptLabel);
			Set<String> conceptSet = List2Set(conceptList);
			
			int SetSize = conceptSet.size();		
			
			while(itPassage.hasNext() && AnswerRetrievalNum < MaxRetrivalNum)
			{
			   Passage snippet = (Passage) itPassage.next();
			   String temp = snippet.getText();
			   String text = temp;
			   temp = preprocess(temp);
			   
			   int count = 0;
			   
			   for(String token : conceptSet)
			   {
				   if(temp.indexOf(token) != -1)
				   {
					   count ++ ;
				   }
				   
			   }
			   
	//		   System.out.println( (double)count/(double)SetSize);
			   
			   if ((double)count/(double)SetSize > threshold)
			   {
				   char cs[] =text.toCharArray();
										
	               Iterator<Chunk> chunkit = chunker.nBestChunks(cs, 0, cs.length, 10);

						
				    while(chunkit.hasNext()){
							
					Chunk chunk = chunkit.next();   
					double conf = Math.pow(2.0, chunk.score());

					if(conf > 0.6)
					{
						String result = text.substring(chunk.start(), chunk.end());
						String answer = result;
						result = preprocess(result);
						
						
						if(questionText.indexOf(result) == -1){
						Answer exactAnswer = TypeFactory.createAnswer(aJCas, answer);
						exactAnswer.addToIndexes();
						
						AnswerRetrievalNum ++;
						}
					}
					
				   }
				   
			   }
			   	
			}
		}
		
		else
		{
			while(itPassage.hasNext() && AnswerRetrievalNum < MaxRetrivalNum)
			{
			   Passage snippet = (Passage) itPassage.next();
			   String text = snippet.getText();
		
				   char cs[] =text.toCharArray();
										
	               Iterator<Chunk> chunkit = chunker.nBestChunks(cs, 0, cs.length, 10);

						
				    while(chunkit.hasNext()){
							
					Chunk chunk = chunkit.next();   
					double conf = Math.pow(2.0, chunk.score());

					if(conf > 0.6)
					{
						String result = text.substring(chunk.start(), chunk.end());
						String answer = result;
						result = preprocess(result);
						
						
						if(questionText.indexOf(result) == -1){
						Answer exactAnswer = TypeFactory.createAnswer(aJCas, answer);
						exactAnswer.addToIndexes();
						
						AnswerRetrievalNum ++;
						}
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
	 *   List  to  HashSet
	 *  	
	 */
	
	 Set<String> List2Set(List<String> list) {
		
		 Set<String> result = new HashSet<String>();
		 
		 for(String s : list)
		 {
			result.add(s); 
		 }
		 
		 return result;

		 
	 }

	 
		public static String preprocess(String oText) {
			
			// 1. only use alphabets
			oText = oText.replaceAll("[^a-zA-Z0-9]+", " ");

			// 2. to lower case
			oText = oText.toLowerCase();
			
			//3. Stemming
			oText = StanfordLemmatizer.stemText(oText);
			
			return oText;
		}
		
		
}




