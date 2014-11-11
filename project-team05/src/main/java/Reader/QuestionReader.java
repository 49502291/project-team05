package Reader;

import java.io.IOException;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.jcas.JCas;

import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.type.input.*;
import json.gson.Question;
import json.gson.QuestionType;
//import json.gson.Question;
//import json.gson.QuestionType;
import json.gson.TestQuestion;
import json.gson.TestSet;
import json.gson.TestSummaryQuestion;
import json.gson.TrainingFactoidQuestion;
import json.gson.TrainingListQuestion;
import json.gson.TrainingQuestion;
import json.gson.TrainingYesNoQuestion;
import edu.cmu.lti.oaqa.type.answer.Answer;
import static java.util.stream.Collectors.toList;
import edu.cmu.lti.oaqa.type.retrieval.ConceptSearchResult;
import edu.cmu.lti.oaqa.type.retrieval.Document;
import edu.cmu.lti.oaqa.type.retrieval.Passage;
import edu.cmu.lti.oaqa.type.retrieval.TripleSearchResult;
import util.TypeFactory;


public class QuestionReader extends CollectionReader_ImplBase {

	private int index;
	private List<TestQuestion> inputs;
	private String filePath;
	
	public void initialize() throws ResourceInitializationException {
		
		filePath = "/questions.json";
		index =0;
		inputs = Lists.newArrayList();
		testRun(filePath);
		
	}
	
	
	@Override
	public void getNext(CAS aCAS) throws IOException, CollectionException {
		// TODO Auto-generated method stub
		JCas jcas;
	    try {
	      jcas = aCAS.getJCas();
	    } catch (CASException e) {
	      throw new CollectionException(e);
	    }
	    
//	    TestQuestion question = inputs.get(index);
//	    Question annotation = new Question (jcas);
//	    annotation.setId(question.getId());
//	    annotation.setText(question.getBody());
//	    
//	    switch (question.getType()){
//	    
//	    case factoid:  annotation.setQuestionType("FACTOID"); break;
//	    case list: annotation.setQuestionType("LIST"); break;
//	    case yesno: annotation.setQuestionType("YES_NO"); break;
//	    case summary: annotation.setQuestionType("SUMMARY"); break;
//	
//	    }
//	    	    
//	    annotation.addToIndexes();
	    
	    
	    addQuestionToIndex(inputs.get(index), "", jcas);
	}

	@Override
	public boolean hasNext() throws IOException, CollectionException {
		// TODO Auto-generated method stub
			return index < inputs.size();
	}

	@Override
	public Progress[] getProgress() {
		// TODO Auto-generated method stub
	return new Progress[] { new ProgressImpl(index, inputs.size(), Progress.ENTITIES) };

	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
         
	}
	

	public void testRun(String filePath) {

	/*	InputStream stream = getClass().getResourceAsStream(filePath);
		try {
			System.out.println("stream " + stream.read());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Object value = filePath;
		if (String.class.isAssignableFrom(value.getClass())) {
			inputs = TestSet
					.load(getClass().getResourceAsStream(
							String.class.cast(value))).stream()
					.collect(toList());
		} else if (String[].class.isAssignableFrom(value.getClass())) {
			inputs = Arrays
					.stream(String[].class.cast(value))
					.flatMap(
							path -> TestSet.load(
									getClass().getResourceAsStream(path))
									.stream()).collect(toList());
		}
		// trim question texts
		
		inputs.stream()
				.filter(input -> input.getBody() != null)
				.forEach(
						input -> input.setBody(input.getBody().trim()
								.replaceAll("\\s+", " ")));
	//	System.out.println("concepts");
	//	System.out.println(inputs.get(0).getBody());
		
	}
	
	public static void addQuestionToIndex(Question input, String source,
			JCas jcas) {
		// question text and type are required
		TypeFactory.createQuestion(jcas, input.getId(), source,
				convertQuestionType(input.getType()), input.getBody())
				.addToIndexes();
		// if documents, snippets, concepts, and triples are found in the input,
		// then add them to CAS
		if (input.getDocuments() != null) {
			input.getDocuments().stream()
					.map(uri -> TypeFactory.createDocument(jcas, uri))
					.forEach(Document::addToIndexes);
		}
		if (input.getSnippets() != null) {
			input.getSnippets()
					.stream()
					.map(snippet -> TypeFactory.createPassage(jcas,
							snippet.getDocument(), snippet.getText(),
							snippet.getOffsetInBeginSection(),
							snippet.getOffsetInEndSection(),
							snippet.getBeginSection(), snippet.getEndSection()))
					.forEach(Passage::addToIndexes);
		}
		if (input.getConcepts() != null) {
			input.getConcepts()
					.stream()
					.map(concept -> TypeFactory.createConceptSearchResult(jcas,
							TypeFactory.createConcept(jcas, concept), concept))
					.forEach(ConceptSearchResult::addToIndexes);
		}
		if (input.getTriples() != null) {
			input.getTriples()
					.stream()
					.map(triple -> TypeFactory.createTripleSearchResult(jcas,
							TypeFactory.createTriple(jcas, triple.getS(),
									triple.getP(), triple.getO())))
					.forEach(TripleSearchResult::addToIndexes);
		}
		// add answers to CAS index
		if (input instanceof TestQuestion) {
			// test question should not have ideal or exact answers
		} else if (input instanceof TrainingQuestion) {
			List<String> summaryVariants = ((TrainingQuestion) input)
					.getIdealAnswer();
			if (summaryVariants != null) {
				TypeFactory.createSummary(jcas, summaryVariants).addToIndexes();
			}
			if (input instanceof TrainingFactoidQuestion) {
				List<String> answerVariants = ((TrainingFactoidQuestion) input)
						.getExactAnswer();
				if (answerVariants != null) {
					TypeFactory.createAnswer(jcas, answerVariants)
							.addToIndexes();
				}
			} else if (input instanceof TrainingListQuestion) {
				List<List<String>> answerVariantsList = ((TrainingListQuestion) input)
						.getExactAnswer();
				if (answerVariantsList != null) {
					answerVariantsList
							.stream()
							.map(answerVariants -> TypeFactory.createAnswer(
									jcas, answerVariants))
							.forEach(Answer::addToIndexes);
				}
			} else if (input instanceof TrainingYesNoQuestion) {
				String answer = ((TrainingYesNoQuestion) input)
						.getExactAnswer();
				if (answer != null) {
					TypeFactory.createAnswer(jcas, answer).addToIndexes();
				}
			} else if (input instanceof TestSummaryQuestion) {
				// summary questions do not have exact answers
			}
		}
	}

	public static String convertQuestionType(QuestionType type) {
		switch (type) {
		case factoid:
			return "FACTOID";
		case list:
			return "LIST";
		case summary:
			return "OPINION";
		case yesno:
			return "YES_NO";
		default:
			return "UNCLASSIFIED";
		}
	}

}
