package edu.cmu.lti.f14.team05;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.lti.oaqa.type.kb.Concept;
import edu.cmu.lti.oaqa.type.kb.Triple;
import edu.cmu.lti.oaqa.type.retrieval.Document;

public class BioConsumer extends CasConsumer_ImplBase {
	@Override
	public void initialize() throws ResourceInitializationException{
		
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
		FileWriter fout;
		try {
			fout = new FileWriter("report.txt", true);
			fout.write("Documents");
			FSIterator it = jcas.getAnnotationIndex(Document.type)
					.iterator();
			while (it.hasNext()) {
				Document doc = (Document) it.next();
				fout.write(doc.getTitle());
				fout.write(doc.getText());
			}
			fout.write("Concepts");
			it = jcas.getAnnotationIndex(Concept.type)
					.iterator();
			while (it.hasNext()) {
				Concept concept = (Concept) it.next();
				fout.write(concept.getName());
				fout.write(concept.getMentions().toString());
			}
			
			it = jcas.getAnnotationIndex(Triple.type)
					.iterator();
			System.out.println("Triples");
			while (it.hasNext()) {
				Triple triple = (Triple) it.next();
				fout.write(triple.getObject());
				fout.write(triple.getPredicate());
				fout.write(triple.getSubject());
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
