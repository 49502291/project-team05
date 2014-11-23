package edu.cmu.lti.f14.team05;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.AtomicQueryConcept;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class TripleAnnotator extends JCasAnnotator_ImplBase {
	public GoPubMedService service=null;
	public PubMedSearchServiceResponse.Result pubmedResult=null;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException{
		super.initialize(aContext);

		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type).iterator();
		while(iter.hasNext())
		{
			Question qt = (Question) iter.next();
			String text = qt.getText().substring(0,qt.getText().length()-1); //remove "?"
			text = text.replaceAll("[^a-zA-Z0-9]+", " ");//remove punctuations
		
			if (text == null)
				text  = "Is Rheumatoid Arthritis more common in men or women";
		
			try {				
				  int rankOfTriple = 1;
				    LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
				            .findLinkedLifeDataEntitiesPaged(text, 0);
				    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
				      for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
				    	  Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(), relation.getObj());
			              TypeFactory.createTripleSearchResult(aJCas, triple, text).setRank(rankOfTriple);
			              rankOfTriple++;
			              TypeFactory.createTripleSearchResult(aJCas, triple, text).setScore(entity.getScore());
				    	  TypeFactory.createTripleSearchResult(aJCas, triple, text).addToIndexes();
				      }
				    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
		}
	}
}

