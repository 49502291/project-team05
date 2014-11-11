package annotator;

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
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;

public class BioAsqAnnotator extends JCasAnnotator_ImplBase {

	private GoPubMedService service;
	public void initializer(UimaContext aContext) throws ResourceInitializationException {
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
			String text = qt.getText();			
			try {
				OntologyServiceResponse.Result diseaseOntologyResult = service
			            .findDiseaseOntologyEntitiesPaged(text, 0);
				List<String> uris = new LinkedList<String>();
				//System.out.println("Disease ontology: " + diseaseOntologyResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
			    	uris.add(finding.getConcept().getUri());
			    	/*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    }
			    Concept concept = TypeFactory.createConcept(aJCas, "diseaseOntology", uris, null);
			    concept.addToIndexes();
			    OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(text,
			            0, 10);
			    //System.out.println("Gene ontology: " + geneOntologyResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri()); */
			    }
			    OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
			    //System.out.println("Jochem: " + jochemResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    }
			    OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
			    //System.out.println("MeSH: " + meshResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    }
			    OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
			    //System.out.println("UniProt: " + uniprotResult.getFindings().size());
			    for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
			      /*System.out.println(" > " + finding.getConcept().getLabel() + " "
			              + finding.getConcept().getUri());*/
			    }
			    LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
			            .findLinkedLifeDataEntitiesPaged(text, 0);
			    //System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
			    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
			      //System.out.println(" > " + entity.getEntity());
			      for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
			    	  Triple triple = TypeFactory.createTriple(aJCas, relation.getSubj(), relation.getPred(), relation.getObj());
		              triple.addToIndexes();
			    	  /*System.out.println("   - labels: " + relation.getLabels());
				        System.out.println("   - pred: " + relation.getPred());
				        System.out.println("   - sub: " + relation.getSubj());
				        System.out.println("   - obj: " + relation.getObj());*/
			      }
			    }
			    PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
			    //System.out.println("documents:" + pubmedResult.getSize());
			    for (Document doc : pubmedResult.getDocuments()) {
			    	String pmid = doc.getPmid();
			    	String uri = "http://www.ncbi.nlm.nih.gov/pubmed/" + pmid;
			    	edu.cmu.lti.oaqa.type.retrieval.Document document = 
							TypeFactory.createDocument(aJCas, uri, pmid);
			    	document.addToIndexes();
		          /*System.out.println("http://www.ncbi.nlm.nih.gov/pubmed/" + 
		           doc.getPmid());
		           */
			    }
			    //System.out.println(pubmedResult.getSize());
			} catch (IOException e) {
			        e.printStackTrace();
			    }
		}
	}

}
