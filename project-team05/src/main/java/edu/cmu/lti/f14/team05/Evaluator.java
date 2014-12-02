package edu.cmu.lti.f14.team05;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import json.JsonCollectionReaderHelper;
import json.gson.Question;
import json.gson.Snippet;
import json.gson.TestQuestion;
import json.gson.Triple;

public class Evaluator {
	List<TestQuestion> goldStandards;
	JsonCollectionReaderHelper jsonHelper;

	Map<String, List<String>> goldDocuments;
	Map<String, List<String>> goldConcepts;
	Map<String, List<Triple>> goldTriples;
	Map<String, List<Snippet>> goldSnippets;
	Map<String, String> goldAnswers;
	FileWriter fout;
	FileWriter report;
	String evalResult = new String();
	public Evaluator(){
		jsonHelper = new JsonCollectionReaderHelper();
		goldStandards = jsonHelper.testRun();
		goldDocuments = new HashMap<String, List<String>>();
		goldConcepts = new HashMap<String, List<String>>();
		goldTriples = new HashMap<String, List<Triple>>();
		goldSnippets = new HashMap<String, List<Snippet>>();
		goldAnswers = new HashMap<String, String>();
		for (TestQuestion question: goldStandards){
			goldDocuments.put(question.getId(), question.getDocuments());
			goldConcepts.put(question.getId(), question.getConcepts());
			goldTriples.put(question.getId(), question.getTriples());
			goldSnippets.put(question.getId(), question.getSnippets());
			goldAnswers.put(question.getId(), question.getIdealAnswer());
		}
	}
	public void outputEvaluate(List<ExactAnswer> testAnswer){
		List<EvaluationResult> conceptsEval = new ArrayList<EvaluationResult>();
	    List<EvaluationResult> documentsEval = new ArrayList<EvaluationResult>();
	    List<EvaluationResult> triplesEval = new ArrayList<EvaluationResult>();
	    List<EvaluationResult> snippetsEval = new ArrayList<EvaluationResult>();
	    List<EvaluationResult> answersEval = new ArrayList<EvaluationResult>();
		for (Question q: testAnswer){		
			evalResult +="Question: "+ q.getBody() + "\n";
			evalResult +="Question Type: "+ q.getType() + "\n";
			boolean exist = false;
			for (Question question : goldStandards) {
				if(question.getId().equals(q.getId())){
					exist = true;
				}
			}
			if(exist){
				conceptsEval.add(evalConcepts(goldConcepts.get(q.getId()),q.getConcepts()));
				documentsEval.add(evalDocuments(goldDocuments.get(q.getId()),q.getDocuments()));
				triplesEval.add(evalTriples(goldTriples.get(q.getId()),q.getTriples()));
				snippetsEval.add(evalSnippets(goldSnippets.get(q.getId()),q.getSnippets()));
				//answer evaluate
			}
		}
		try {
			fout = new FileWriter("Evaluation Result.txt");
			fout.write(evalResult);
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public EvaluationResult evalDocuments (List<String> gold, List<String> test){
		evalResult += "Documents:\n";
		double precision = solvePrecision(gold,test);
		double recall = solveRecall(gold, test);
	    double fmeasure = solveFMeasure(precision, recall);
	    double ap = solveAP(gold, test);
	    evalResult += "Precision of document is:\n";
	    evalResult += precision + "\n";
	    evalResult += "Recall of document is:\n";
	    evalResult += recall + "\n";
	    evalResult += "F score of document is:\n";
	    evalResult += fmeasure + "\n";
	    evalResult += "Average precision of document is:\n";
	    evalResult += ap + "\n";
		return new EvaluationResult(precision,recall,fmeasure,ap);
	}
	public EvaluationResult evalConcepts (List<String> gold, List<String> test){
		evalResult += "Concepts:\n";
		double precision = solvePrecision(gold,test);
		double recall = solveRecall(gold, test);
	    double fmeasure = solveFMeasure(precision, recall);
	    double ap = solveAP(gold, test);
	    evalResult += "Precision of concept is:\n";
	    evalResult += precision + "\n";
	    evalResult += "Recall of concept is:\n";
	    evalResult += recall + "\n";
	    evalResult += "F score of concept is:\n";
	    evalResult += fmeasure + "\n";
	    evalResult += "Average precision of concept is:\n";
	    evalResult += ap + "\n";
		return new EvaluationResult(precision,recall,fmeasure,ap);
	}
	public EvaluationResult evalTriples (List<Triple> gold, List<Triple> test){
		evalResult += "Triples:\n";
		double precision = solvePrecision(gold,test);
		double recall = solveRecall(gold, test);
	    double fmeasure = solveFMeasure(precision, recall);
	    double ap = solveAP(gold, test);
	    evalResult += "Precision of triple is:\n";
	    evalResult += precision + "\n";
	    evalResult += "Recall of triple is:\n";
	    evalResult += recall + "\n";
	    evalResult += "F score of triple is:\n";
	    evalResult += fmeasure + "\n";
	    evalResult += "Average precision of triple is:\n";
	    evalResult += ap + "\n";
		return new EvaluationResult(precision,recall,fmeasure,ap);
	}
	public EvaluationResult evalSnippets (List<Snippet> gold, List<Snippet> test){
		evalResult += "Snippet:\n";
		double precision = solveSnippetPrecision(gold,test);
		double recall = solveSnippetRecall(gold, test);
	    double fmeasure = solveFMeasure(precision, recall);
	    double ap = solveSnippetAP(gold, test);
	    evalResult += "Precision of snippet is:\n";
	    evalResult += precision + "\n";
	    evalResult += "Recall of snippet is:\n";
	    evalResult += recall + "\n";
	    evalResult += "F score of snippet is:\n";
	    evalResult += fmeasure + "\n";
	    evalResult += "Average precision of snippet is:\n";
	    evalResult += ap + "\n";
		return new EvaluationResult(precision,recall,fmeasure,ap);
	}
	private <T> List<T> emptyListIfNull(List<T> list) {
	    if (list == null)
	      return new ArrayList<T>();
	    return list;
	  }
	private <T> double solvePrecision(List<T> gold, List<T> test){
		gold = emptyListIfNull(gold);
		test = emptyListIfNull(test);
	    Set<T> goldSet = new HashSet<T>(gold);
	    Set<T> testSet = new HashSet<T>(test);
	    testSet.retainAll(goldSet);
	    Integer truePositive = testSet.size();
	    if (test.size() == 0) {
	      return 0;
	    }
	    return ((double) truePositive) / ((double) test.size()); 
	}
	private <T> double solveRecall(List<T> gold, List<T> test){
		gold = emptyListIfNull(gold);
		test = emptyListIfNull(test);
	    Set<T> goldSet = new HashSet<T>(gold);
	    Set<T> testSet = new HashSet<T>(test);
	    testSet.retainAll(goldSet);
	    Integer truePositive = testSet.size();
	    if (test.size() == 0) {
	      return 0;
	    }
	    return ((double) truePositive) / ((double) goldSet.size()); 
	}
	private double solveFMeasure(Double p, Double r) {
	    if (p + r == 0) {
	      return 0;
	    }
	    return (2 * p * r) / (p + r);
	}
	private <T> double solveAP (List<T> gold, List<T> test){
		gold = emptyListIfNull(gold);
		test = emptyListIfNull(test);
	    int count = 0;
	    double ap = 0.0;
	    int c = 0;
	    for (T item : test) {
	      if (gold.contains(item)) {
	    	  count += 1;
	        ap += (count / ((double) (c + 1)));
	      }
	      c = c + 1;
	    }
	    return ap / (count + Math.pow(10, -10));
	}
	private double solveSnippetPrecision (List<Snippet> gold, List<Snippet> test){
		gold = emptyListIfNull(gold);
	    test = emptyListIfNull(test);
	    if (test.size() == 0)
	      return 0;
	    int overlap = solveSnippetOverlap(gold, test);   
	    int testLength = 0;
	    for (Snippet snip : test)
	    	testLength += ( snip.getOffsetInEndSection() - snip.getOffsetInBeginSection() );
	    return ((double) overlap) / testLength;
	}
	private double solveSnippetRecall (List<Snippet> gold, List<Snippet> test){
		gold = emptyListIfNull(gold);
	    test = emptyListIfNull(test);
	    if (test.size() == 0)
	      return 0;
	    int overlap = solveSnippetOverlap(gold, test);   
	    int goldLength = 0;
	    for (Snippet snip : gold)
	    	goldLength += ( snip.getOffsetInEndSection() - snip.getOffsetInBeginSection() );
	    return ((double) overlap) / goldLength;
	}
	private int solveSnippetOverlap (List<Snippet> gold, List<Snippet> test){
		int overlap = 0;
		for (Snippet goldSnip : gold) {
		  List<Snippet> matches = new ArrayList<Snippet>();
		  for (Snippet s : test) {
		    if (s.getDocument().equals(goldSnip.getDocument()) && s.getBeginSection().equals(goldSnip.getBeginSection()))
		      matches.add(s);
		  }
		  for (Snippet testSnip : matches) {
		    int overlapBegin = Math.max(goldSnip.getOffsetInBeginSection(), testSnip.getOffsetInBeginSection());
		    int overlapEnd = Math.min(goldSnip.getOffsetInEndSection(), testSnip.getOffsetInEndSection());
		    if (overlapBegin < overlapEnd) {
		    	overlap += (overlapEnd - overlapBegin);
		    }
		  }
		}
		return overlap;
	}
	private double solveSnippetAP(List<Snippet> gold, List<Snippet> test){
		int count = 0;
	    double ap = 0.0;
	    int totalOverlap = 0;
	    int totalTest = 0;
	    for (Snippet testSnip : test) {
	      List<Snippet> matches = new ArrayList<Snippet>();
	      for (Snippet s : gold) {
	        if (s.getDocument().equals(testSnip.getDocument()) && s.getBeginSection().equals(testSnip.getBeginSection()))
	          matches.add(s);
	      }
	      totalTest += testSnip.getOffsetInEndSection() - testSnip.getOffsetInBeginSection();
	      for (Snippet goldSnip : matches) {
	        int overlapBegin = Math.max(goldSnip.getOffsetInBeginSection(), testSnip.getOffsetInBeginSection());
	        int overlapEnd = Math.min(goldSnip.getOffsetInEndSection(), testSnip.getOffsetInEndSection());
	        if (overlapBegin < overlapEnd) {
	          totalOverlap += (overlapEnd - overlapBegin);
	          count += 1;
	          ap += (totalOverlap / ((double) (totalTest)));
	        }
	      }
	    }
	    return ap / (count + Math.pow(10, -10));
	}
	
}
