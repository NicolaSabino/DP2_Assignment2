package it.polito.dp2.RNS.sol2;

import it.polito.dp2.RNS.lab2.PathFinder;
import it.polito.dp2.RNS.lab2.PathFinderException;

public class PathFinderFactory_ extends it.polito.dp2.RNS.lab2.PathFinderFactory {


	@Override
	public PathFinder newPathFinder() throws PathFinderException {
		// TODO Auto-generated method stub
		
		// check if the system property is properly setted
		if(System.getProperty("it.polito.dp2.RNS.lab2.URL") == null){
			System.err.println("the system property  `it.polito.dp2.NFV.lab2.URL` is null");
			throw new PathFinderException();
		}
		
		// create a new path finder
		PathFinder_ f = new PathFinder_();
		return f;
	}

}