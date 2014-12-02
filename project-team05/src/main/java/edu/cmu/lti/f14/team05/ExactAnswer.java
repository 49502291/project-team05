package edu.cmu.lti.f14.team05;

import java.util.List;

import com.google.gson.Gson;

import json.gson.QuestionType;
import json.gson.Snippet;
import json.gson.TestListQuestion;
import json.gson.Triple;

public class ExactAnswer extends TestListQuestion{
	
	public ExactAnswer(String id, String body, List<String> documents, List<String> concepts,
			List<Triple> triples){
	  super(id, body, null, documents, null, concepts, triples, null, null);
	}
	public ExactAnswer(String id, String body, List<String> documents, List<String> concepts,
			List<Triple> triples, List<Snippet> snippets){
		
	  super(id, body, null, documents, snippets, concepts, triples, null, null);
	 
	}
//	public ExactAnswer(String id, String body, List<String> documents, List<Snippet> snippets, 
//			List<String> concepts, List<Triple> triples,  List<String> exactAnswer) {
//	  super(id, body, null, documents, snippets, concepts, triples);
//	  this.exactAnswer = exactAnswer;
//	}
	public ExactAnswer(String id, String body, QuestionType type, List<String> documents,
	        List<Snippet> snippets, List<String> concepts, List<Triple> triples, List<List<String>> exactAnswer) {
	  super(id, body, type, documents, snippets, concepts, triples, null, exactAnswer);
	}

}
