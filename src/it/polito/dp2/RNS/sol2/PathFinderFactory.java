package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.lab2.PathFinderException;

public class PathFinderFactory extends it.polito.dp2.RNS.lab2.PathFinderFactory {
	

	@Override
	public PathFinder newPathFinder() throws PathFinderException {
		
		// check if the system property is properly setted
		if(System.getProperty("it.polito.dp2.RNS.lab2.URL") == null){
			System.err.println("the system property  `it.polito.dp2.NFV.lab2.URL` is null");
			throw new PathFinderException();
		}
		
		// create a new path finder
		PathFinder f = new PathFinder(System.getProperty("it.polito.dp2.RNS.lab2.URL"));
		return f;
		
	}

}