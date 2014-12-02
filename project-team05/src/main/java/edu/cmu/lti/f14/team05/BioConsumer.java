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
import json.gson.QuestionType;
import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.Triple;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;

import util.TypeUtil;
import edu.cmu.lti.oaqa.type.answer.Answer;
import edu.cmu.lti.oaqa.type.input.Question;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
//import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;

public class BioConsumer extends CasConsumer_ImplBase {
	FileWriter fout;
	List<ExactAnswer> exactAnswer;
	Evaluator evaluator = new Evaluator();
	
	@Override
	public void initialize() throws ResourceInitializationException{
		exactAnswer = new ArrayList<ExactAnswer>();
		try {
			fout = new FileWriter("ListAnswer.json");
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
		Question currentQuestion = TypeUtil.getQuestion(jcas);
		String queryId = TypeUtil.getQuestion(jcas).getId();
		String queryText = TypeUtil.getQuestion(jcas).getText();
		Collection<Document> docCollection = TypeUtil.getRankedDocuments(jcas);
		ArrayList<Document> documentList = new ArrayList<Document>();
		documentList.addAll(docCollection);
		ArrayList<String> documents = new ArrayList<String>();
		if (!documentList.isEmpty()){
			for (Document doc: documentList){
				documents.add(doc.getUri());

			}
		}			
		Collection<ConceptSearchResult> conceptCollection = TypeUtil.getRankedConceptSearchResults(jcas);
		LinkedList<ConceptSearchResult> conceptList = new LinkedList<ConceptSearchResult>();
		conceptList.addAll(conceptCollection);
		ArrayList<String> concepts = new ArrayList<String>();
		if (!conceptList.isEmpty()){
			for (ConceptSearchResult concept: conceptList){
				concepts.add(concept.getUri());

			}
		}	
		Collection<TripleSearchResult> tripleCollection = TypeUtil.getRankedTripleSearchResults(jcas);
		ArrayList<TripleSearchResult> tripleList = new ArrayList<TripleSearchResult>();
		tripleList.addAll(tripleCollection);
		ArrayList<Triple> triples = new ArrayList<Triple>();
		if(!tripleList.isEmpty()){
			for (TripleSearchResult result: tripleCollection) {
				if (result != null) {
					edu.cmu.lti.oaqa.type.kb.Triple t = result.getTriple();
					if (t != null ) {
						triples.add(new Triple(t.getSubject(),t.getPredicate(),t.getObject()));
					}
				}
			}
		}
		Collection<Passage> snippetCollection = TypeUtil.getRankedPassages(jcas);
		ArrayList<Passage> snippetList = new ArrayList<Passage>();
		snippetList.addAll(snippetCollection);
		ArrayList<Snippet> snippets = new ArrayList<Snippet>();
		if(!snippetList.isEmpty()){
			for (Passage snip : snippetList) {
				Snippet tempSnippet = new Snippet(snip.getUri(),snip.getText(),snip.getOffsetInBeginSection(),
						snip.getOffsetInEndSection(),snip.getBeginSection(),snip.getEndSection());
				snippets.add(tempSnippet);
				//System.out.println("Test:" + gson.toJson(tempSnippet));
			}			
		}
		Collection<Answer> answerCollection = TypeUtil.getAnswers(jcas);
		ArrayList<Answer> answerList = new ArrayList<Answer>();
		answerList.addAll(answerCollection);
		List<List<String>> answers = new ArrayList<List<String>>();
		if(!answerList.isEmpty()){
			for(Answer ans : answerList){
				List<String> line = new ArrayList<String>();
				line.add(ans.getText());
				answers.add(line);
			}
		}
		// type = new QuestionType();
		ExactAnswer tempAns = new ExactAnswer(queryId,queryText,QuestionType.list,documents,
				snippets,concepts,triples,answers);
		exactAnswer.add(tempAns);
//		 Gson gson = new Gson();
//			System.out.println(gson.toJson(exactAnswer.get(exactAnswer.size()-1)));
	}
	
	@Override
	public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
    IOException {
		super.collectionProcessComplete(arg0);
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		String Output = gson.toJson(exactAnswer);
		fout.write("{\" questions\":  " + Output + "\n");
		fout.close();
		evaluator.outputEvaluate(exactAnswer);
	}

}
