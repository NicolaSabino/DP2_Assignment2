package it.polito.dp2.RNS.sol2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import java.math.BigInteger;
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
import it.polito.dp2.RNS.rest.jaxb.Paths;
import it.polito.dp2.RNS.rest.jaxb.Paths.Path;
import it.polito.dp2.RNS.rest.jaxb.PathsRequest;
import it.polito.dp2.RNS.rest.jaxb.Relationship;
import it.polito.dp2.RNS.rest.jaxb.RelationshipResult;

public class PathFinder implements it.polito.dp2.RNS.lab2.PathFinder {
	
	private WebTarget			target;			// base URL
	private Client				client;
	private RnsReader 			monitor;		// used to access to the interface
	private RnsReaderFactory	rFactory;		// factory used to instantiate the interface
	private Status				status;
	private Map<String,URI>		sys_link_map;	// `system identifier - db URI resource` mapping
	private Map<URI,String>		link_sys_map;	// `db URI resource - system identifier` mapping
	private Map<String,Integer>	sys_db_map;		// `db identifier - system identifier` mapping	
	private Set<URI>			r_set;			// loaded relationships
	
	
	
	public PathFinder(String sys_property){
		
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
		
		
		this.sys_link_map = new HashMap<>();
		this.link_sys_map = new HashMap<>();
		this.sys_db_map = new HashMap<>();
		this.r_set = new HashSet<>();
			
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
		
		Set<PlaceReader> set = monitor.getPlaces(null); // get a random set of places
		if(set == null) throw new ModelException("Exception during reading random datas");
	
		// -- NODES --
		for(PlaceReader reader : set ){ // for each place in the system
			Node node = new Node();														// create a new empty node
			node.setId(reader.getId());													// fill it with the corresponding id of the reader
			NodeResult result = insertNewNode(node);									// try to insert in Neo4j `node`	
			this.sys_link_map.put(reader.getId(),URI.create(result.getSelf()));			// store the URI identifier in `n_map` with the corresponding id
			this.sys_db_map.put(reader.getId(),result.getMetadata().getId().intValue());
			//System.out.println("NODE:" + reader.getId() + "\t\t" + result.getSelf() );
		}
		// -- RELATIONSHIPS --
		for(PlaceReader reader : set){ // for each place in the system
			//System.out.println(reader.getId());
			for(PlaceReader reader2 : reader.getNextPlaces()){	// for each next hop
				//System.out.println("\t"+reader2.getId());
				Relationship relationship = new Relationship();							// create a new empty relationship
				URI uri = sys_link_map.get(reader2.getId());							// get the URI associated with the id in `n_map`
				int id = this.sys_db_map.get(reader.getId());							// retrieve the from node identifier
				relationship.setTo(uri.toString());										// set this URI in `to` field
				relationship.setType("ConnectedTo");									// set the connection type
				RelationshipResult result = insertRelationship(relationship,id);			// try to insert in Neo4j `relationship`
				this.r_set.add(URI.create(result.getSelf()));							// store the URI identifier in `r_set`
			}
		}
		
		this.status = Status.LOADED; // change the state

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
		Set<List<String>> resultSet = new HashSet<List<String>>();	
		Paths result;
		
		System.out.println("source:" + source + " destination:" + destination + " maxlenght:" + maxlength);
		
		if(this.status == Status.NOT_LOADED)						// if the operation is called when in the initial state 
			throw new BadStateException("No model loaded");			// (no model loaded) throw an exception														
		Integer from = this.sys_db_map.get(source);					// get the `from` identifier  previously stored
		if(from == null)											// Check if `source` is reliable
			throw new UnknownIdException("Bad `from` identifier");					
		URI to = this.sys_link_map.get(destination); 				// get the `to` resource previously stored
		if(to == null)												// check id `destination is reliable
			throw new UnknownIdException("Bad `to` identifier");						
		PathsRequest request = new PathsRequest();					// create a new path request
		request.setMaxDepth(BigInteger.valueOf(maxlength));			// set `maxlength`
		request.setTo(to.toString());								// set `to`
		
		// -- CALCULATE THE SHORTEST PATHS --
		try{
			result = this.target															// base URI
					.path("data").path("node").path(String.valueOf(from)).path("paths")		// `/data/node/{nodeFromID}/paths`
					.request(MediaType.APPLICATION_JSON)									// start building a request and define the response media types.
					.post(Entity.entity(request, MediaType.APPLICATION_JSON), Paths.class);	// build a POST request invocation
				// if we obtain an empty result, throw an exception
				if(result == null)
					throw new ServiceException("An ecxception occurred while uploading");
		}catch (Exception e) {
			throw new ServiceException("Exception during shortest paths calculation");
		}
		
		// at this point we have several paths stored in `result`
		for(Path path : result.getPath()){ 				// for each path stored in `result`
			List<String> tmp = new ArrayList<>();		// create a temporary list
			for(String s : path.getNodes().getNode()){	// for each node in the path
				String id = this.link_sys_map.get(s);	// calculate the correspondig place stored in `link_sys_map`
				if(id == null) throw new UnknownIdException("Bad id");
				tmp.add(id);							// add `id` to the temp list
				System.out.print(id + " ");
			}
			System.out.println("");
			resultSet.add(tmp);							// add `tmp` to the `resultSet`
		}
		
		return resultSet;
	}
	
	/**
	 * Insert a node in Neo4j
	 * Syntax:		POST http://localhost:7474/db/data/node
	 * Body[JSON]:	{ "id" : "identifier" } 
	 * @param n
	 * @return An object representing the result of the insert operation
	 * @throws ServiceException
	 */
	public NodeResult insertNewNode(Node n) throws ServiceException {
		NodeResult result;																// create an empty result
		try{																			// try to insert the node 
			result = this.target														// base URI
				.path("data").path("node")												// `/data/node`
				.request(MediaType.APPLICATION_JSON)									// start building a request and define the response media types.
				.post(Entity.entity(n, MediaType.APPLICATION_JSON), NodeResult.class);	// build a POST request invocation
			// if we obtain an empty result, throw an exception
			if(result == null) throw new ServiceException("An ecxception occurred while uploading");
		}catch(Exception e){
			throw new ServiceException("Exception during node uploading");
		}
		return result;
	}
	
	/**
	 * Insert a node in Neo4j
	 * Syntax:		POST http://localhost:7474/db/data/node/{nodeFromID}/relationships
	 * Body[JSON]:	{ "to" : "http://localhost:7474/db/data/node/{nodeToID}", "type" : "ConnectedTo" } 
	 * @param r
	 * @param id 
	 * @return An object representing the result of the insert operation
	 * @throws ServiceException
	 */
	public RelationshipResult insertRelationship(Relationship r, int id) throws ServiceException{
		
		RelationshipResult result;																// create an empty result
		try{																					// try to insert the relationship 
			result = this.target																// base URI
				.path("data").path("node")														// `/data/node/`
				.path(String.valueOf(id)).path("relationships")									// `{nodeFromID}/relationships`
				.request(MediaType.APPLICATION_JSON)											// start building a request and define the response media types.
				.post(Entity.entity(r, MediaType.APPLICATION_JSON), RelationshipResult.class);	// build a POST request invocation
			// if we obtain an empty result, throw an exception
			if(result == null) throw new ServiceException("An ecxception occurred while uploading");
		}catch(Exception e){
			throw new ServiceException("Exception during relationship uploading");
		}
		return result;
	}
	

}