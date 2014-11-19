package edu.cmu.lti.f14.team05;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.TypeFactory;
import util.Utils;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;

public class BioAsqAnnotator extends JCasAnnotator_ImplBase {

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
		while(iter.hasNext())
		{
			Question qt = (Question) iter.next();
			String text = qt.getText().substring(0,qt.getText().length()-1);
			//System.out.println(text);
			if (text == null)
				text  = "Is Rheumatoid Arthritis more common in men or women";
//			GoPubMedService service;
//			try {
//				service = new GoPubMedService("./project.properties");
			
			try {
				OntologyServiceResponse.Result diseaseOntologyResult = service
			            .findDiseaseOntologyEntitiesPaged(text, 0);
				List<String> uris = new LinkedList<String>();
			    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
			    	uris.add(finding.getConcept().getUri());
			    }
			    StringList uriList = Utils.createStringList(aJCas, uris);
			    Concept concept = new Concept(aJCas);
			    		//TypeFactory.createConcept(aJCas, "diseaseOntology", uris, null);
			    concept.setUris(uriList);
			    concept.addToIndexes();
			    
			    /*OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(text,
			            0, 10);
			    for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) { 
			    }
			    OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
			    for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
			    }
			    OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
			    for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
			    }
			    OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
			    for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
			    }*/
			    LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
			            .findLinkedLifeDataEntitiesPaged(text, 0);
			    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
			      for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
			    	  Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(), relation.getObj());
		              triple.addToIndexes();
			      }
			    }
			    PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
			    for (Document doc : pubmedResult.getDocuments()) {
			    	String pmid = doc.getPmid();
			    	String uri = "http://www.ncbi.nlm.nih.gov/pubmed/" + pmid;
			    	edu.cmu.lti.oaqa.type.retrieval.Document document = 
							TypeFactory.createDocument(aJCas, uri, pmid,text);
			    	document.addToIndexes();
			    }
			} catch (IOException e) {
			        e.printStackTrace();
			}
			}
		}
	

}
