package edu.cmu.lti.f14.team05;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		if(type != "LIST")
			return;
		
		
		
		FSIterator<TOP> itConcept = aJCas.getJFSIndexRepository().getAllIndexedFS(ConceptSearchResult.type);
		Double maxScore = 0.0; 
		String conceptLabel = null;
		
		// Get concept label with max Score retrieved from Mesh service
		while(itConcept.hasNext())
		{
			ConceptSearchResult concept = (ConceptSearchResult) itConcept.next();
			
			if(concept.getServiceType() == "mesh")
			{
				if(concept.getScore() > maxScore)
				{
					maxScore = concept.getScore();
					conceptLabel = concept.getConcept().getName();
				}
			}
			
		}
		
		if(conceptLabel == null)
		{
			System.out.println("concept Label is null");
			return;
		}
		
		List<String> conceptToken = tokenize0(conceptLabel);
		int listSize = conceptToken.size();		
		double threshold = 0.6;
		Chunking chunking =null;
		int AnswerRetrievalNum = 0;
		
		FSIterator<TOP> itPassage = aJCas.getJFSIndexRepository().getAllIndexedFS(Passage.type);
		
		while(itPassage.hasNext() && AnswerRetrievalNum < MaxRetrivalNum)
		{
		   Passage snippet = (Passage) itPassage.next();
		   String text = snippet.getText();
		   int count = 0;
		   
		   for(String token : conceptToken)
		   {
			   if(text.indexOf(token) != -1)
			   {
				   count ++ ;
			   }
			   
		   }
		   
		   if (count/listSize > threshold)
		   {
			   char cs[] =text.toCharArray();
									
               Iterator<Chunk> chunkit = chunker.nBestChunks(cs, 0, cs.length, 10);

					
			    while(chunkit.hasNext()){
						
				Chunk chunk = chunkit.next();   
				double conf = Math.pow(2.0, chunk.score());

				if(conf > 0.6)
				{
					String answer = text.substring(chunk.start(), chunk.end());
					
					if(questionText.indexOf(answer) == -1){
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


}




