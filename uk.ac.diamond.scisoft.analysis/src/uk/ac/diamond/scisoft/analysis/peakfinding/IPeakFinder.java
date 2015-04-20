/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.peakfinding;

import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public interface IPeakFinder {
	
	/**
	 * Get the name of the peak finding method.
	 * 
	 * @return peak finder's name
	 */
	public String getName();
	
	/**
	 * Get the map of parameters which can be set for this method.
	 * 
	 * @return <String name, IParameter value> map of all parameters available 
	 * to control this IPeakFinder. 
	 */
	public Map<String, Number> getParameters();
	
	/**
	 * Get a named parameter from the map of parameters.
	 * 
	 * @param pName name of the parameter in the map
	 * @return parameter value
	 */
	public Number getParameter(String pName) throws Exception;
	
	/**
	 * Change the current state of a named parameter to the given state.
	 * 
	 * @param pName name of the parameter to update
	 * @param pValue parameter value
	 */
	public void setParameter(String pName, Number pValue) throws Exception;
	
	/**
	 * An implementation of an algorithm capable of identifying a number peaks
	 * within a one dimensional dataset. As it's input, this method takes the
	 * dataset and two optional arguments, the x-coordinate dataset and the
	 * maximum number of peaks to find.
	 * This algorithm may have a set of additional parameters to control it's
	 * behaviour, which may be accessed/changed using the getters/setters of 
	 * this class.
	 * 
	 * @param xData x-coordinate dataset
	 * @param yData histogram/distribution dataset to find peaks in
	 * @param nPeaks maximum number of peaks to find
	 * @return A set containing all or a number of peaks found by this IPeakFinder
	 */
	public Set<Double> findPeaks(IDataset xData, IDataset yData, Integer nPeaks);

}
