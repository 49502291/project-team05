package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.http.client.ClientProtocolException;

import edu.cmu.lti.oaqa.bio.bioasq.services.GoPubMedService;
import edu.cmu.lti.oaqa.bio.bioasq.services.LinkedLifeDataServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.OntologyServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse;
import edu.cmu.lti.oaqa.bio.bioasq.services.PubMedSearchServiceResponse.Document;

public class GoPubMedServiceExample {

  public static void main(String[] args) throws ClientProtocolException, IOException,
          ConfigurationException {
    // String text = "Is Rheumatoid Arthritis more common in men or women?";
	File f = new File("OutputTest.txt");
	try{
		if (!f.exists())
			f.delete();
		else
			f.createNewFile();
	} catch (IOException e){
		e.printStackTrace();
	}
	FileWriter writer = new FileWriter(f, true); 
    String text = "Are there any DNMT3 proteins present in plants";
    GoPubMedService service = new GoPubMedService("./project.properties");

    OntologyServiceResponse.Result diseaseOntologyResult = service
            .findDiseaseOntologyEntitiesPaged(text, 0);
    writer.write("Disease ontology: " + diseaseOntologyResult.getFindings().size());
    System.out.println("Disease ontology: " + diseaseOntologyResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : diseaseOntologyResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
      writer.write(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result geneOntologyResult = service.findGeneOntologyEntitiesPaged(text,
            0, 10);
    System.out.println("Gene ontology: " + geneOntologyResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : geneOntologyResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result jochemResult = service.findJochemEntitiesPaged(text, 0);
    System.out.println("Jochem: " + jochemResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : jochemResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result meshResult = service.findMeshEntitiesPaged(text, 0);
    System.out.println("MeSH: " + meshResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : meshResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    OntologyServiceResponse.Result uniprotResult = service.findUniprotEntitiesPaged(text, 0);
    System.out.println("UniProt: " + uniprotResult.getFindings().size());
    for (OntologyServiceResponse.Finding finding : uniprotResult.getFindings()) {
      System.out.println(" > " + finding.getConcept().getLabel() + " "
              + finding.getConcept().getUri());
    }
    LinkedLifeDataServiceResponse.Result linkedLifeDataResult = service
            .findLinkedLifeDataEntitiesPaged(text, 0);
    writer.write("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
    System.out.println("LinkedLifeData: " + linkedLifeDataResult.getEntities().size());
    for (LinkedLifeDataServiceResponse.Entity entity : linkedLifeDataResult.getEntities()) {
      System.out.println(" > " + entity.getEntity());
      for (LinkedLifeDataServiceResponse.Relation relation : entity.getRelations()) {
        System.out.println("   - labels: " + relation.getLabels());
        System.out.println("   - pred: " + relation.getPred());
        System.out.println("   - sub: " + relation.getSubj());
        System.out.println("   - obj: " + relation.getObj());
        writer.write("   - labels: " + relation.getLabels());
        writer.write("   - pred: " + relation.getPred());
        writer.write("   - sub: " + relation.getSubj());
        writer.write("   - obj: " + relation.getObj());
      }
    }
    PubMedSearchServiceResponse.Result pubmedResult = service.findPubMedCitations(text, 0);
    //System.out.println(pubmedResult.getSize());
    System.out.println("documents:" + pubmedResult.getSize());
    writer.write("documents:" + pubmedResult.getSize());
    for (Document doc : pubmedResult.getDocuments()) {
      System.out.println("http://www.ncbi.nlm.nih.gov/pubmed/" + doc.getPmid());
    }
    writer.close();
  }
}
