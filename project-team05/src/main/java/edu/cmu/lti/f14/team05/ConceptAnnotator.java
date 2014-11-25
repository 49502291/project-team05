package edu.cmu.lti.f14.team05;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.coref.Matcher;

import util.TypeFactory;
import util.TypeUtil;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;

public class ConceptAnnotator extends JCasAnnotator_ImplBase {

private GoPubMedService service = null;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
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
		// TODO Auto-generated method stub

		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type).iterator();
		int rankOfConcept;
		while(iter.hasNext())
		{
			Question qt = (Question) iter.next();
			String text = qt.getText().substring(0,qt.getText().length()-1); //remove "?"
			text = text.replaceAll("[^a-zA-Z0-9]+", " ");//remove punctuations
		
			if (text == null)
				text  = "Is Rheumatoid Arthritis more common in men or women";
		
			/*
			 * Disease Ontology
			 */
			try {
				OntologyServiceResponse.Result diseaseOntologyResult = service
			            .findDiseaseOntologyEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			
			/*
			 * Gene Ontology
			 */
			try {
				OntologyServiceResponse.Result geneOntologyResult = service
			            .findGeneOntologyEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			
			
			/*
			 * Jochem Entities
			 */
			try {
				OntologyServiceResponse.Result jochemEntitiesResult = service
			            .findJochemEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : jochemEntitiesResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			
			/*
			 * Mesh Entities
			 */	
			try {
				OntologyServiceResponse.Result meshResult = service
			            .findMeshEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			
			/*
			 * Uniprot Entities
			 */
			try {
				OntologyServiceResponse.Result uniprotResult = service
			            .findUniprotEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			/*
			 * add result to CAS
			 */
			Collection<ConceptSearchResult> conceptSearchResult = TypeUtil.getRankedConceptSearchResults(aJCas);

			Collection<ConceptSearchResult> result = TypeUtil.rankedSearchResultsByScore(
					JCasUtil.select(aJCas, ConceptSearchResult.class), conceptSearchResult.size());
			rankOfConcept = 1;
			Iterator<ConceptSearchResult> it = result.iterator();
			while (it.hasNext()) {
				it.next().setRank(rankOfConcept);
				rankOfConcept++;	
			}
			
		}
	}
}
