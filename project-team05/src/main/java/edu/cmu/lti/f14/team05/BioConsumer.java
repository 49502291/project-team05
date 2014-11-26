package edu.cmu.lti.f14.team05;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import json.JsonCollectionReaderHelper;
import json.gson.TestQuestion;
import json.gson.Triple;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
//import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class BioConsumer extends CasConsumer_ImplBase {
	FileWriter fout;
	Map<String, List<String>> goldDocs;
	Map<String, List<String>> goldConcepts;
	Map<String, List<Triple>> goldTriples;
	List<TestQuestion> goldStandards;
	JsonCollectionReaderHelper jsonHelper;
	List<Double> docPrecisionList;
	
	
	@Override
	public void initialize() throws ResourceInitializationException{
		try {
			fout = new FileWriter("report.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonHelper = new JsonCollectionReaderHelper();
		goldStandards = jsonHelper.testRun();
		goldDocs = new HashMap<String, List<String>>();
		goldConcepts = new HashMap<String, List<String>>();
		goldTriples = new HashMap<String, List<Triple>>();
		for (TestQuestion question: goldStandards){
			goldDocs.put(question.getId(), question.getDocuments());
			goldConcepts.put(question.getId(), question.getConcepts());
			goldTriples.put(question.getId(), question.getTriples());
		}
		
		docPrecisionList = new ArrayList<Double>();
	}
	@Override
	public void processCas(CAS aCAS) throws ResourceProcessException {
		// TODO Auto-generated method stub
		int tpOfDocument = 0;
		int tpOfConcept = 0;
		int tpOfTriple = 0;
		double precisionOfDocument = 0.0;
		double precisionOfConcept = 0.0;
		double precisionOfTriple = 0.0;
		double recallOfDocument = 0.0;
		double recallOfConcept = 0.0;
		double recallOfTriple = 0.0;
		double FScoreOfDocument = 0.0;
		double FScoreOfConcept = 0.0;
		double FScoreOfTriple = 0.0;
		
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
			String queryId = TypeUtil.getQuestion(jcas).getId();
			List<String> docResult = goldDocs.get(queryId);
			
			Collection<Document> docCollection = TypeUtil.getRankedDocuments(jcas);

			LinkedList<Document> documentList = new LinkedList<Document>();
			documentList.addAll(docCollection);
			if (!documentList.isEmpty()){
				for (Document doc: documentList){
					fout.write(doc.getUri() + "\n");
					if(docResult.contains(doc.getUri()))
						tpOfDocument++;
				}
			}
			precisionOfDocument = (tpOfDocument * 1.0) / documentList.size() * 1.0;
			recallOfDocument = (tpOfDocument * 1.0) / docResult.size() * 1.0;
			if (precisionOfDocument == 0 && recallOfDocument == 0)
				FScoreOfDocument = 0.0;
			else
				FScoreOfDocument = 2.0 * precisionOfDocument * recallOfDocument 
				/ (precisionOfDocument + recallOfDocument);

			docPrecisionList.add(precisionOfDocument);
			fout.write("\n");
			fout.write("Precision of document is:\n");
			fout.write( precisionOfDocument + "\n");
			fout.write("Recall of document is:\n");
			fout.write( recallOfDocument + "\n");
			fout.write("F score of document is:\n");
			fout.write( FScoreOfDocument + "\n");
			
			fout.write("\n");
			fout.write("Concepts:\n");

			List<String> conceptResult = goldConcepts.get(queryId);
			Collection<ConceptSearchResult> conceptCollection = TypeUtil.getRankedConceptSearchResults(jcas);
			LinkedList<ConceptSearchResult> conceptList = new LinkedList<ConceptSearchResult>();
			conceptList.addAll(conceptCollection);
			if (!conceptList.isEmpty()){
				for (ConceptSearchResult concept: conceptList){
					//fout.write(concept.getUri() + "\n");
					if(conceptResult.contains(concept.getUri()))
						tpOfConcept++;
				}
			}
			precisionOfConcept = (tpOfConcept * 1.0) / conceptList.size() * 1.0;
			recallOfConcept = (tpOfConcept * 1.0) / conceptResult.size() * 1.0;
			if (precisionOfConcept == 0 && recallOfConcept == 0)
				FScoreOfConcept = 0.0;
			else
				FScoreOfConcept = 2.0 * precisionOfConcept * recallOfConcept 
				/ (precisionOfConcept + recallOfConcept);
	
			fout.write("\n");
			fout.write("Precision of concept is:\n");
			fout.write( precisionOfConcept + "\n");
			fout.write("Recall of concept is:\n");
			fout.write( recallOfConcept + "\n");
			fout.write("F score of concept is:\n");
			fout.write( FScoreOfConcept + "\n");
			
			fout.write("Triples:\n");
			List<Triple> tripleResult = goldTriples.get(queryId);
			System.out.println("***************");
			System.out.println(queryId);
			Collection<TripleSearchResult> tripleCollection = TypeUtil.getRankedTripleSearchResults(jcas);
			LinkedList<TripleSearchResult> tripleList = new LinkedList<TripleSearchResult>();
			tripleList.addAll(tripleCollection);
			
			for (TripleSearchResult result: tripleCollection) {
				if (result != null) {
					edu.cmu.lti.oaqa.type.kb.Triple t = result.getTriple();
					if (t != null /*&& t.getSubject()!= null && t.getObject()!= null && t.getPredicate()!= null*/) {
						fout.write("Subject:" + t.getSubject() + "\n");
						fout.write("Object:" + t.getObject() + "\n");
						fout.write("Predicate:" + t.getPredicate() + "\n");
						for (int i=0;i<tripleResult.size();i++) {
							if (t.getSubject() == tripleResult.get(i).getS()) {
								tpOfTriple++;
							}
						}
					}
				}
			}
			precisionOfTriple = (tpOfTriple * 1.0) / tripleList.size() * 1.0;
			recallOfTriple = (tpOfTriple * 1.0) / tripleResult.size() * 1.0;
			if (precisionOfTriple == 0 && recallOfTriple == 0)
				FScoreOfTriple = 0.0;
			else
				FScoreOfTriple = 2.0 * precisionOfTriple * recallOfTriple 
				/ (precisionOfTriple + recallOfTriple);
			fout.write("\n");
			fout.write("Precision of triple is:\n");
			fout.write( precisionOfTriple + "\n");
			fout.write("Recall of triple is:\n");
			fout.write( recallOfTriple + "\n");
			fout.write("F score of triple is:\n");
			fout.write( FScoreOfTriple + "\n");
			
			
			fout.write("Passages:\n");
			Collection<Passage> passageCollection = TypeUtil.getRankedPassages(jcas);
			for (Passage passage : passageCollection) {
				fout.write(passage.getText() + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
    IOException {
		fout.write("Precision List of Documents:"  + docPrecisionList.toString() + "\n");
		fout.close();
	}

}
