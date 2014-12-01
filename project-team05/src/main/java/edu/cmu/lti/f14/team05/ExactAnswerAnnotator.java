package edu.cmu.lti.f14.team05;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;

import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Passage;



public class ExactAnswerAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

		FSIterator<TOP> itConcept = aJCas.getJFSIndexRepository().getAllIndexedFS(ConceptSearchResult.type);
		
		
	
		
		FSIterator<TOP> itPassage = aJCas.getJFSIndexRepository().getAllIndexedFS(Passage.type);
		
		while(itPassage.hasNext())
		{
		   Passage snippet = (Passage) itPassage.next();
		   
		   
		
		}
		
	}

}
