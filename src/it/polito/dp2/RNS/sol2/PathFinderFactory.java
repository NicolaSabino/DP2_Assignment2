package it.polito.dp2.RNS.sol2;

import java.util.List;
import java.util.Set;

import it.polito.dp2.RNS.lab2.BadStateException;
import it.polito.dp2.RNS.lab2.ModelException;
import it.polito.dp2.RNS.lab2.ServiceException;
import it.polito.dp2.RNS.lab2.UnknownIdException;

public class PathFinderFactory implements it.polito.dp2.RNS.lab2.PathFinder {

	@Override
	public boolean isModelLoaded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reloadModel() throws ServiceException, ModelException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<List<String>> findShortestPaths(String source, String destination, int maxlength)
			throws UnknownIdException, BadStateException, ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

}