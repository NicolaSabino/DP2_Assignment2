package it.polito.dp2.RNS.sol2;

import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;

public class PathFinder implements it.polito.dp2.RNS.lab2.PathFinder {
	
	private WebTarget target;
	private Client client;
	
	public PathFinder() {
		// TODO Auto-generated constructor stub
		
		// create the client object
		this.client = ClientBuilder.newClient();
		
		// create a web target for the main URI
		//this.target = client.target(getBaseURI()).path("negotiateService");
	}

	
	/**
	 * Checks the current state
	 * @return true if the current state is the operating state (model loaded)
	 */
	@Override
	public boolean isModelLoaded() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub

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
	

}
