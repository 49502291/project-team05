package edu.cmu.lti.f14.team05;

import java.io.IOException;

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
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {
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
		
		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type).iterator();
		while(iter.hasNext())
		{
			Question qt = (Question) iter.next();
			String text = qt.getText().substring(0,qt.getText().length()-1); //remove "?"
			text = text.replaceAll("[^a-zA-Z0-9]+", " ");//remove punctuations
		
			if (text == null)
				text  = "Is Rheumatoid Arthritis more common in men or women";
		
			try {
				PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
				
				int rankOfDocument=1;
				for (Document doc : pubmedResult.getDocuments()) {
		    	String pmid = doc.getPmid();
		    	String uri = "http://www.ncbi.nlm.nih.gov/pubmed/" + pmid;
		    	edu.cmu.lti.oaqa.type.retrieval.Document document = 
						TypeFactory.createDocument(aJCas,uri,"xxxx",rankOfDocument,"cccc",doc.getPmid(),doc.getPmid());
		    	rankOfDocument++;
		    	document.addToIndexes();
		    	}
			} catch (IOException e) {
		        e.printStackTrace();
				}
		}
	}
	
}
