/**
 * 
 */
package it.unibz.krdb.sql;

import static org.junit.Assert.*;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.sql.UserConstraints;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.*;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dagc
 *
 */
public class TestQuestUserConstraints {

	Quest quest;
	static String owlfile = "src/test/resources/userconstraints/uc.owl";
	static String obdafile = "src/test/resources/userconstraints/uc.obda";
	static String uc_file = "src/test/resources/userconstraints/keys.lst";

	private OBDADataFactory fac;
	private QuestOWLConnection conn;
	private QuestOWLFactory factory;
	
	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	private QuestOWL reasoner;
	private Connection sqlConnection;


	@Before
	public void setUp() throws Exception {
		try {
			sqlConnection= DriverManager.getConnection("jdbc:h2:mem:countries","sa", "");
			java.sql.Statement s = sqlConnection.createStatement();

			try {
				String text = new Scanner( new File("src/test/resources/userconstraints/create.sql") ).useDelimiter("\\A").next();
				s.execute(text);
				//Server.startWebServer(sqlConnection);

			} catch(SQLException sqle) {
				System.out.println("Exception in creating db from script");
			}

			s.close();


			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

			// Loading the OBDA data
			fac = OBDADataFactoryImpl.getInstance();
			obdaModel = fac.getOBDAModel();

			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdafile);

			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

			
			// Creating a new instance of the reasoner
			this.factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

			
		} catch (Exception exc) {
			try {
				tearDown();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}	

	}


	@After
	public void tearDown() throws Exception{
		conn.close();
		reasoner.dispose();
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				System.out.println("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}

	@Test
	public void testNoSelfJoinElim() throws Exception {
		
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1(.*),(.*)TABLE1(.*)");
		assertTrue(m);
		
		
	}

	@Test
	public void testForeignKeysNoSelfJoinElim() throws Exception {
		
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)TABLE2(.*),(.*)TABLE2(.*)");
		assertTrue(m);
		
		
	}
	
	@Test
	public void testWithSelfJoinElim() throws Exception {
		// Parsing user constraints
		UserConstraints userConstraints = new UserConstraints(uc_file);
		factory.setUserConstraints(userConstraints);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal1 ?v1; :hasVal2 ?v2.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)TABLE1(.*),(.*)TABLE1(.*)");
		assertFalse(m);
		
		
	}
	
	@Test
	public void testForeignKeysWithSelfJoinElim() throws Exception {
		// Parsing user constraints
		UserConstraints userConstraints = new UserConstraints(uc_file);
		factory.setUserConstraints(userConstraints);
		this.reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());


		// Now we are ready for querying
		this.conn = reasoner.getConnection();
		String query = "PREFIX : <http://www.semanticweb.org/ontologies/2013/7/untitled-ontology-150#> SELECT * WHERE {?x :hasVal3 ?v1; :hasVal4 ?v4.}";
		QuestOWLStatement st = conn.createStatement();
		
		
		String sql = st.getUnfolding(query);
		boolean m = sql.matches("(?ms)(.*)TABLE2(.*),(.*)TABLE2(.*)");
		assertTrue(m);
		
		
	}
	


}