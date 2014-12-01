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

	private GoPubMedService service = null;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		// 1. GoPubMed service
		try {
			service = new GoPubMedService("project.properties");
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = aJCas.getAnnotationIndex(Question.type)
				.iterator();
		while (iter.hasNext()) {
			Question qt = (Question) iter.next();
			String text = qt.getText();
			text = QueryUtil.preprocess(text);
			// text = QueryUtil.POS(text);
			// String query = buildQuery(text);
			text = text.replaceAll("\\|", " AND ");
			System.out.println(qt.getText());
			int resultLength = 0;
			try {
				PubMedSearchServiceResponse.Result pubmedResult = null;
				while (resultLength == 0 && !text.isEmpty()) {
					System.out.println(text);
					pubmedResult = service.findPubMedCitations(text, 0);
					resultLength = pubmedResult.getDocuments().size();
					System.out.println("Done");
					if (resultLength == 0) {
						String [] parts = text.split(" AND ");
						StringBuilder newStr = new StringBuilder();
						for (int i=1;i<parts.length;i++){
							newStr.append(parts[i]);
							if (i < parts.length -1) {
								newStr.append("|");
							}
						}
						text = newStr.toString().trim().replaceAll("\\|", " AND ").trim();
					}
				}

				int rankOfDocument = 1;
				for (Document doc : pubmedResult.getDocuments()) {
					String pmid = doc.getPmid();
					String uri = "http://www.ncbi.nlm.nih.gov/pubmed/" + pmid;
					edu.cmu.lti.oaqa.type.retrieval.Document document = TypeFactory
							.createDocument(aJCas, uri, "xxxx", rankOfDocument,
									"cccc", doc.getPmid(), doc.getPmid());
					rankOfDocument++;
					document.addToIndexes();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
