package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.ontology.Description;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.dag.TBoxDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.DAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.GraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.NamedDescriptionDAGImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxGraphImpl;
import it.unibz.krdb.obda.owlrefplatform.core.dagjgrapht.TBoxReasonerImpl;

import java.io.File;

import junit.framework.TestCase;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S_NewGraphTest  extends TestCase{
	
	Logger log = LoggerFactory.getLogger(S_NewGraphTest.class);

	public void testCreation() throws Exception {
		String classowlfile = "src/test/resources/dag-tests-1.owl";
		String roleowlfile = "src/test/resources/test/dag/test-role-hierarchy.owl";
		
		 String owlfile =
		 "src/test/resources/test/stockexchange-unittest.owl";
		log.info("Loading ontology");

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		log.info("Translating");

		OWLAPI3Translator translator = new OWLAPI3Translator();
		Ontology o = translator.translate(ontology);

		log.info("Generating graph");
		TBoxGraphImpl change= new TBoxGraphImpl(o);
		
		log.info("Get the graph");
		GraphImpl graph = (GraphImpl) change.getGraph();
		System.out.println(graph);
		
		log.info("See information");
		System.out.println("vertexes"+ graph.vertexSet());
		log.debug("vertexes {}", graph.vertexSet());
		System.out.println(graph.getClasses());
		System.out.println(graph.getRoles());
		System.out.println(graph.edgeSet());
//		
		log.info("From graph to dag");
		GraphDAGImpl change2 = new GraphDAGImpl(graph);
		
		log.info("Get dag");
		DAGImpl dag=(DAGImpl) change2.getDAG();
		System.out.println(dag);
		System.out.println(dag.getReplacements());
		System.out.println(dag.getMapEquivalences());
		
		log.info("See information");
		System.out.println(dag.vertexSet());
		System.out.println(dag.getClasses());
		System.out.println(dag.getRoles());
		System.out.println(dag.edgeSet());
		
//		log.info("See relations");
//		TBoxReasonerImpl tbox= new TBoxReasonerImpl(dag);
//		for (Description d: dag.vertexSet()){
//		System.out.println("parents "+d+" "+tbox.getDirectParents(d));
//		System.out.println("children "+d+" "+tbox.getDirectChildren(d));
//		
//		log.info("Descendants");
//		System.out.println("descendants "+d+ " "+tbox.getDescendants(d));
//		
//		log.info("Ancestors");
//		System.out.println("ancestors "+d+" "+ tbox.getAncestors(d));
//		}
		log.info("Get named dag");
		NamedDescriptionDAGImpl namedDAGclass=  new NamedDescriptionDAGImpl(dag);
		DAGImpl namedDAG= namedDAGclass.getDAG();
		System.out.println(namedDAG);
		
		log.info("See information named DAG");
		System.out.println(namedDAG.getClasses());
		System.out.println(namedDAG.getRoles());
		System.out.println(namedDAG.edgeSet());
		
//		log.info("See relations named DAG");
//		TBoxReasonerImpl tbox2= new TBoxReasonerImpl(dag);
//		for (Description d2: dag.vertexSet()){
//		System.out.println("parents "+d2+" "+tbox2.getDirectParents(d2));
//		System.out.println("children "+d2+" "+tbox2.getDirectChildren(d2));
//		
//		log.info("Descendants namedDAG");
//		System.out.println("descendants "+d2+" "+tbox2.getDescendants(d2));
//		
//		log.info("Ancestors namedDAG");
//		System.out.println("ancestors "+d2+" "+ tbox2.getAncestors(d2));
//		
//		}
	}
}
