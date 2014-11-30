package edu.cmu.lti.f14.team05;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ConfidenceChunker;
import com.aliasi.util.AbstractExternalizable;

import util.TypeFactory;
import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;
import edu.cmu.lti.oaqa.type.input.Question;

public class DocumentAnnotator extends JCasAnnotator_ImplBase {
//	private static final String MODEL_PATH = "/models/ne-en-bio-genetag.HmmChunker";
//	private ConfidenceChunker chunker;
//	private static final int MAX_N_BEST_CHUNKS = 5;
	private GoPubMedService service = null;
	
	StanfordLemmatizer lemmatizer = null;
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		
		// 1. Stanford lemmatizer
		lemmatizer = new StanfordLemmatizer();
		
		// 2. GoPubMed service
		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		// 4. Pingpipe
//		try {
//			chunker = (ConfidenceChunker) AbstractExternalizable
//					.readResourceObject(MODEL_PATH);
//			// .readObject(modelFile);
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/**
	 * 
	 * Helper functions
	 * 
	 */
	
	
//	private String buildQuery(String oText) {
//		
//		List<String> bioWords = new ArrayList<String>();
//		List<String> helperWords = new ArrayList<String>();
//		
//		int nBestChunks = MAX_N_BEST_CHUNKS;
//		List<String> wordList = new ArrayList<String>(Arrays.asList(oText.split(" ")));
//		int nWords = wordList.size();
//		if (nWords > 20) {
//			nBestChunks = nWords / 4;
//		}
//		Iterator<Chunk> chunking = chunker.nBestChunks(
//				oText.toCharArray(), 0, oText.length(), nBestChunks);
//
//		while (chunking.hasNext()) {
//			Chunk chunk = chunking.next();
//			int start = chunk.start();
//			int end = chunk.end();
//			double confidence = Math.pow(2.0, chunk.score());
//			String text = oText.substring(start, end);
//			if (confidence >= 0.1) {
//				bioWords.add(text);
//			} else if (wordList.contains(text)){
//				helperWords.add(text);
//			}
//		}
//		StringBuilder query = new StringBuilder();
//		if (!bioWords.isEmpty()) {
//			query.append("(")
//				.append("\"" + bioWords.get(0) + "\"");
//			for (int i = 1;i<bioWords.size();i++) {
//				query.append(" AND ")
//					.append("\"" + bioWords.get(i) + "\"");
//			}
//			query.append(")");
//		}
//		if (!helperWords.isEmpty()) {
//			if (query.length() > 0) {
//				query.append(" AND ");
//			}
//			query.append("(")
//				.append("\"" + helperWords.get(0) +"\"");
//			for (int i = 1;i<helperWords.size();i++) {
//				query.append(" OR ")
//					.append("\"" + helperWords.get(i)+"\"");
//			}
//			query.append(")");
//		}
//		return query.toString();
//	}
	
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type).iterator();
		while(iter.hasNext())
		{
			Question qt = (Question) iter.next();
			String text = qt.getText();
			text = QueryUtil.preprocess(text);
//			String query = buildQuery(text);
//			System.out.println(query);
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
