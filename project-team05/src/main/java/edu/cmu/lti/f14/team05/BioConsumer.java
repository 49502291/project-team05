package edu.cmu.lti.f14.team05;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import com.google.common.collect.Lists;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class BioConsumer extends CasConsumer_ImplBase {
	FileWriter fout;
	@Override
	public void initialize() throws ResourceInitializationException{
		try {
			fout = new FileWriter("report.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		// TODO Auto-generated method stub
		JCas jcas = null;
		try {
			jcas = aCAS.getJCas();
		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Question currentQuestion = TypeUtil.getQuestion(jcas);
			fout.write("CurrentQuestion:" + currentQuestion.getText() + "\n");
			fout.write("CurrentType:" + currentQuestion.getQuestionType() + "\n");
			fout.write("Documents:\n");
			Collection<Document> docCollection = TypeUtil.getRankedDocuments(jcas);
		
			if (!docCollection.isEmpty()) {
				for (Document doc:docCollection) {
					
				}
			}
			
			
			fout.write("\n");
			fout.write("Concepts:\n");
			Collection<ConceptSearchResult> conceptCollection = TypeUtil.getRankedConceptSearchResults(jcas);
			for (ConceptSearchResult result: conceptCollection) {
				fout.write(result.getUri() + "\n");
			}
	
			fout.write("\n");
			fout.write("Triples:\n");
			Collection<TripleSearchResult> tripleCollection = TypeUtil.getRankedTripleSearchResults(jcas);
			for (TripleSearchResult result: tripleCollection) {
				if (result != null) {
					Triple t = result.getTriple();
					if (t != null)
						fout.write(t.getObject() + "\n");
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
    IOException {
		fout.close();
	}

}
