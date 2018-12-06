package it.polito.dp2.RNS.sol2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.sun.xml.internal.ws.runtime.config.ObjectFactory;

import java.net.URI;

import it.polito.dp2.RNS.PlaceReader;
import it.polito.dp2.RNS.RnsReader;
import it.polito.dp2.RNS.RnsReaderException;
import it.polito.dp2.RNS.RnsReaderFactory;
import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;
import it.polito.dp2.RNS.rest.jaxb.Node;
import it.polito.dp2.RNS.rest.jaxb.NodeResult;
import it.polito.dp2.RNS.rest.jaxb.Relationship;
import it.polito.dp2.RNS.rest.jaxb.RelationshipResult;

public class PathFinder_ implements it.polito.dp2.RNS.lab2.PathFinder {
	
	private WebTarget			target;		// base URL
	private Client				client;
	private RnsReader 			monitor;	// used to access to the interface
	private RnsReaderFactory	rFactory;	// factory used to instantiate the interface
	private Status				status;
	private Map<URI,String>		n_map;		// loaded nodes
	private Map<URI, String> 	r_map;		// loaded relationships
	
	
	
	public PathFinder_(String sys_property){
		
		// The implementation of the interfaces to be used as data source
		// must be selected using the  `abstract factory` pattern:
		// we must create the data source by instantiating 
		// `it.polito.dp2.RNS.RnsReaderFactory` by means of its static method
		// `newInstance()`
		this.rFactory	= RnsReaderFactory.newInstance();
		try {
			this.monitor	= this.rFactory.newRnsReader();
		} catch (RnsReaderException e) {
			e.printStackTrace();
			System.err.println(" `factory.newRnsReader()` exception");
		}
		
		
		// setting the web target
		this.client = ClientBuilder.newClient(); 		// create the Client object
		this.target = this.client.target(sys_property); // create a web target for the main URI
		
		// setting the status
		this.status = Status.NOT_LOADED;
		
		
		this.n_map = new HashMap<>();
		this.r_map = new HashMap<>();
		
		// new factory for the geneated classes in 
		//this.oFactory = new ObjectFactory();
		
	}

	
	/**
	 * Checks the current state
	 * @return true if the current state is the operating state (model loaded)
	 */
	@Override
	public boolean isModelLoaded() {
		// check `status` attribute
		if(this.status.compareTo(Status.LOADED) == 0){
			return true;	// the PathFinder is able to compute a shortest path
		}else{
			return false;	// the PathFInder is not able to compute a shortest path
			// TODO: throw an exception
		}
	}
	
	/**
	 * Loads the current version of the model so that, if the operation is successful,
	 * after the operation the PathFinder is in the operating state (model loaded) and
	 * it can compute shortest paths on the loaded model.
	 * @throws ServiceException if the operation cannot be completed because the remote service is not available or fails
	 * @throws ModelException if the operation cannot be completed because the current model cannot be read or is wrong (the problem is not related to the remote service)
	 */
	@Override
	public void reloadModel() throws ServiceException, ModelException {
	
		for(PlaceReader reader : monitor.getPlaces(null)){ // for each place in the system
			Node node = new Node();														// create a new empty node
			node.setId(reader.getId());													// fill it with the corresponding id of the reader
			NodeResult result = insertNewNode(node);									// try to insert in Neo4j `node`	
			this.n_map.put(URI.create(result.getSelf()), reader.getId());				// store the URI identifier in `n_map` with the corresponding id
			
		}

	}

	/**
	 * Looks for the shortest paths connecting a source place to a destination place
	 * Each path is returned as a list of place identifiers, where the first place in the list is the source
	 * and the last place is the destination.
	 * @param source The id of the source of the paths to be found
	 * @param destination The id of the destination of the paths to be found
	 * @param maxlength The maximum length of the paths to be found (0 or negative means no bound on the length)
	 * @return the set of the shortest paths connecting source to destination
	 * @throws UnknownIdException if source or destination is not a known place identifier
	 * @throws BadStatedException if the operation is called when in the initial state (no model loaded)
	 * @throws ServiceException if the operation cannot be completed because the remote service is not available or fails
	 */
	@Override
	public Set<List<String>> findShortestPaths(String source, String destination, int maxlength)
			throws UnknownIdException, BadStateException, ServiceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	// TODO new node
	public NodeResult insertNewNode(Node n){
		return null;
	}
	
	// TODO new relationship
	public RelationshipResult insertRelationship(Relationship r){
		return null;
	}
	

}
