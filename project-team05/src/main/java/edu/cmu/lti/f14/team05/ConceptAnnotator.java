package edu.cmu.lti.f14.team05;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.client.ClientProtocolException;
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

	private static GoPubMedService service = null;
	
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
	public static String identifySingleConcept(String text) {
		double maxScore = 0;
		text = text.toLowerCase().trim().replaceAll("^a-z0-9", "");
		String concept = text;
		List<OntologyServiceResponse.Result> results = new ArrayList<OntologyServiceResponse.Result>();
		System.out.println("Finding:" + text);
		try {
			System.out.println("Disease");
			results.add(service.findDiseaseOntologyEntitiesPaged(text, 0, 1));
			System.out.println("Gene");
			results.add(service.findGeneOntologyEntitiesPaged(text, 0, 1));
			System.out.println("Jochem");
			results.add(service.findJochemEntitiesPaged(text, 0, 1));
			System.out.println("Mesh");
			results.add(service.findMeshEntitiesPaged(text, 0, 1));
			System.out.println("Uniprot");
			results.add(service.findUniprotEntitiesPaged(text, 0, 1));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (OntologyServiceResponse.Result result : results) {
			if (result.getFindings().size() > 0 && maxScore < result.getFindings().get(0).getScore()) {
				OntologyServiceResponse.Finding finding = result.getFindings().get(0);
				if (maxScore < finding.getScore() && finding.getScore() > 0.8) {
					maxScore = finding.getScore();
					concept = finding.getConcept().getLabel();
				}
			}
		}
		System.out.println("SCORE:" + maxScore);
		return concept.toLowerCase().trim();
	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub

		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type).iterator();
		int rankOfConcept;
		while(iter.hasNext()) {
			Question qt = (Question) iter.next();
			String text = qt.getText();
			
			//text = QueryUtil.preprocess(text);
			//QueryUtil.POS(text);
			/*
			 * Disease Ontology
			 */
			try {
				System.out.println("Finding disease ontology for:" + text);
				OntologyServiceResponse.Result diseaseOntologyResult = service
			            .findDiseaseOntologyEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	System.out.println("Concpet:" + finding.getConcept().getLabel()+ " score: "+finding.getScore());
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.setScore(finding.getScore());
			    	conceptSearchResult.setServiceType("disease");
			    	conceptSearchResult.addToIndexes();
			    	rankOfConcept++;	        	
			    }
			} catch (IOException e) {
		        e.printStackTrace();
			}
			
			//if (true) continue;
			/*
			 * Gene Ontology
			 */
			try {
				
				System.out.println("Finding gene ontology for:" + text);
				OntologyServiceResponse.Result geneOntologyResult = service
			            .findGeneOntologyEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.5) break;
			    	System.out.println("Concpet:" + finding.getConcept().getLabel()+ " score: "+finding.getScore());
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.setScore(finding.getScore());
			    	conceptSearchResult.setServiceType("gene");
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
				System.out.println("Finding jochem ontology for:" + text);
				OntologyServiceResponse.Result jochemEntitiesResult = service
			            .findJochemEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : jochemEntitiesResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	System.out.println("Concpet:" + finding.getConcept().getLabel()+ " score: "+finding.getScore());
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.setScore(finding.getScore());
			    	conceptSearchResult.setServiceType("jochem");
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
				System.out.println("Finding mesh ontology for:" + text);
				OntologyServiceResponse.Result meshResult = service
			            .findMeshEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	System.out.println("Concpet:" + finding.getConcept().getLabel()+ " score: "+finding.getScore());
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.setScore(finding.getScore());
			    	conceptSearchResult.setServiceType("mesh");
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
				System.out.println("Finding uniprot ontology for:" + text);
				OntologyServiceResponse.Result uniprotResult = service
			            .findUniprotEntitiesPaged(text, 0);
				rankOfConcept = 1;

				for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
			    	
			    	if (finding == null || finding.getScore() < 0.1) break;
			    	
			    	ConceptSearchResult conceptSearchResult = TypeFactory.createConceptSearchResult(aJCas,
			    			TypeFactory.createConcept(aJCas, finding.getConcept().getLabel(), 
			    					finding.getConcept().getUri()), finding.getConcept().getUri());
			    	conceptSearchResult.setRank(rankOfConcept);
			    	conceptSearchResult.setScore(finding.getScore());
			    	conceptSearchResult.setServiceType("uniprot");
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
