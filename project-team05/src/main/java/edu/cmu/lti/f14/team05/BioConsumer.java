package edu.cmu.lti.f14.team05;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class BioConsumer extends CasConsumer_ImplBase {

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
		FSIterator it = jcas.getAnnotationIndex(Document.type)
				.iterator();
		while (it.hasNext()) {
			Document doc = (Document) it.next();
			System.out.println(doc.getTitle());
			System.out.println(doc.getText());
			
		}
	}

}
