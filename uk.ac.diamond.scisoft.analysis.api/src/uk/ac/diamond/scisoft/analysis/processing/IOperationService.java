/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.processing;

import java.util.Collection;

/**
 * This is a service for creating and returning operations.
 * 
 * The service is provided by another plugin and returned using OSGI.
 */
public interface IOperationService {
	
	/**
	 * Finds an operation by a string which does a search
	 * on the regex passed in. All operations whose descriptions
	 * match the regex are returned. For instance:
	 * 
	 * find("correction");  // Might give operations "Dark correction", "Flat correction"
	 * find("integration"); // Might give operations "Azimuthal integration", "Radial integration", "Box integration", "Line integration"
	 * 
	 * NOTE the regex will be checked if matching on the description, and if not the description in lower case.
	 * 
	 * @param operationRegex
	 * @return list of operations which match
	 */
	public Collection<IOperation> find(String operationRegex)  throws Exception;
	
	/**
	 * Gets the ids of all the operations. The id is defined in the extension point.
	 * @return all the ids which have been contributed via extensions
	 */
	public Collection<String> getRegisteredOperations()  throws Exception;

	/**
	 * Creates an operation by using its type. This method will create a new
	 * operation using the no argument constructor.
	 * 
	 * @return IOperation
	 */
	public IOperation create(String operationId) throws Exception;
	
	/**
	 * Executes a chain of operations in series. NOTE the fist operation must have
	 * its data set and other operations should have their parameters set before
	 * execution.
	 * 
	 * @param series
	 * @return IRichDataset result returned from last operation.
	 * @throws OperationException
	 */
	public IRichDataset executeSeries(IOperation... series) throws OperationException;
	
	/**
	 * Runs a set of operations by following a graph chaining the operations together.
	 * This run uses a recursive method and 
	 * @param root
	 * @return IRichDataset
	 */
	// If we start to need things like this:
	//public IRichDataset executeGraph(IOperationNode root) throws OperationException;
	// Then this service has inadvertently become a workflow tool and we are reinventing ptolemy
}
