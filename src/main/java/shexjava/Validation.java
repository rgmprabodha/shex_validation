package shexjava;


import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.rdf4j.RDF4J;
import org.apache.jena.util.FileManager;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import fr.inria.lille.shexjava.GlobalFactory;
import fr.inria.lille.shexjava.schema.Label;
import fr.inria.lille.shexjava.schema.ShexSchema;
import fr.inria.lille.shexjava.schema.parsing.GenParser;
import fr.inria.lille.shexjava.validation.RefineValidation;
import fr.inria.lille.shexjava.validation.ValidationAlgorithm;

public class Validation {

	public static void main(String[] args) throws Exception {

		Path path = Paths.get(".").toAbsolutePath().normalize();

		String dataf = path.toFile().getAbsolutePath() + "/src/main/resources/temperature.ttl";
		Path shape = Paths.get("./src/main/resources/shape.json").toAbsolutePath().normalize();

		RDF4J factory = new RDF4J();
		GlobalFactory.RDFFactory = factory; // set the global factory used in shexjava

		// load and create the shex schema
		ShexSchema schema = GenParser.parseSchema(shape);
		
//		System.out.println("Reading schema");
//		for (Label label : schema.getRules().keySet())
//			System.out.println(label + ": " + schema.getRules().get(label));

		// load the model
		String baseIRI = "http://a.example.shex/";
		Model data = Rio.parse(FileManager.get().open(dataf), baseIRI, RDFFormat.TURTLE);
//		System.out.println("Reading data");
//		Iterator<Statement> ite = data.iterator();
//		while (ite.hasNext())
//			System.out.println(ite.next());

		// create the graph
		Graph dataGraph = factory.asGraph(data);

		// focus node and shape label to be validated
		IRI focusNode = factory.createIRI("http://example.com/ns#Sensor1"); 
		Label shapeLabel = new Label(factory.createIRI("http://example.com/ns#TemperatureShape")); 

		System.out.println();
		System.out.println("Refine validation:");
		// create the validation algorithm
		ValidationAlgorithm validation = new RefineValidation(schema, dataGraph);

		// validate
		validation.validate(focusNode, shapeLabel);
		System.out.println(validation.getTyping().getStatusMap());

		String reportFile = path.toFile().getAbsolutePath() + "/src/main/resources/temperatureReport.ttl";

		FileWriter myWriter = new FileWriter(reportFile);
		myWriter.write(validation.getTyping().getStatusMap().toString());
		myWriter.close();

		System.out.println("Done");

		// check the result
		boolean result = validation.getTyping().isConformant(focusNode, shapeLabel);
		System.out.println("Does " + focusNode + " has shape " + shapeLabel + "? " + result);


	}
}
