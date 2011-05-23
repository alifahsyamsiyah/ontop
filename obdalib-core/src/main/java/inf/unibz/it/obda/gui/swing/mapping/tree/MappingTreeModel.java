/***
 * Copyright (c) 2008, Mariano Rodriguez-Muro.
 * All rights reserved.
 *
 * The OBDA-API is licensed under the terms of the Lesser General Public
 * License v.3 (see OBDAAPI_LICENSE.txt for details). The components of this
 * work include:
 *
 * a) The OBDA-API developed by the author and licensed under the LGPL; and,
 * b) third-party components licensed under terms that may be different from
 *   those of the LGPL.  Information about such licenses can be found in the
 *   file named OBDAAPI_3DPARTY-LICENSES.txt.
 */
package inf.unibz.it.obda.gui.swing.mapping.tree;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.api.controller.MappingControllerListener;
import inf.unibz.it.obda.domain.DataSource;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.domain.TargetQuery;
import inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel;
import inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSOBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.utils.codec.SourceQueryToTextCodec;
import inf.unibz.it.utils.codec.TargetQeryToTextCodec;

import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.antlr.runtime.RecognitionException;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.tools.parser.DatalogProgramParser;
import org.obda.query.tools.parser.DatalogQueryHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Make the three model be based on the actual mapping collection
public class MappingTreeModel extends DefaultTreeModel implements
		MappingControllerListener, FilteredTreeModel {

	private static final long serialVersionUID = -1151057670798439917L;

	private MappingController controller = null;
	private DefaultMutableTreeNode root = null;
//	private DatasourcesController dsc = null; TODO Remove this ds?
	private URI currentDataSourceUri;
	private final List<TreeModelFilter<OBDAMappingAxiom>> ListFilters = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();
	protected APIController apic = null;

	DatalogProgramParser datalogParser = new DatalogProgramParser();

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public MappingTreeModel(APIController apic,	MappingController controller) {
		super(new DefaultMutableTreeNode("Mappings"));
		this.apic = apic;
		root = (DefaultMutableTreeNode) getRoot();
		this.controller = controller;
	}
	
	// TODO Remove this ds?
//	 public MappingTreeModel(APIController apic, DatasourcesController dsc,
//	      MappingController controller) {
//	    super(new DefaultMutableTreeNode("Mappings"));
//	    this.apic = apic;
//	    this.dsc = dsc;
//	    root = (DefaultMutableTreeNode) getRoot();
//	    this.controller = controller;
//	  }

	/***************************************************************************
	 * Invoked when the model changes. It should be invoked only when it is
	 * changed trough a process not related to the mappings or data sources,
	 * like a node editor in the GUI. It calls the update methods of the mapping
	 * controller.
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		DefaultMutableTreeNode oldmappingnode = (DefaultMutableTreeNode) path
				.getLastPathComponent();
		DefaultMutableTreeNode parentnode = (DefaultMutableTreeNode) oldmappingnode
				.getParent();
		Object oldmappingvalue = oldmappingnode.getUserObject();
		super.valueForPathChanged(path, newValue);

		// TODO Remove this ds?
//		URI sourceName = dsc.getCurrentDataSource().getSourceID();
		URI sourceName = currentDataSourceUri;
		
		if (oldmappingnode instanceof MappingNode) {
			String query = (String) newValue;
			controller.updateMapping(sourceName, (String) oldmappingvalue, query);
		} else if (oldmappingnode instanceof MappingHeadNode) {
			String query = (String) newValue;
			CQIE newquery = parse(query);
			controller.updateTargetQueryMapping(sourceName, (String) parentnode
					.getUserObject(), newquery);
		} else if (oldmappingnode instanceof MappingBodyNode) {
			RDBMSSQLQuery query = new RDBMSSQLQuery((String) newValue);
			controller.updateSourceQueryMapping(sourceName, (String) parentnode
					.getUserObject(), query);
		}
	}

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is added. Updates
	 * the model to include the corresponding node. Only if the current source
	 * is srcuri.
	 */
	public void mappingDeleted(URI srcuri, String mappingId) {
	  // TODO Remove this ds?
//		if (dsc.getCurrentDataSource()== null || !srcuri.equals(dsc.getCurrentDataSource().getSourceID())) {
//			return;
//		}
		Enumeration<MappingNode> mappingNodes = root.children();
		MappingNode affectedNode = null;
		while (mappingNodes.hasMoreElements()) {
		  MappingNode currentNode = mappingNodes.nextElement();
		  String currentNodeId = currentNode.getUserObject().toString();
		  if (currentNodeId.equals(mappingId)) {
		    affectedNode = currentNode;
		    break;
		  }
		}
		removeNodeFromParent(affectedNode);
		nodeStructureChanged(root);
	}

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is added. Updates
	 * the model to include the corresponding node. Only if the current source
	 * is srcuri.
	 */
	public void mappingInserted(URI srcuri, String mapping_id) {
		try {
		  // TODO remove this ds?
//  		DataSource currentsource = dsc.getCurrentDataSource();
//  		if ((currentsource == null)|| !srcuri.equals(currentsource.getSourceID())) {
//  			return;
//  		}
//  		URI src_uri = dsc.getCurrentDataSource().getSourceID();
  		RDBMSOBDAMappingAxiom mapping = (RDBMSOBDAMappingAxiom) controller.getMapping(srcuri, mapping_id);
  		MappingNode mappingNode = getMappingNodeFromMapping(mapping);

			insertNodeInto(mappingNode, root, root
					.getChildCount());
			nodeStructureChanged(root);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/***************************************************************************
	 * Called from the mapping controller when anew mapping is updated
	 * programatically. Updates the model to include the changes for
	 * corresponding node. Only if the current source is srcuri.
	 */
	public void mappingUpdated(URI srcuri, String mapping_id, OBDAMappingAxiom mapping) {
		// SYNCWITH EVERYBODY EXCEPT WITH THE CONTROLLER SINCE IT WAS THE SOURCE
		// OF THIS EVENT
//		if (dsc.getCurrentDataSource() == null || !srcuri.equals(dsc.getCurrentDataSource().getSourceID())) {
//			return;
//		}
		try {
			MappingNode mappingnode = getMappingNode(mapping_id);
			if (mappingnode == null) {
				return;
			}
			MappingBodyNode body = mappingnode.getBodyNode();
			MappingHeadNode head = mappingnode.getHeadNode();

			Query srcq = mapping.getSourceQuery();
			Query trgq = mapping.getTargetQuery();
			TargetQeryToTextCodec tttc = new TargetQeryToTextCodec(apic);
			SourceQueryToTextCodec sttc = new SourceQueryToTextCodec(apic);
			String newbodyquery =sttc.encode(srcq);
			String newheadquery = tttc.encode(trgq);

			body.setQuery(newbodyquery);
			head.setQuery(newheadquery);
			mappingnode.setMappingID(mapping.getId());
			nodeStructureChanged(mappingnode);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/***************************************************************************
	 * Invoked by the mapping controller to notify the change of the current
	 * Datasource. The model dumps all mapping nodes and adds the nodes for the
	 * mappings of the current source.
	 */
	public void currentSourceChanged(URI oldsrcuri, URI newsrcuri) {
		// SYNCWITH EVERYBODY EXCEPT WITH THE CONTROLLER SINCE IT WAS THE SOURCE
		// OF THIS EVENT
	  this.currentDataSourceUri = newsrcuri;
	  
		try {
			if (newsrcuri != null) {
				root.setUserObject("Mappings for: " + newsrcuri);

	      ArrayList<MappingNode> newnodes = new ArrayList<MappingNode>();
	      ArrayList<OBDAMappingAxiom> newmappings = controller
	          .getMappings(newsrcuri);

	      if (newmappings != null) {
	        for (OBDAMappingAxiom dataSourceMapping : newmappings) {
	          RDBMSOBDAMappingAxiom mappingTest = (RDBMSOBDAMappingAxiom) dataSourceMapping;
	          if (testFilters(mappingTest))
	            newnodes
	                .add(getMappingNodeFromMapping(mappingTest));
	        }
	      }
	      root.removeAllChildren();
	      for (MappingNode newnode : newnodes) {
	        root.insert(newnode, root.getChildCount());
	      }
	      nodeStructureChanged(root);
			} 
			else {
				root.setUserObject("No src uri");
			}
		} 
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/***************************************************************************
	 * Gets the mapping node for the mapping identified by mappingid
	 *
	 * @param mappingid
	 * @return
	 */
	public MappingNode getMappingNode(String mappingid) {
		for (int i = 0; i < root.getChildCount(); i++) {
			MappingNode mapping = (MappingNode) root.getChildAt(i);
			if (mapping.getMappingID().equals(mappingid)) {
				return mapping;
			}
		}
		return null;
	}

	/***************************************************************************
	 * Gets the mapping node that corresponds to the mapping.
	 *
	 * @param mapping
	 * @return
	 */
	private MappingNode getMappingNodeFromMapping(OBDAMappingAxiom mapping) {
		MappingNode mappingnode = new MappingNode(mapping.getId());
		Query srcquery = mapping.getSourceQuery();
		CQIE tgtquery = (CQIEImpl) mapping.getTargetQuery();
		MappingBodyNode body = null;
		MappingHeadNode head = null;
		if (srcquery != null) {
			SourceQueryToTextCodec codec = new SourceQueryToTextCodec(apic);
			body = new MappingBodyNode(codec.encode(srcquery));
		} else {
			body = new MappingBodyNode("");
		}
		if (tgtquery != null) {
			TargetQeryToTextCodec codec = new TargetQeryToTextCodec(apic);
			head = new MappingHeadNode(codec.encode(tgtquery));
		} else {
			head = new MappingHeadNode("");
		}
		mappingnode.add(head);
		mappingnode.add(body);

		return mappingnode;
	}

	/**
	 * Synchronizes an array of mapping axioms to the tree node structure
	 * following a particular data source URI. Each data source has a collection
	 * of mapping axioms in which each axiom contains a source query and a target
	 * query.
	 *
	 * @param datasourceUri a data source uri.
	 * @param mappings an array of mapping axioms.
	 * @see OBDAMappingAxiom
	 * @see SourceQuery
	 * @see TargetQuery
	 */
	public void synchronize(URI datasourceUri, ArrayList<OBDAMappingAxiom> mappings) {
	  int size = mappings.size();
	  for(int i = 0; i < size; i++) {
	    String mappingId = mappings.get(i).getId();
	    mappingInserted(datasourceUri, mappingId);
	  }
	}

	/*
	 * @see
	 * inf.unibz.it.obda.api.controller.MappingControllerListener#allMappingsRemoved
	 * ()
	 */
	public void allMappingsRemoved() {
		try {
			root.removeAllChildren();
			nodeStructureChanged(root);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void ontologyChanged() {
		try {
			DataSource ds = apic.getDatasourcesController().getCurrentDataSource();
			if(ds!=null){
				ArrayList<MappingNode> newnodes = new ArrayList<MappingNode>();
				ArrayList<OBDAMappingAxiom> newmappings = controller.getMappings(ds.getSourceID());
				if (newmappings != null) {
					for (OBDAMappingAxiom dataSourceMapping : newmappings) {
						newnodes.add(getMappingNodeFromMapping(dataSourceMapping));
					}
				}
				root.removeAllChildren();
				for (MappingNode newnode : newnodes) {
					root.insert(newnode, root.getChildCount());
				}
				nodeStructureChanged(root);
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#addFilter
	 * (inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter)
	 */
	@Override
	public void addFilter(TreeModelFilter<OBDAMappingAxiom> T) {
		ListFilters.add(T);
	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#addFilters
	 * (java.util.List)
	 */
	@Override
	public void addFilters(List<TreeModelFilter<OBDAMappingAxiom>> T) {
		// TODO Auto-generated method stub
		for (int i = 0; i < T.size(); i++) {
			ListFilters.add(T.get(i));
		}
	}

	/*
	 * @seeinf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#
	 * removeAllFilters()
	 */
	@Override
	public void removeAllFilters() {
		ListFilters.clear();
	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#removeFilter
	 * (inf.unibz.it.obda.gui.swing.treemodel.filter.TreeModelFilter)
	 */
	@Override
	public void removeFilter(TreeModelFilter<OBDAMappingAxiom> filter) {
		// TODO Auto-generated method stub
		Iterator<TreeModelFilter<OBDAMappingAxiom>> iter = ListFilters.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			if (element.equals(filter))
				ListFilters.remove(element);
			break;
		}
	}

	/*
	 * @see
	 * inf.unibz.it.obda.gui.swing.treemodel.filter.FilteredTreeModel#removeFilter
	 * (java.util.List)
	 */
	@Override
	public void removeFilter(List<TreeModelFilter<OBDAMappingAxiom>> filters) {
		// TODO Auto-generated method stub
		Iterator<TreeModelFilter<OBDAMappingAxiom>> iter = ListFilters.iterator();
		while (iter.hasNext()) {
			Object element = iter.next();
			ListFilters.remove(element);
		}
	}

	/******
	 * This function compares with all the elements of the list of filters
	 * "ListFilters", if any of it doesn't match then it returns false
	 *
	 * @param mapping
	 * @return allFiltersTrue
	 *
	 */
	private boolean testFilters(RDBMSOBDAMappingAxiom mapping) {
		boolean allFiltersTrue = true;
		for (int i = 0; i < ListFilters.size(); i++) {
			allFiltersTrue = allFiltersTrue
					&& ListFilters.get(i).match(mapping);
		}
		return allFiltersTrue;
	}

	private CQIE parse(String query) {
		CQIE cq = null;
		query = prepareQuery(query);
		try {
			datalogParser.parse(query);
			cq = datalogParser.getRule(0);
		}
		catch (RecognitionException e) {
			log.warn(e.getMessage());
		}
		return cq;
	}

	private String prepareQuery(String input) {
		String query = "";
		DatalogQueryHelper queryHelper =
			new DatalogQueryHelper(apic.getIOManager().getPrefixManager());

		String[] atoms = input.split(DatalogQueryHelper.DATALOG_IMPLY_SYMBOL, 2);
		if (atoms.length == 1)  // if no head
			query = queryHelper.getDefaultHead() + " " +
			 	DatalogQueryHelper.DATALOG_IMPLY_SYMBOL + " " +
			 	input;

		// Append the prefixes
		query = queryHelper.getPrefixes() + query;

		return query;
	}
}
