package edu.cmu.lti.f14.team05;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;

import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {
	private GoPubMedService service = null;
	private List<String> stopWordList = null;
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File stopWordFile = new File("src/main/resources/stopwords.txt");
		try {
			stopWordList = new ArrayList<String>(Arrays.asList(FileUtils.file2String(stopWordFile).split("\n")));
		} catch (IOException e) {
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
			text = text.toLowerCase();
			String [] textList = text.split(" ");
			StringBuilder newStr = new StringBuilder();
			for (int i=0;i<textList.length;i++) {
				if (!stopWordList.contains(textList[i])) {
					newStr.append(textList[i] + " ");
				}
			}
			text = newStr.toString().trim();
			//System.out.println("*****"+text);
		
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
